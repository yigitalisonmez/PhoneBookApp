package com.example.phonebookapp.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phonebookapp.presentation.ui.common.IosTextField
import com.example.phonebookapp.presentation.ui.common.LocalScreenDimensions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.phonebookapp.presentation.ui.common.IosPhoneTextField
import kotlinx.coroutines.launch
import android.util.Log
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.FileProvider
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.SuccessResult
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.BlurMaskFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@Composable
fun ProfileBottomSheet(
    contactId: String,
    onDismiss: () -> Unit,
    onEdit: (String) -> Unit,
    onRequestDelete: (String) -> Unit,
    onContactUpdated: () -> Unit = {},
    initialEditMode: Boolean = false,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val context = LocalContext.current
    val screenWidth = LocalScreenDimensions.current.width

    // Dominant renk için state
    var dominantColor by remember { mutableStateOf(Color(0xFFF8BBD0)) } // Default pembe

    LaunchedEffect(contactId, initialEditMode) { 
        viewModel.initialize(contactId, initialEditMode)
    }

    val scope = rememberCoroutineScope()
    
    var showActionMenu by remember { mutableStateOf(false) }
    var showImageSourceBottomSheet by remember { mutableStateOf(false) }
    
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Görselden dominant renk çıkar
    suspend fun extractDominantColor(imageUrl: String) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Palette için gerekli
                .build()
            
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap
            
            bitmap?.let {
                withContext(Dispatchers.Default) {
                    val palette = Palette.from(it).generate()
                    
                    // En baskın rengi bul (vibrant, muted, dark vibrant vs.)
                    val color = palette.vibrantSwatch?.rgb
                        ?: palette.lightVibrantSwatch?.rgb
                        ?: palette.darkVibrantSwatch?.rgb
                        ?: palette.mutedSwatch?.rgb
                        ?: palette.lightMutedSwatch?.rgb
                        ?: palette.darkMutedSwatch?.rgb
                    
                    color?.let { colorInt ->
                        dominantColor = Color(colorInt)
                        Log.d("ProfileBottomSheet", "Dominant color extracted: $dominantColor")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileBottomSheet", "Error extracting dominant color", e)
        }
    }
    
    // Görsel URL değiştiğinde rengi güncelle
    LaunchedEffect(state.imageUrl, state.editImageUrl) {
        val imageUrl = if (state.isEditMode) state.editImageUrl else (state.imageUrl ?: "")
        if (imageUrl.isNotEmpty()) {
            extractDominantColor(imageUrl)
        } else {
            dominantColor = Color(0xFFF8BBD0) // Default pembe
        }
    }
    
    fun createImageUri(): Uri {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.cacheDir
        val imageFile = java.io.File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("ProfileBottomSheet", "Gallery picker result: $uri")
        uri?.let { selectedUri ->
            try {
                Log.d("ProfileBottomSheet", "Processing selected image: $selectedUri")
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                inputStream?.let { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    Log.d("ProfileBottomSheet", "Bitmap decoded successfully, size: ${bitmap.width}x${bitmap.height}")
                    
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val imageBytes = outputStream.toByteArray()
                    Log.d("ProfileBottomSheet", "Image converted to bytes, size: ${imageBytes.size} bytes")
                    
                    Log.d("ProfileBottomSheet", "Starting image upload to API")
                    viewModel.uploadProfileImage(imageBytes) {
                        Log.d("ProfileBottomSheet", "Image upload successful, refreshing contacts list")
                        onContactUpdated()
                    }
                } ?: run {
                    Log.e("ProfileBottomSheet", "Failed to open input stream for URI: $selectedUri")
                }
            } catch (e: Exception) {
                Log.e("ProfileBottomSheet", "Error processing selected image", e)
            }
        } ?: run {
            Log.d("ProfileBottomSheet", "No image selected by user")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        Log.d("ProfileBottomSheet", "Camera result: $success")
        if (success) {
            Log.d("ProfileBottomSheet", "Camera photo taken successfully")
            photoUri?.let { uri ->
                try {
                    Log.d("ProfileBottomSheet", "Processing camera photo from URI: $uri")
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.let { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        Log.d("ProfileBottomSheet", "Camera bitmap decoded: ${bitmap.width}x${bitmap.height}")
                        
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val imageBytes = outputStream.toByteArray()
                        Log.d("ProfileBottomSheet", "Camera image converted to bytes: ${imageBytes.size} bytes")
                        
                        Log.d("ProfileBottomSheet", "Starting camera image upload to API")
                        viewModel.uploadProfileImage(imageBytes) {
                            Log.d("ProfileBottomSheet", "Image upload successful, refreshing contacts list")
                            onContactUpdated()
                        }
                    } ?: run {
                        Log.e("ProfileBottomSheet", "Failed to open input stream for camera URI: $uri")
                    }
                } catch (e: Exception) {
                    Log.e("ProfileBottomSheet", "Error processing camera image", e)
                }
            } ?: run {
                Log.e("ProfileBottomSheet", "Photo URI is null, cannot process camera image")
            }
        } else {
            Log.d("ProfileBottomSheet", "Camera photo cancelled")
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("ProfileBottomSheet", "CAMERA permission granted: $isGranted")
        if (isGranted) {
            try {
                photoUri = createImageUri()
                Log.d("ProfileBottomSheet", "Created photo URI: $photoUri")
                cameraLauncher.launch(photoUri)
            } catch (e: Exception) {
                Log.e("ProfileBottomSheet", "Error launching camera", e)
            }
        } else {
            Log.e("ProfileBottomSheet", "CAMERA permission denied")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("ProfileBottomSheet", "Permission granted, showing bottom sheet")
            showImageSourceBottomSheet = true
        } else {
            Log.e("ProfileBottomSheet", "Permission denied")
        }
    }

    fun selectImage() {
        Log.d("ProfileBottomSheet", "selectImage() called")
        
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        val hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        Log.d("ProfileBottomSheet", "Permission check for $permission: $hasPermission")
        
        if (hasPermission) {
            Log.d("ProfileBottomSheet", "Permission already granted, showing image source bottom sheet")
            showImageSourceBottomSheet = true
        } else {
            Log.d("ProfileBottomSheet", "Requesting permission: $permission")
            permissionLauncher.launch(permission)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            // Main content card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 8.dp)
                    ) {
                        if (state.isEditMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { 
                                        viewModel.exitEditMode()
                                        onDismiss()
                                    }
                                ) {
                                    Text(
                                        text = "Cancel",
                                        color = Color(0xFF007AFF),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.W400,
                                        fontFamily = FontFamily.SansSerif,
                                        letterSpacing = (-0.4).sp
                                    )
                                }
                                
                                Text(
                                    text = "Edit Contact",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.W600,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color(0xFF1D1D1F),
                                    letterSpacing = (-0.4).sp
                                )
                                
                                TextButton(
                                    onClick = { 
                                        viewModel.saveContact {
                                            onEdit(contactId)
                                            onDismiss()
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Done",
                                        color = Color(0xFF007AFF),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.W600,
                                        fontFamily = FontFamily.SansSerif,
                                        letterSpacing = (-0.4).sp
                                    )
                                }
                            }
                        } else {
                            // View mode - Back button on left, 3 dots on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color(0xFF007AFF)
                                    )
                                }
                                
                                IconButton(onClick = { showActionMenu = !showActionMenu }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More",
                                        tint = Color(0xFF1D1D1F)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Avatar with custom shadow
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Custom shadow layer
                        Canvas(
                            modifier = Modifier
                                .size(120.dp)
                                .offset(y = 4.dp)
                        ) {
                            drawIntoCanvas { canvas ->
                                val paint = Paint().asFrameworkPaint()
                                paint.color = dominantColor.copy(alpha = 0.4f).toArgb()
                                paint.maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
                                
                                canvas.nativeCanvas.drawCircle(
                                    size.width / 2,
                                    size.height / 2,
                                    size.width / 2,
                                    paint
                                )
                            }
                        }
                        
                        // Avatar content
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(dominantColor.copy(alpha = 0.15f))
                                .clickable { 
                                    if (state.isEditMode) {
                                        selectImage()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val currentImageUrl = if (state.isEditMode) state.editImageUrl else (state.imageUrl ?: "")
                            
                            if (currentImageUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(context)
                                            .data(currentImageUrl)
                                            .build()
                                    ),
                                    contentDescription = "Profile Image",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val currentFirstName = if (state.isEditMode) state.editFirstName else state.firstName
                                val initial = currentFirstName.firstOrNull()?.uppercase() ?: ""
                                Text(
                                    text = initial.toString(),
                                    fontSize = 48.sp,
                                    color = Color(0xFF1D1D1F),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Change Photo - her zaman tıklanabilir
                    Text(
                        text = "Change Photo",
                        fontSize = 18.sp,
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.3).sp,
                        modifier = Modifier.clickable { selectImage() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (state.isEditMode) {
                            IosTextField(
                                value = state.editFirstName,
                                onValueChange = { viewModel.updateEditFirstName(it) },
                                label = "First Name",
                                enabled = true
                            )

                            IosTextField(
                                value = state.editLastName,
                                onValueChange = { viewModel.updateEditLastName(it) },
                                label = "Last Name",
                                enabled = true
                            )

                            IosPhoneTextField(
                                value = state.editPhoneNumber,
                                onValueChange = { viewModel.updateEditPhoneNumber(it) },
                                label = "Phone Number"
                            )
                        } else {
                            IosTextField(
                                value = state.firstName,
                                onValueChange = { /* Read only */ },
                                label = "First Name",
                                enabled = false
                            )

                            IosTextField(
                                value = state.lastName,
                                onValueChange = { /* Read only */ },
                                label = "Last Name",
                                enabled = false
                            )

                            IosTextField(
                                value = state.phoneNumber,
                                onValueChange = { /* Read only */ },
                                label = "Phone Number",
                                enabled = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action menu overlay
                    if (showActionMenu) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 0.dp, y = (-300).dp)
                                    .width(screenWidth * 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showActionMenu = false
                                                viewModel.enterEditMode()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Edit",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color(0xFF1D1D1F),
                                            fontWeight = FontWeight.Normal
                                        )
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color(0xFF1D1D1F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                
                                    HorizontalDivider(
                                        color = Color(0xFFE5E5E5),
                                        thickness = 0.8.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showActionMenu = false
                                                onRequestDelete(state.id)
                                                onDismiss()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Delete",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color(0xFFFF3B30),
                                            fontWeight = FontWeight.Normal
                                        )
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFFF3B30),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Image source bottom sheet - separate dialog
    if (showImageSourceBottomSheet) {
        ImageSourceBottomSheet(
            onDismiss = { showImageSourceBottomSheet = false },
            onCameraClick = {
                showImageSourceBottomSheet = false
                val cameraPermission = android.Manifest.permission.CAMERA
                val hasCameraPermission = ContextCompat.checkSelfPermission(
                    context, 
                    cameraPermission
                ) == PackageManager.PERMISSION_GRANTED
                
                if (hasCameraPermission) {
                    try {
                        photoUri = createImageUri()
                        cameraLauncher.launch(photoUri)
                    } catch (e: Exception) {
                        Log.e("ProfileBottomSheet", "Error launching camera", e)
                    }
                } else {
                    cameraPermissionLauncher.launch(cameraPermission)
                }
            },
            onGalleryClick = {
                showImageSourceBottomSheet = false
                galleryLauncher.launch("image/*")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageSourceBottomSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val imageSheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = imageSheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Button(
                onClick = onCameraClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Camera",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGalleryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Gallery",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF007AFF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

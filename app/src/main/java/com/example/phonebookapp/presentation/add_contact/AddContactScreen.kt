package com.example.phonebookapp.presentation.add_contact

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phonebookapp.R
import com.example.phonebookapp.presentation.profile.components.ImageSourceBottomSheet
import com.example.phonebookapp.presentation.profile.components.rememberImagePickerState
import com.example.phonebookapp.presentation.ui.common.IosPhoneTextField
import com.example.phonebookapp.presentation.ui.common.IosTextField
import com.example.phonebookapp.presentation.ui.common.LocalScreenDimensions
import com.example.phonebookapp.presentation.ui.success.SuccessScreen
import com.example.phonebookapp.ui.theme.*

@Composable
fun AddContactScreen(
    viewModel: AddContactViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state.value
    val screenWidth = LocalScreenDimensions.current.width
    
    var showImageSourceBottomSheet by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Image picker state
    val imagePickerState = rememberImagePickerState { imageBytes ->
        Log.d("AddContactScreen", "Image selected, storing in temp state")
        viewModel.onEvent(AddContactEvent.ImageSelected(imageBytes))
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imagePickerState.handleGalleryResult(uri)
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        imagePickerState.handleCameraResult(success)
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("AddContactScreen", "CAMERA permission granted: $isGranted")
        if (isGranted) {
            imagePickerState.launchCamera(cameraLauncher)
        } else {
            Log.e("AddContactScreen", "CAMERA permission denied")
        }
    }

    // General permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("AddContactScreen", "Permission granted, showing bottom sheet")
            showImageSourceBottomSheet = true
        } else {
            Log.e("AddContactScreen", "Permission denied")
        }
    }

    // Success handler
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            showSuccess = true
        }
    }

    // Error handler
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.onEvent(AddContactEvent.DismissError)
        }
    }

    fun selectImage() {
        imagePickerState.checkAndRequestPermission(permissionLauncher) {
            showImageSourceBottomSheet = true
        }
    }

    Dialog(
        onDismissRequest = onNavigateBack,
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
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onNavigateBack) {
                                Text(
                                    "Cancel",
                                    color = IosBlue,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.W400,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = (-0.4).sp
                                )
                            }

                            Text(
                                text = "New Contact",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = IosDarkText,
                                letterSpacing = (-0.5).sp
                            )

                            val isDoneEnabled = state.firstName.isNotBlank() &&
                                    state.phoneNumber.isNotBlank() &&
                                    !state.isLoading

                            TextButton(
                                onClick = { viewModel.onEvent(AddContactEvent.SaveContact) },
                                enabled = isDoneEnabled
                            ) {
                                Text(
                                    "Done",
                                    color = if (isDoneEnabled) IosBlue else IosGrey,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.W600,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = (-0.4).sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile Photo Preview
                    if (state.tempImageBytes != null) {
                        // Seçilmiş fotoğrafı göster
                        val bitmap = remember(state.tempImageBytes) {
                            BitmapFactory.decodeByteArray(
                                state.tempImageBytes,
                                0,
                                state.tempImageBytes.size
                            )
                        }
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected Photo",
                            modifier = Modifier
                                .size(screenWidth * 0.4f)
                                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                                .clip(CircleShape)
                                .clickable { selectImage() },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Vector Asset
                        Image(
                            painter = painterResource(id = R.drawable.person),
                            contentDescription = "Add Photo",
                            modifier = Modifier
                                .size(screenWidth * 0.4f)
                                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                                .clickable { selectImage() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add Photo",
                        fontSize = 18.sp,
                        color = IosBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.3).sp,
                        modifier = Modifier.clickable { selectImage() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form Fields
                    IosTextField(
                        value = state.firstName,
                        onValueChange = { viewModel.onEvent(AddContactEvent.FirstNameChanged(it)) },
                        label = "First Name",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    IosTextField(
                        value = state.lastName,
                        onValueChange = { viewModel.onEvent(AddContactEvent.LastNameChanged(it)) },
                        label = "Last Name",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    IosPhoneTextField(
                        value = state.phoneNumber,
                        onValueChange = { viewModel.onEvent(AddContactEvent.PhoneNumberChanged(it)) },
                        label = "Phone Number",
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.isLoading || state.isUploadingImage) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(modifier = Modifier.width(200.dp))
                            if (state.isUploadingImage) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Uploading photo...",
                                    fontSize = 14.sp,
                                    color = IosGrey
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Success screen
    if (showSuccess) {
        SuccessScreen(
            onDismiss = {
                showSuccess = false
                onNavigateBack()
            },
            title = "All Done!",
            subtitle = "New contact saved",
            emoji = "🎉"
        )
    }

    // Image source bottom sheet
    if (showImageSourceBottomSheet) {
        ImageSourceBottomSheet(
            onDismiss = { showImageSourceBottomSheet = false },
            onCameraClick = {
                showImageSourceBottomSheet = false
                val cameraPermission = android.Manifest.permission.CAMERA
                val hasCameraPermission = ContextCompat.checkSelfPermission(
                    imagePickerState.context,
                    cameraPermission
                ) == PackageManager.PERMISSION_GRANTED

                if (hasCameraPermission) {
                    imagePickerState.launchCamera(cameraLauncher)
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
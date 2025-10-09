package com.example.phonebookapp.presentation.profile

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.res.painterResource
import com.example.phonebookapp.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phonebookapp.presentation.profile.components.*
import com.example.phonebookapp.presentation.ui.common.IosPhoneTextField
import com.example.phonebookapp.presentation.ui.common.IosTextField
import com.example.phonebookapp.presentation.ui.common.LocalScreenDimensions
import com.example.phonebookapp.presentation.ui.components.CustomSnackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch

@Composable
fun ProfilePage(
    contactId: String,
    onDismiss: () -> Unit,
    onEdit: (String) -> Unit,
    onRequestDelete: (String) -> Unit,
    onContactUpdated: () -> Unit = {},
    initialEditMode: Boolean = false,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val screenWidth = LocalScreenDimensions.current.width
    val scope = rememberCoroutineScope()
    
    var showActionMenu by remember { mutableStateOf(false) }
    var showImageSourceBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Image picker state
    val imagePickerState = rememberImagePickerState { imageBytes ->
        Log.d("ProfilePage", "Starting image upload to API")
        viewModel.uploadProfileImage(imageBytes) {
            Log.d("ProfilePage", "Image upload successful, refreshing contacts list")
            onContactUpdated()
        }
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
        Log.d("ProfilePage", "CAMERA permission granted: $isGranted")
        if (isGranted) {
            imagePickerState.launchCamera(cameraLauncher)
        } else {
            Log.e("ProfilePage", "CAMERA permission denied")
        }
    }

    // General permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("ProfilePage", "Permission granted, showing bottom sheet")
            showImageSourceBottomSheet = true
        } else {
            Log.e("ProfilePage", "Permission denied")
        }
    }
    
    // Contacts permission launcher
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d("ProfilePage", "Contacts permissions granted")
            viewModel.saveToPhoneContact()
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "User is added to your phone!",
                    duration = SnackbarDuration.Short
                )
            }
        } else {
            Log.e("ProfilePage", "Contacts permissions denied")
        }
    }

    // Initialize contact data
    LaunchedEffect(contactId, initialEditMode) { 
        viewModel.initialize(contactId, initialEditMode)
    }

    fun selectImage() {
        imagePickerState.checkAndRequestPermission(permissionLauncher) {
            showImageSourceBottomSheet = true
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
                    // Header
                    ProfileHeader(
                        isEditMode = state.isEditMode,
                        onCancelClick = { 
                            viewModel.exitEditMode()
                            onDismiss()
                        },
                        onDoneClick = { 
                            viewModel.saveContact {
                                onEdit(contactId)
                                onDismiss()
                            }
                        },
                        onBackClick = onDismiss,
                        onMoreClick = { showActionMenu = !showActionMenu }
                    )
                    
                    // Action menu - right below the 3 dot button
                    ProfileActionMenu(
                        visible = showActionMenu,
                        onEditClick = {
                            showActionMenu = false
                            viewModel.enterEditMode()
                        },
                        onDeleteClick = {
                            showActionMenu = false
                            onRequestDelete(state.id)
                            onDismiss()
                        },
                        screenWidth = screenWidth
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Avatar
                    val currentImageUrl = if (state.isEditMode) state.editImageUrl else (state.imageUrl ?: "")
                    val currentFirstName = if (state.isEditMode) state.editFirstName else state.firstName
                    
                    ProfileAvatar(
                        imageUrl = currentImageUrl.takeIf { it.isNotEmpty() },
                        firstName = currentFirstName,
                        isEditMode = state.isEditMode,
                        onAvatarClick = { selectImage() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Change Photo text
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

                    // Form fields
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

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Save to Phone Contact Button
                    if (!state.isEditMode) {
                        OutlinedButton(
                            onClick = { 
                                if (!state.isSavedToPhone) {
                                    // Check contacts permissions
                                    val hasReadPermission = ContextCompat.checkSelfPermission(
                                        imagePickerState.context,
                                        android.Manifest.permission.READ_CONTACTS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    val hasWritePermission = ContextCompat.checkSelfPermission(
                                        imagePickerState.context,
                                        android.Manifest.permission.WRITE_CONTACTS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (hasReadPermission && hasWritePermission) {
                                        viewModel.saveToPhoneContact()
                                        onContactUpdated() // ContactsScreen'i yenile
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "User is added to your phone!",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        contactsPermissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.READ_CONTACTS,
                                                android.Manifest.permission.WRITE_CONTACTS
                                            )
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (state.isSavedToPhone) Color(0xFFF0F0F0) else Color.White,
                                contentColor = if (state.isSavedToPhone) Color(0xFF8E8E93) else Color.Black
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                if (state.isSavedToPhone) Color(0xFF8E8E93) else Color(0xFF000000)
                            ),
                            enabled = !state.isSavedToPhone
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.bookmark),
                                contentDescription = "Save",
                                tint = if (state.isSavedToPhone) Color(0xFF8E8E93) else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save to My Phone Contact",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (state.isSavedToPhone) Color(0xFF8E8E93) else Color.Black
                            )
                        }
                        
                        // Info row - show if saved
                        if (state.isSavedToPhone) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = Color(0xFF6C757D),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "This contact is already saved to your phone.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6C757D),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
        
        // Snackbar Host - altta
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            SnackbarHost(
                hostState = snackbarHostState
            ) { data ->
                CustomSnackbar(message = data.visuals.message)
            }
        }
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

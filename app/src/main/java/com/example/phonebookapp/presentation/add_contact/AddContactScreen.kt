package com.example.phonebookapp.presentation.add_contact

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phonebookapp.presentation.ui.success.SuccessScreen
import com.example.phonebookapp.presentation.ui.common.IosPhoneTextField
import com.example.phonebookapp.presentation.ui.common.IosTextField
import com.example.phonebookapp.presentation.ui.common.LocalScreenDimensions
import com.example.phonebookapp.ui.theme.*
import com.example.phonebookapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    viewModel: AddContactViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state.value
    val context = LocalContext.current
    val screenWidth = LocalScreenDimensions.current.width
    var showPhotoOptions by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                viewModel.onEvent(AddContactEvent.ImageSelected(bytes))
            }
        }
    }
    
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            showSuccess = true
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.onEvent(AddContactEvent.DismissError)
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

            

            // Profile Photo - Vector Asset
            Image(
                painter = painterResource(id = R.drawable.person),
                contentDescription = "Add Photo",
                modifier = Modifier
                    .size(screenWidth * 0.4f)
                    .padding(start = 16.dp, top = 40.dp, end = 16.dp)
                    .clickable { showPhotoOptions = true }
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Add Photo",
                fontSize = 18.sp,
                color = IosBlue,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.3).sp,
                modifier = Modifier.clickable { showPhotoOptions = true }
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

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

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

    if (showPhotoOptions) {
        ModalBottomSheet(onDismissRequest = { showPhotoOptions = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Add Photo", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { showPhotoOptions = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) { Text("📷  Camera") }
                }

                TextButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                        showPhotoOptions = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) { Text("🖼️  Gallery") }
                }

                TextButton(onClick = { showPhotoOptions = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
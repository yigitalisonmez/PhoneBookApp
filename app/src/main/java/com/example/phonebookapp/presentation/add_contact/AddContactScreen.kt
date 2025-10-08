package com.example.phonebookapp.presentation.add_contact

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phonebookapp.presentation.ui.success.SuccessScreen
import com.example.phonebookapp.presentation.ui.common.IosPhoneTextField
import com.example.phonebookapp.presentation.ui.common.IosTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    viewModel: AddContactViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state.value
    val context = LocalContext.current
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { it != SheetValue.PartiallyExpanded })
    LaunchedEffect(Unit) { sheetState.expand() }

    ModalBottomSheet(onDismissRequest = onNavigateBack, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateBack) { 
                    Text("Cancel", color = Color(0xFF0075FF)) 
                }
                Text(text = "New Contact", style = MaterialTheme.typography.titleMedium)
                TextButton(
                    onClick = { viewModel.onEvent(AddContactEvent.SaveContact) },
                    enabled = !state.isLoading &&
                            state.firstName.isNotBlank() &&
                            state.lastName.isNotBlank() &&
                            state.phoneNumber.isNotBlank()
                ) { 
                    Text("Done", color = Color(0xFF0075FF)) 
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profile Photo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.profileImageUrl != null)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { showPhotoOptions = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.profileImageUrl != null) {
                        state.firstName.firstOrNull()?.uppercase() ?: "👤"
                    } else "👤",
                    fontSize = 60.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { showPhotoOptions = true }) {
                Text("Add Photo")
            }

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
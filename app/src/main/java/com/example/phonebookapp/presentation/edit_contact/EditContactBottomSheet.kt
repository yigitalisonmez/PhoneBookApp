package com.example.phonebookapp.presentation.edit_contact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phonebookapp.presentation.ui.common.IosPhoneTextField
import com.example.phonebookapp.presentation.ui.common.IosTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactBottomSheet(
    contactId: String,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onRequestDelete: (String) -> Unit,
    viewModel: EditContactViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    LaunchedEffect(contactId) { viewModel.initialize(contactId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onSaved() }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { it != SheetValue.PartiallyExpanded })
    LaunchedEffect(Unit) { sheetState.expand() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var menuExpanded by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            viewModel.onEvent(EditContactEvent.Save)
                        },
                        trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onRequestDelete(contactId)
                        },
                        trailingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val context = LocalContext.current
            val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
            ) { uri ->
                uri?.let {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        viewModel.onEvent(EditContactEvent.ImageSelected(bytes))
                    }
                }
            }

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
                    .clickable { galleryLauncher.launch("image/*") },
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
            TextButton(onClick = { galleryLauncher.launch("image/*") }) { Text("Change Photo") }

            IosTextField(
                value = state.firstName,
                onValueChange = { viewModel.onEvent(EditContactEvent.FirstNameChanged(it)) },
                label = "First Name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            IosTextField(
                value = state.lastName,
                onValueChange = { viewModel.onEvent(EditContactEvent.LastNameChanged(it)) },
                label = "Last Name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            IosPhoneTextField(
                value = state.phoneNumber,
                onValueChange = { viewModel.onEvent(EditContactEvent.PhoneNumberChanged(it)) },
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


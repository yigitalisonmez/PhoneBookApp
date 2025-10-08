package com.example.phonebookapp.presentation.edit_contact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    viewModel: EditContactViewModel = hiltViewModel(),
    contactId: String,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state.value

    LaunchedEffect(contactId) { viewModel.initialize(contactId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }

    val canSave = !state.isLoading &&
            state.firstName.isNotBlank() &&
            state.lastName.isNotBlank() &&
            state.phoneNumber.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Contact") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Cancel") }
                },
                actions = {
                    TextButton(onClick = { viewModel.onEvent(EditContactEvent.Save) }, enabled = canSave) {
                        Text("Done")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

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
                        .clickable { /* open image picker later */ },
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
                TextButton(onClick = { /* open image picker later */ }) {
                    Text("Change Photo")
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.onEvent(EditContactEvent.FirstNameChanged(it)) },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.onEvent(EditContactEvent.LastNameChanged(it)) },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = { viewModel.onEvent(EditContactEvent.PhoneNumberChanged(it)) },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !state.isLoading
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error?.let { err ->
                SnackbarHost(hostState = remember { SnackbarHostState() }).also {
                    // Basic inline error handling: a transient snackbar approach can be added
                }
            }
        }
    }
}

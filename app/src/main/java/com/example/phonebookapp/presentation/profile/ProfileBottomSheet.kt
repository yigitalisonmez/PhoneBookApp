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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phonebookapp.presentation.ui.common.IosTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBottomSheet(
    contactId: String,
    onDismiss: () -> Unit,
    onEdit: (String) -> Unit,
    onRequestDelete: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    LaunchedEffect(contactId) { viewModel.initialize(contactId) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(Unit) { sheetState.expand() }

    var showEdit by remember { mutableStateOf(false) }
    var showActionSheet by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss, 
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showActionSheet = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEDFAFF)),
                contentAlignment = Alignment.Center
            ) {
                val initial = state.firstName.firstOrNull()?.uppercase() ?: ""
                Text(
                    text = initial.toString(), 
                    fontSize = 48.sp, 
                    color = Color(0xFF0075FF), 
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Name
            Text(
                text = state.firstName + " " + state.lastName, 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D1D1F)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Contact Details - Separate TextFields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Custom iOS-style Action Sheet
    if (showActionSheet) {
        Dialog(
            onDismissRequest = { showActionSheet = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    // Edit Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                showActionSheet = false
                                showEdit = true 
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF0075FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Edit Contact",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF0075FF),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    HorizontalDivider(
                        color = Color(0xFFF0F0F0),
                        thickness = 0.5.dp
                    )
                    
                    // Delete Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                showActionSheet = false
                                onRequestDelete(state.id) 
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Delete Contact",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFF3B30),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showEdit) {
        com.example.phonebookapp.presentation.edit_contact.EditContactBottomSheet(
            contactId = state.id,
            onDismiss = { showEdit = false },
            onSaved = { showEdit = false },
            onRequestDelete = { id -> showEdit = false; onRequestDelete(id) }
        )
    }
}


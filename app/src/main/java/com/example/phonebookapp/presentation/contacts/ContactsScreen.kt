package com.example.phonebookapp.presentation.contacts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.phonebookapp.domain.model.Contact
import com.example.phonebookapp.presentation.profile.ProfileBottomSheet
import com.example.phonebookapp.presentation.ui.components.ContactsList
import com.example.phonebookapp.presentation.ui.components.ContactRowItem
import com.example.phonebookapp.presentation.ui.success.SuccessScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
    onNavigateToAddContact: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit = onNavigateToProfile
) {
    val state = viewModel.state.value
    val lifecycleOwner = LocalLifecycleOwner.current
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var profileId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Ekran ön plana geldi, listeyi yenile
                viewModel.onEvent(ContactsEvent.Refresh)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF6F6F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Contacts",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToAddContact) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0075FF)),
                            contentAlignment = Alignment.Center
                        ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Contact",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                        )
                    }
                }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F6F6))
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF6F6F6))
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(ContactsEvent.SearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Error message
            else if (state.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "❌",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.onEvent(ContactsEvent.Refresh) }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            }
            // Empty state
            else if (state.contacts.isEmpty()) {
                EmptyContactsState(onCreateContact = onNavigateToAddContact)
            }
            // Contact list
            else {
                ContactsList(
                    contacts = state.contacts,
                    onContactClick = { id -> profileId = id },
                    onDeleteClick = { id -> pendingDeleteId = id },
                    onEditClick = { id -> profileId = id }
                )
            }
        }

        if (pendingDeleteId != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteId = null },
                title = { Text("Emin misin?") },
                text = { Text("Bu kişiyi silmek istediğine emin misin?") },
                confirmButton = {
                    TextButton(onClick = {
                        pendingDeleteId?.let { viewModel.onEvent(ContactsEvent.DeleteContact(it)) }
                        pendingDeleteId = null
                    }) {
                        Text("Sil")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteId = null }) {
                        Text("İptal")
                    }
                }
            )
        }

        // Edit sheet artık Profile içinden açılıyor; burada kaldırıldı

        profileId?.let { pid ->
            ProfileBottomSheet(
                contactId = pid,
                onDismiss = { profileId = null },
                onEdit = { id ->
                    // Edit artık ProfileBottomSheet içinde hallediliyor
                },
                onRequestDelete = { id ->
                    profileId = null
                    pendingDeleteId = id
                }
            )
        }
        
        if (showSuccess) {
            SuccessScreen(
                onDismiss = { showSuccess = false },
                title = "All Done!",
                subtitle = "Contact updated",
                emoji = "🎉"
            )
        }
    }
}

@Composable
fun EmptyContactsState(onCreateContact: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👤",
                fontSize = 70.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No Contacts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Contacts you've added will appear here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onCreateContact,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Create New Contact",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}






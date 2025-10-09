package com.example.phonebookapp.presentation.contacts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.phonebookapp.domain.model.Contact
import com.example.phonebookapp.presentation.profile.ProfileBottomSheet
import com.example.phonebookapp.presentation.ui.components.ContactsList
import com.example.phonebookapp.presentation.ui.components.ContactRowItem
import com.example.phonebookapp.presentation.ui.components.CustomSnackbar
import com.example.phonebookapp.presentation.ui.components.EmptyContactsState
import com.example.phonebookapp.presentation.ui.components.SearchHistorySection
import com.example.phonebookapp.presentation.ui.components.TopNameMatchesSection
import com.example.phonebookapp.presentation.ui.success.SuccessScreen
import com.example.phonebookapp.ui.theme.*
import kotlinx.coroutines.launch

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
    val focusManager = LocalFocusManager.current
    var showSuccess by remember { mutableStateOf(false) }
    var profileId by remember { mutableStateOf<String?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    val searchInteractionSource = remember { MutableInteractionSource() }
    val isSearchFocused by searchInteractionSource.collectIsFocusedAsState()
    
    // Snackbar için
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    // Delete işlemi sonrası snackbar göster
    var previousContactCount by remember { mutableStateOf(state.contacts.size) }
    
    LaunchedEffect(state.contacts.size) {
        if (previousContactCount > state.contacts.size) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "User is deleted!",
                    duration = SnackbarDuration.Short
                )
            }
        }
        previousContactCount = state.contacts.size
    }

    Scaffold(
        containerColor = IosBackground,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 16.dp)
            ) { data ->
                CustomSnackbar(message = data.visuals.message)
            }
        },
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
                                .background(IosBlue),
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IosBackground)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IosBackground)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    // Search bar'dan focus'u kaldır
                    focusManager.clearFocus()
                }
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { query -> viewModel.onEvent(ContactsEvent.SearchQuery(query)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { 
                    Text(
                        "Search by name",
                        color = IosGrey
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = IosGrey
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                interactionSource = searchInteractionSource
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
            // Search History (sadece search bar focus olduğunda ve query boş olduğunda)
            if (isSearchFocused && state.searchQuery.isBlank() && state.searchHistory.isNotEmpty()) {
                SearchHistorySection(
                    searchHistory = state.searchHistory,
                    onHistoryItemClick = { query ->
                        viewModel.onEvent(ContactsEvent.SearchQuery(query))
                    },
                    onRemoveHistoryItem = { query ->
                        viewModel.onEvent(ContactsEvent.RemoveFromSearchHistory(query))
                    },
                    onClearHistory = {
                        viewModel.onEvent(ContactsEvent.ClearSearchHistory)
                    }
                )
            }
            // Search Results (arama yapıldığında)
            else if (state.searchQuery.isNotBlank() && state.contacts.isNotEmpty()) {
                TopNameMatchesSection(
                    contacts = state.contacts,
                    searchQuery = state.searchQuery,
                    onContactClick = { id -> profileId = id }
                )
            }
            // Contact list (normal durum)
            else {
                ContactsList(
                    contacts = state.contacts,
                    onContactClick = { id -> 
                        profileId = id
                        isEditMode = false
                    },
                    onDeleteClick = { id -> viewModel.onEvent(ContactsEvent.DeleteContact(id)) },
                    onEditClick = { id -> 
                        profileId = id
                        isEditMode = true
                    }
                )
            }
        }

        // AlertDialog kaldırıldı - artık sadece ContactRowItem'daki DeleteContactBottomSheet kullanılıyor


        profileId?.let { pid ->
            ProfileBottomSheet(
                contactId = pid,
                onDismiss = { 
                    profileId = null
                    isEditMode = false
                },
                onEdit = { id ->
                    // Edit işlemi sonrası snackbar göster
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "User is updated!",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onRequestDelete = { id ->
                    profileId = null
                    isEditMode = false
                    viewModel.onEvent(ContactsEvent.DeleteContact(id))
                },
                onContactUpdated = {
                    // Contact güncellendiğinde listeyi yenile
                    viewModel.onEvent(ContactsEvent.Refresh)
                },
                initialEditMode = isEditMode
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

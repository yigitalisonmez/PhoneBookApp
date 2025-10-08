package com.example.phonebookapp.presentation.contacts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
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
import com.example.phonebookapp.R
import com.example.phonebookapp.ui.theme.*

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
        containerColor = IosBackground,
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
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(ContactsEvent.SearchQuery(it)) },
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
                    onDeleteClick = { id -> viewModel.onEvent(ContactsEvent.DeleteContact(id)) },
                    onEditClick = { id -> profileId = id }
                )
            }
        }

        // AlertDialog kaldırıldı - artık sadece ContactRowItem'daki DeleteContactBottomSheet kullanılıyor


        profileId?.let { pid ->
            ProfileBottomSheet(
                contactId = pid,
                onDismiss = { profileId = null },
                onEdit = { id ->
                },
                onRequestDelete = { id ->
                    profileId = null
                    viewModel.onEvent(ContactsEvent.DeleteContact(id))
                },
                onContactUpdated = {
                    // Contact güncellendiğinde listeyi yenile
                    viewModel.onEvent(ContactsEvent.Refresh)
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
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    

    val topBarHeight = with(density) { 152.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    val offsetY = -(topBarHeight / 2) / density.density
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = offsetY.dp),  // Dinamik offset
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vector icon (SVG'den dönüştürülmüş) - Yukarıda
            Image(
                painter = painterResource(id = R.drawable.person),
                contentDescription = "No Contacts",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // "No Contacts" başlığı - Yukarıda
            Text(
                text = "No Contacts",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosDarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Açıklama metni - TAM ORTADA (referans nokta)
            Text(
                text = "Contacts you've added will appear here.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // "Create New Contact" butonu - Altta
            TextButton(
                onClick = onCreateContact,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Create New Contact",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IosBlue
                )
            }
        }
    }
}






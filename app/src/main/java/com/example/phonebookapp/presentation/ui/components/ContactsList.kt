package com.example.phonebookapp.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.phonebookapp.domain.model.Contact

@Composable
fun ContactsList(
    contacts: List<Contact>,
    onContactClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onEditClick: (String) -> Unit
) {
    // Global state: Sadece bir contact swipe edilebilir
    var activeSwipedContactId by remember { mutableStateOf<String?>(null) }
    
    val groupedContacts = contacts
        .sortedBy { it.firstName.lowercase() }
        .groupBy { it.firstName.first().uppercaseChar() }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        groupedContacts.forEach { (initial, contactsForLetter) ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp ,vertical=8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = initial.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        Divider(color = Color(0xFFF6F7FA))

                        contactsForLetter.forEachIndexed { index, contact ->
                            ContactRowItem(
                                contact = contact,
                                onClick = { onContactClick(contact.id) },
                                onEdit = { onEditClick(contact.id) },
                                onDelete = { onDeleteClick(contact.id) },
                                isActive = activeSwipedContactId == contact.id,
                                onSwipeStart = { activeSwipedContactId = contact.id },
                                onSwipeEnd = { 
                                    if (activeSwipedContactId == contact.id) {
                                        activeSwipedContactId = null
                                    }
                                }
                            )
                            if (index != contactsForLetter.lastIndex) {
                                Divider(color = Color(0xFFF6F7FA))
                            }
                        }
                    }
                }
            }
        }
    }
}



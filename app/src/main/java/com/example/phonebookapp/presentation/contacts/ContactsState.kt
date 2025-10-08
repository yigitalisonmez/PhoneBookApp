package com.example.phonebookapp.presentation.contacts

import com.example.phonebookapp.domain.model.Contact

data class ContactsState(
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)
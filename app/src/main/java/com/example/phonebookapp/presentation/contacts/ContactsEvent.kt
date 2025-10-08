package com.example.phonebookapp.presentation.contacts

sealed class ContactsEvent {
    object LoadContacts : ContactsEvent()
    data class SearchQuery(val query: String) : ContactsEvent()
    data class DeleteContact(val id: String) : ContactsEvent()
    object Refresh : ContactsEvent()
}
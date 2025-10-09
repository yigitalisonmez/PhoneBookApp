package com.example.phonebookapp.presentation.contacts

sealed class ContactsEvent {
    object LoadContacts : ContactsEvent()
    data class SearchQuery(val query: String) : ContactsEvent()
    data class DeleteContact(val id: String) : ContactsEvent()
    object Refresh : ContactsEvent()
    data class AddToSearchHistory(val query: String) : ContactsEvent()
    data class RemoveFromSearchHistory(val query: String) : ContactsEvent()
    object ClearSearchHistory : ContactsEvent()
    object LoadSearchHistory : ContactsEvent()
}
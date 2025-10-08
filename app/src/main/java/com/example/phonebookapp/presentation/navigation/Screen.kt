package com.example.phonebookapp.presentation.navigation

sealed class Screen(val route: String) {
    object Contacts : Screen("contacts")
    object AddContact : Screen("add_contact")
    object Profile : Screen("profile/{contactId}") {
        fun createRoute(contactId: String) = "profile/$contactId"
    }
    object EditContact : Screen("edit_contact/{contactId}") {
        fun createRoute(contactId: String) = "edit_contact/$contactId"
    }
}
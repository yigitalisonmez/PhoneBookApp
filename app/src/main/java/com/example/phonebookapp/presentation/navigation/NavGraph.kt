package com.example.phonebookapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.phonebookapp.presentation.add_contact.AddContactScreen
import com.example.phonebookapp.presentation.contacts.ContactsScreen
import com.example.phonebookapp.presentation.edit_contact.EditContactScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Contacts.route
    ) {
        composable(Screen.Contacts.route) { backStackEntry ->
            ContactsScreen(
                onNavigateToAddContact = {
                    navController.navigate(Screen.AddContact.route)
                },
                onNavigateToProfile = { contactId ->
                    navController.navigate(Screen.Profile.createRoute(contactId))
                },
                onNavigateToEdit = { contactId ->
                    navController.navigate(Screen.EditContact.createRoute(contactId))
                }
            )
        }

        composable(Screen.AddContact.route) {
            AddContactScreen(
                onNavigateBack = {  // Returns unit
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditContact.route) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: return@composable
            EditContactScreen(
                contactId = contactId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
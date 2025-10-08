package com.example.phonebookapp.presentation.add_contact

data class AddContactState(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
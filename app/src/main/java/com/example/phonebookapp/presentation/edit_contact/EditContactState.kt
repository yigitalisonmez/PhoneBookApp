package com.example.phonebookapp.presentation.edit_contact

data class EditContactState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String? = null
)

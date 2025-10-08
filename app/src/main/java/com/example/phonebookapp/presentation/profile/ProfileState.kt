package com.example.phonebookapp.presentation.profile

data class ProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val imageUrl: String? = null
)



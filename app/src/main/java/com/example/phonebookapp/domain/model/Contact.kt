package com.example.phonebookapp.domain.model

data class Contact(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val imageUrl: String? = null,
    val isInDeviceContacts: Boolean = false
) {
    val fullName: String
        get() = "$firstName $lastName"

    val initials: String
        get() = "${firstName.firstOrNull()?.uppercase() ?: ""}${lastName.firstOrNull()?.uppercase() ?: ""}"
}
package com.example.phonebookapp.data.remote.dto

import com.example.phonebookapp.domain.model.Contact

fun ContactDto.toDomain(): Contact {
    return Contact(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        imageUrl = profileImageUrl,
        isInDeviceContacts = false
    )
}

fun Contact.toDto(): ContactDto {
    return ContactDto(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        profileImageUrl = imageUrl
    )
}
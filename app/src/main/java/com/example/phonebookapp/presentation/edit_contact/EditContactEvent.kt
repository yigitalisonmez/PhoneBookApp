package com.example.phonebookapp.presentation.edit_contact

sealed class EditContactEvent {
    object Load : EditContactEvent()
    data class FirstNameChanged(val value: String) : EditContactEvent()
    data class LastNameChanged(val value: String) : EditContactEvent()
    data class PhoneNumberChanged(val value: String) : EditContactEvent()
    data class ImageSelected(val imageBytes: ByteArray) : EditContactEvent()
    object Save : EditContactEvent()
    object DismissError : EditContactEvent()
}

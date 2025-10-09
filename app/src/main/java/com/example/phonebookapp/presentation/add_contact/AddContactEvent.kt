package com.example.phonebookapp.presentation.add_contact

sealed class AddContactEvent {
    data class FirstNameChanged(val value: String) : AddContactEvent()
    data class LastNameChanged(val value: String) : AddContactEvent()
    data class PhoneNumberChanged(val value: String) : AddContactEvent()
    data class ImageSelected(val imageBytes: ByteArray) : AddContactEvent() // Geçici olarak sakla
    object SaveContact : AddContactEvent() // Done'a basınca hem contact'ı hem fotoğrafı kaydet
    object DismissError : AddContactEvent()
}
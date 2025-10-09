package com.example.phonebookapp.presentation.add_contact

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phonebookapp.domain.repository.ContactsRepository
import com.example.phonebookapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val repository: ContactsRepository
) : ViewModel() {

    private val _state = mutableStateOf(AddContactState())
    val state: State<AddContactState> = _state

    fun onEvent(event: AddContactEvent) {
        when (event) {
            is AddContactEvent.FirstNameChanged -> {
                _state.value = _state.value.copy(firstName = event.value)
            }
            is AddContactEvent.LastNameChanged -> {
                _state.value = _state.value.copy(lastName = event.value)
            }
            is AddContactEvent.PhoneNumberChanged -> {
                _state.value = _state.value.copy(phoneNumber = event.value)
            }
            is AddContactEvent.ImageSelected -> {
                uploadImage(event.imageBytes)
            }
            is AddContactEvent.SaveContact -> {
                saveContact()
            }
            is AddContactEvent.DismissError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun uploadImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // API'ye image upload et
            when (val result = repository.uploadImage(imageBytes)) {
                is Resource.Success -> {
                    // Başarılı, URL'i state'e kaydet
                    _state.value = _state.value.copy(
                        profileImageUrl = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = "Fotoğraf yüklenemedi: ${result.message}",
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }

    private fun saveContact() {
        // Validasyon - Last name zorunlu değil
        if (_state.value.firstName.isBlank()) {
            _state.value = _state.value.copy(error = "İsim boş olamaz")
            return
        }

        if (_state.value.phoneNumber.isBlank()) {
            _state.value = _state.value.copy(error = "Telefon numarası boş olamaz")
            return
        }

        // API'ye POST isteği
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val result = repository.createContact(
                firstName = _state.value.firstName.trim(),
                lastName = _state.value.lastName.trim(), // Boş string olabilir
                phoneNumber = _state.value.phoneNumber.trim(),
                imageUrl = _state.value.profileImageUrl
            )

            when (result) {
                is Resource.Success -> {
                    // Başarılı! Ekranı kapat
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = "Kayıt başarısız: ${result.message}",
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }
}
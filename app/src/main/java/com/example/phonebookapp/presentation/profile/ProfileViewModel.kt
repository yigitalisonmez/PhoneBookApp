package com.example.phonebookapp.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phonebookapp.domain.repository.ContactsRepository
import com.example.phonebookapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ContactsRepository
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    private var loadedId: String? = null

    fun initialize(id: String, initialEditMode: Boolean = false) {
        if (loadedId == id) {
            // Aynı contact, sadece edit mode'u ayarla
            if (initialEditMode && !_state.value.isEditMode) {
                enterEditMode()
            }
            return
        }
        loadedId = id
        load(id, initialEditMode)
    }

    private fun load(id: String, initialEditMode: Boolean = false) {
        repository.getContactById(id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val c = result.data
                    if (c != null) {
                        _state.value = ProfileState(
                            isLoading = false,
                            error = null,
                            id = c.id,
                            firstName = c.firstName,
                            lastName = c.lastName,
                            phoneNumber = c.phoneNumber,
                            imageUrl = c.imageUrl,
                            isEditMode = initialEditMode,
                            editFirstName = c.firstName,
                            editLastName = c.lastName,
                            editPhoneNumber = c.phoneNumber,
                            editImageUrl = c.imageUrl ?: ""
                        )
                    }
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun enterEditMode() {
        _state.value = _state.value.copy(
            isEditMode = true,
            editFirstName = _state.value.firstName,
            editLastName = _state.value.lastName,
            editPhoneNumber = _state.value.phoneNumber,
            editImageUrl = _state.value.imageUrl ?: ""
        )
    }

    fun exitEditMode() {
        _state.value = _state.value.copy(isEditMode = false)
    }

    fun updateEditFirstName(firstName: String) {
        _state.value = _state.value.copy(editFirstName = firstName)
    }

    fun updateEditLastName(lastName: String) {
        _state.value = _state.value.copy(editLastName = lastName)
    }

    fun updateEditPhoneNumber(phoneNumber: String) {
        _state.value = _state.value.copy(editPhoneNumber = phoneNumber)
    }

    fun updateEditImageUrl(imageUrl: String) {
        _state.value = _state.value.copy(editImageUrl = imageUrl)
    }

    fun saveContact(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val result = repository.updateContact(
                id = _state.value.id,
                firstName = _state.value.editFirstName,
                lastName = _state.value.editLastName,
                phoneNumber = _state.value.editPhoneNumber,
                imageUrl = _state.value.editImageUrl.ifEmpty { null }
            )
            
            when (result) {
                is Resource.Success -> {
                    val updatedContact = result.data
                    if (updatedContact != null) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isEditMode = false,
                            firstName = updatedContact.firstName,
                            lastName = updatedContact.lastName,
                            phoneNumber = updatedContact.phoneNumber,
                            imageUrl = updatedContact.imageUrl
                        )
                        onSuccess()
                    }
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun uploadProfileImage(imageBytes: ByteArray, onSuccess: () -> Unit = {}) {
        Log.d("ProfileViewModel", "uploadProfileImage called with ${imageBytes.size} bytes")
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            Log.d("ProfileViewModel", "Starting image upload, setting loading state to true")
            
            when (val result = repository.uploadImage(imageBytes)) {
                is Resource.Success -> {
                    Log.d("ProfileViewModel", "Image upload successful, received URL: ${result.data}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        editImageUrl = result.data ?: ""
                    )
                    onSuccess() // Contacts listesini yenile
                }
                is Resource.Error -> {
                    Log.e("ProfileViewModel", "Image upload failed: ${result.message}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    Log.d("ProfileViewModel", "Image upload in progress")
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }
}



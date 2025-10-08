package com.example.phonebookapp.presentation.edit_contact

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

@HiltViewModel
class EditContactViewModel @Inject constructor(
    private val repository: ContactsRepository
) : ViewModel() {

    private val _state = mutableStateOf(EditContactState())
    val state: State<EditContactState> = _state

    private var contactId: String? = null

    fun initialize(id: String) {
        if (contactId == id) return
        contactId = id
        onEvent(EditContactEvent.Load)
    }

    fun onEvent(event: EditContactEvent) {
        when (event) {
            is EditContactEvent.Load -> load()
            is EditContactEvent.FirstNameChanged -> _state.value = _state.value.copy(firstName = event.value)
            is EditContactEvent.LastNameChanged -> _state.value = _state.value.copy(lastName = event.value)
            is EditContactEvent.PhoneNumberChanged -> _state.value = _state.value.copy(phoneNumber = event.value)
            is EditContactEvent.ImageSelected -> uploadImage(event.imageBytes)
            is EditContactEvent.Save -> save()
            is EditContactEvent.DismissError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun load() {
        val id = contactId ?: return
        repository.getContactById(id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val c = result.data
                    if (c != null) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = null,
                            firstName = c.firstName,
                            lastName = c.lastName,
                            phoneNumber = c.phoneNumber,
                            profileImageUrl = c.imageUrl
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

    private fun save() {
        val id = contactId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val res = repository.updateContact(
                id = id,
                firstName = _state.value.firstName,
                lastName = _state.value.lastName,
                phoneNumber = _state.value.phoneNumber,
                imageUrl = _state.value.profileImageUrl
            )) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isSaved = true, error = null)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                else -> {}
            }
        }
    }

    private fun uploadImage(bytes: ByteArray) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val res = repository.uploadImage(bytes)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(profileImageUrl = res.data, isLoading = false)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = res.message, isLoading = false)
                }
                else -> {}
            }
        }
    }
}

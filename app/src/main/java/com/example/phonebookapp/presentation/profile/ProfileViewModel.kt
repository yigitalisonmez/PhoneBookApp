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
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ContactsRepository
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    private var loadedId: String? = null

    fun initialize(id: String) {
        if (loadedId == id) return
        loadedId = id
        load(id)
    }

    private fun load(id: String) {
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
                            imageUrl = c.imageUrl
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
}



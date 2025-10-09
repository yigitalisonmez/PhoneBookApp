package com.example.phonebookapp.presentation.contacts

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phonebookapp.data.local.SearchHistoryManager
import com.example.phonebookapp.domain.repository.ContactsRepository
import com.example.phonebookapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val repository: ContactsRepository,
    private val searchHistoryManager: SearchHistoryManager
) : ViewModel() {

    private val _state = mutableStateOf(ContactsState())
    val state: State<ContactsState> = _state

    private var searchJob: Job? = null

    init {
        loadContacts()
        loadSearchHistory()
    }

    fun onEvent(event: ContactsEvent) {
        when (event) {
            is ContactsEvent.LoadContacts -> {
                loadContacts()
            }
            is ContactsEvent.SearchQuery -> {
                _state.value = _state.value.copy(searchQuery = event.query)
                searchContacts(event.query)
            }
            is ContactsEvent.DeleteContact -> {
                deleteContact(event.id)
            }
            is ContactsEvent.Refresh -> {
                loadContacts()
            }
            is ContactsEvent.AddToSearchHistory -> {
                addToSearchHistory(event.query)
            }
            is ContactsEvent.RemoveFromSearchHistory -> {
                removeFromSearchHistory(event.query)
            }
            is ContactsEvent.ClearSearchHistory -> {
                clearSearchHistory()
            }
            is ContactsEvent.LoadSearchHistory -> {
                loadSearchHistory()
            }
        }
    }

    private fun loadContacts() {
        repository.getAllContacts().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        contacts = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun searchContacts(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce

            if (query.isBlank()) {
                loadContacts()
                return@launch
            }

            // Search history'ye ekle
            addToSearchHistory(query)

            repository.searchContacts(query).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            contacts = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun deleteContact(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            when (val result = repository.deleteContact(id)) {
                is Resource.Success -> {
                    // Silme başarılı, listeyi yenile
                    loadContacts()
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }

    private fun loadSearchHistory() {
        val history = searchHistoryManager.getSearchHistory()
        _state.value = _state.value.copy(searchHistory = history)
    }

    private fun addToSearchHistory(query: String) {
        searchHistoryManager.addSearchQuery(query)
        loadSearchHistory()
    }

    private fun removeFromSearchHistory(query: String) {
        searchHistoryManager.removeSearchQuery(query)
        loadSearchHistory()
    }

    private fun clearSearchHistory() {
        searchHistoryManager.clearSearchHistory()
        loadSearchHistory()
    }
}
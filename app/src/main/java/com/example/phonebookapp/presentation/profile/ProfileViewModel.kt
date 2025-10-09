package com.example.phonebookapp.presentation.profile

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phonebookapp.domain.model.Contact
import com.example.phonebookapp.domain.repository.ContactsRepository
import com.example.phonebookapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ContactsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    private var loadedId: String? = null
    
    private val sharedPreferences by lazy {
        context.getSharedPreferences("phone_contacts", Context.MODE_PRIVATE)
    }

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
                        val isSaved = sharedPreferences.getBoolean("saved_${c.id}", false)
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
                            editImageUrl = c.imageUrl ?: "",
                            isSavedToPhone = isSaved
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
                    val newImageUrl = result.data ?: ""
                    _state.value = _state.value.copy(
                        isLoading = false,
                        imageUrl = newImageUrl, // Normal mode için
                        editImageUrl = newImageUrl // Edit mode için
                    )
                    
                    // Repository'deki contact'ı da güncelle
                    updateContactInRepository(newImageUrl)
                    
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
    
    fun saveToPhoneContact() {
        viewModelScope.launch {
            try {
                val state = _state.value
                
                // Check if already saved
                if (state.isSavedToPhone) {
                    Log.d("ProfileViewModel", "Contact already saved to phone")
                    return@launch
                }
                
                // Create contact operations
                val operations = ArrayList<ContentProviderOperation>()
                
                // Insert raw contact
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build()
                )
                
                // Insert name
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, state.firstName)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, state.lastName)
                        .build()
                )
                
                // Insert phone number
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, state.phoneNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build()
                )
                
                // Apply operations
                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
                
                // Save to SharedPreferences
                sharedPreferences.edit().putBoolean("saved_${state.id}", true).apply()
                
                // Update state
                _state.value = _state.value.copy(isSavedToPhone = true)
                
                Log.d("ProfileViewModel", "Contact saved to phone successfully")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving contact to phone", e)
                _state.value = _state.value.copy(error = "Failed to save contact: ${e.message}")
            }
        }
    }
    
    fun removeFromPhoneContact() {
        viewModelScope.launch {
            try {
                // Remove from SharedPreferences
                sharedPreferences.edit().remove("saved_${_state.value.id}").apply()
                
                // Update state
                _state.value = _state.value.copy(isSavedToPhone = false)
                
                Log.d("ProfileViewModel", "Contact removed from phone")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error removing contact from phone", e)
            }
        }
    }
    
    private fun updateContactInRepository(newImageUrl: String) {
        viewModelScope.launch {
            val currentState = _state.value
            repository.updateContact(
                id = currentState.id,
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                phoneNumber = currentState.phoneNumber,
                imageUrl = newImageUrl
            )
        }
    }
}



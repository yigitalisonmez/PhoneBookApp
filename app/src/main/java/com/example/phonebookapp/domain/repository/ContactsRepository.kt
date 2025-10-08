package com.example.phonebookapp.domain.repository

import com.example.phonebookapp.domain.model.Contact
import com.example.phonebookapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {

    fun getAllContacts(): Flow<Resource<List<Contact>>>

    fun getContactById(id: String): Flow<Resource<Contact>>

    suspend fun createContact(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        imageUrl: String?
    ): Resource<Contact>

    suspend fun updateContact(
        id: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        imageUrl: String?
    ): Resource<Contact>

    suspend fun deleteContact(id: String): Resource<Unit>

    suspend fun uploadImage(imageBytes: ByteArray): Resource<String>

    fun searchContacts(query: String): Flow<Resource<List<Contact>>>
}
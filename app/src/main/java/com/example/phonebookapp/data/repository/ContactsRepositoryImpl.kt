package com.example.phonebookapp.data.repository

import com.example.phonebookapp.data.remote.api.ContactsApi
import com.example.phonebookapp.data.remote.dto.CreateContactRequest
import com.example.phonebookapp.data.remote.dto.UpdateContactRequest
import com.example.phonebookapp.data.remote.dto.toDomain
import com.example.phonebookapp.domain.model.Contact
import com.example.phonebookapp.domain.repository.ContactsRepository
import com.example.phonebookapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import android.util.Log

class ContactsRepositoryImpl @Inject constructor(
    private val api: ContactsApi
) : ContactsRepository {

    override fun getAllContacts(): Flow<Resource<List<Contact>>> = flow {
        emit(Resource.Loading())

        try {
            val response = api.getAllContacts()

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true) {
                    val contacts = apiResponse.data?.users?.map { it.toDomain() } ?: emptyList()
                    emit(Resource.Success(contacts))
                } else {
                    val errorMsg = apiResponse?.messages?.joinToString(", ") ?: "Bilinmeyen hata"
                    emit(Resource.Error(errorMsg))
                }
            } else {
                emit(Resource.Error("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Sunucu hatası: ${e.localizedMessage}"))
        } catch (e: IOException) {
            emit(Resource.Error("İnternet bağlantısı hatası"))
        } catch (e: Exception) {
            emit(Resource.Error("Beklenmeyen hata: ${e.localizedMessage}"))
        }
    }

    override fun getContactById(id: String): Flow<Resource<Contact>> = flow {
        emit(Resource.Loading())

        try {
            val response = api.getContactById(id)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data.toDomain()))
                } else {
                    emit(Resource.Error("Kişi bulunamadı"))
                }
            } else {
                emit(Resource.Error("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Hata: ${e.localizedMessage}"))
        }
    }

    override suspend fun createContact(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        imageUrl: String?
    ): Resource<Contact> {
        return try {
            val request = CreateContactRequest(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                profileImageUrl = imageUrl
            )

            val response = api.createContact(request)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true && apiResponse.data != null) {
                    Resource.Success(apiResponse.data.toDomain())
                } else {
                    Resource.Error("Kişi oluşturulamadı")
                }
            } else {
                Resource.Error("HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Hata: ${e.localizedMessage}")
        }
    }

    override suspend fun updateContact(
        id: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        imageUrl: String?
    ): Resource<Contact> {
        return try {
            val request = UpdateContactRequest(
                id = id,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                profileImageUrl = imageUrl
            )

            val response = api.updateContact(id, request)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true && apiResponse.data != null) {
                    Resource.Success(apiResponse.data.toDomain())
                } else {
                    Resource.Error("Güncelleme başarısız")
                }
            } else {
                Resource.Error("HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Hata: ${e.localizedMessage}")
        }
    }

    override suspend fun deleteContact(id: String): Resource<Unit> {
        return try {
            val response = api.deleteContact(id)

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Silme başarısız")
            }
        } catch (e: Exception) {
            Resource.Error("Hata: ${e.localizedMessage}")
        }
    }

    override suspend fun uploadImage(imageBytes: ByteArray): Resource<String> {
        Log.d("ContactsRepository", "uploadImage called with ${imageBytes.size} bytes")
        return try {
            val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", "contact_image.jpg", requestBody)
            Log.d("ContactsRepository", "Created multipart body, making API call")

            val response = api.uploadImage(part)
            Log.d("ContactsRepository", "API response received: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d("ContactsRepository", "API response body: $apiResponse")

                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d("ContactsRepository", "Image upload successful, URL: ${apiResponse.data.imageUrl}")
                    Resource.Success(apiResponse.data.imageUrl ?: "")
                } else {
                    Log.e("ContactsRepository", "Image upload failed: ${apiResponse?.messages}")
                    Resource.Error("Görsel yüklenemedi")
                }
            } else {
                Log.e("ContactsRepository", "HTTP error: ${response.code()} - ${response.message()}")
                Resource.Error("HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ContactsRepository", "Exception during image upload", e)
            Resource.Error("Hata: ${e.localizedMessage}")
        }
    }

    override fun searchContacts(query: String): Flow<Resource<List<Contact>>> = flow {
        emit(Resource.Loading())

        try {
            val response = api.getAllContacts()

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true) {
                    val allContacts = apiResponse.data?.users?.map { it.toDomain() } ?: emptyList()

                    val filteredContacts = if (query.isBlank()) {
                        allContacts
                    } else {
                        allContacts.filter { contact ->
                            contact.fullName.contains(query, ignoreCase = true) ||
                                    contact.phoneNumber.contains(query)
                        }
                    }

                    emit(Resource.Success(filteredContacts))
                } else {
                    emit(Resource.Error("Arama başarısız"))
                }
            } else {
                emit(Resource.Error("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Hata: ${e.localizedMessage}"))
        }
    }
}
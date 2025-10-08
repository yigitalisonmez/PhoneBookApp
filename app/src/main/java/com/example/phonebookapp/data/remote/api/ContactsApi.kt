package com.example.phonebookapp.data.remote.api

import com.example.phonebookapp.data.remote.dto.ApiResponse
import com.example.phonebookapp.data.remote.dto.ContactDto
import com.example.phonebookapp.data.remote.dto.CreateContactRequest
import com.example.phonebookapp.data.remote.dto.UpdateContactRequest
import com.example.phonebookapp.data.remote.dto.UserListData
import com.example.phonebookapp.data.remote.dto.ImageUploadData
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ContactsApi {

    @GET("api/User/GetAll")
    suspend fun getAllContacts(): Response<ApiResponse<UserListData>>  // ✅ UserListData!

    @GET("api/User/{id}")
    suspend fun getContactById(@Path("id") id: String): Response<ApiResponse<ContactDto>>

    @POST("api/User")
    suspend fun createContact(@Body request: CreateContactRequest): Response<ApiResponse<ContactDto>>

    @PUT("api/User/{id}")
    suspend fun updateContact(
        @Path("id") id: String,
        @Body request: UpdateContactRequest
    ): Response<ApiResponse<ContactDto>>

    @DELETE("api/User/{id}")
    suspend fun deleteContact(@Path("id") id: String): Response<ApiResponse<Unit>>

    @Multipart
    @POST("api/User/UploadImage")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<ApiResponse<ImageUploadData>>
}
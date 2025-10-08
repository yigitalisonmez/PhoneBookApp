package com.example.phonebookapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// Dış wrapper
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("messages")
    val messages: List<String>? = null,

    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("data")
    val data: T? = null
)

// GetAll için özel data yapısı
data class UserListData(
    @SerializedName("users")
    val users: List<ContactDto>? = null
)

// Image upload için özel data yapısı
data class ImageUploadData(
    @SerializedName("imageUrl")
    val imageUrl: String? = null
)
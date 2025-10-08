package com.example.phonebookapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ContactDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("profileImageUrl")  // ← imageUrl değil!
    val profileImageUrl: String? = null
)

// API Request Models
data class CreateContactRequest(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null
)

data class UpdateContactRequest(
    @SerializedName("id")
    val id: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null
)
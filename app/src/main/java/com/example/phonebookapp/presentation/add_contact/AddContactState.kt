package com.example.phonebookapp.presentation.add_contact

data class AddContactState(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String? = null,
    val tempImageBytes: ByteArray? = null, // Geçici olarak tutulan fotoğraf (Done'a basılmadan önce)
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddContactState

        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (phoneNumber != other.phoneNumber) return false
        if (profileImageUrl != other.profileImageUrl) return false
        if (tempImageBytes != null) {
            if (other.tempImageBytes == null) return false
            if (!tempImageBytes.contentEquals(other.tempImageBytes)) return false
        } else if (other.tempImageBytes != null) return false
        if (isLoading != other.isLoading) return false
        if (isUploadingImage != other.isUploadingImage) return false
        if (error != other.error) return false
        if (isSuccess != other.isSuccess) return false

        return true
    }

    override fun hashCode(): Int {
        var result = firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + (profileImageUrl?.hashCode() ?: 0)
        result = 31 * result + (tempImageBytes?.contentHashCode() ?: 0)
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isUploadingImage.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + isSuccess.hashCode()
        return result
    }
}
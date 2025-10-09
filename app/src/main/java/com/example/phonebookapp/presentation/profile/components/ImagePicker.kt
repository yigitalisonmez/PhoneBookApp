package com.example.phonebookapp.presentation.profile.components

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream

class ImagePickerState(
    val context: Context,
    private val onImageSelected: (ByteArray) -> Unit
) {
    var photoUri by mutableStateOf<Uri?>(null)
        private set

    fun createImageUri(): Uri {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.cacheDir
        val imageFile = java.io.File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    fun handleGalleryResult(uri: Uri?) {
        Log.d("ImagePicker", "Gallery picker result: $uri")
        uri?.let { selectedUri ->
            try {
                Log.d("ImagePicker", "Processing selected image: $selectedUri")
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                inputStream?.let { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    Log.d("ImagePicker", "Bitmap decoded successfully, size: ${bitmap.width}x${bitmap.height}")
                    
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val imageBytes = outputStream.toByteArray()
                    Log.d("ImagePicker", "Image converted to bytes, size: ${imageBytes.size} bytes")
                    
                    onImageSelected(imageBytes)
                } ?: run {
                    Log.e("ImagePicker", "Failed to open input stream for URI: $selectedUri")
                }
            } catch (e: Exception) {
                Log.e("ImagePicker", "Error processing selected image", e)
            }
        } ?: run {
            Log.d("ImagePicker", "No image selected by user")
        }
    }

    fun handleCameraResult(success: Boolean) {
        Log.d("ImagePicker", "Camera result: $success")
        if (success) {
            Log.d("ImagePicker", "Camera photo taken successfully")
            photoUri?.let { uri ->
                try {
                    Log.d("ImagePicker", "Processing camera photo from URI: $uri")
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.let { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        Log.d("ImagePicker", "Camera bitmap decoded: ${bitmap.width}x${bitmap.height}")
                        
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val imageBytes = outputStream.toByteArray()
                        Log.d("ImagePicker", "Camera image converted to bytes: ${imageBytes.size} bytes")
                        
                        onImageSelected(imageBytes)
                    } ?: run {
                        Log.e("ImagePicker", "Failed to open input stream for camera URI: $uri")
                    }
                } catch (e: Exception) {
                    Log.e("ImagePicker", "Error processing camera image", e)
                }
            } ?: run {
                Log.e("ImagePicker", "Photo URI is null, cannot process camera image")
            }
        } else {
            Log.d("ImagePicker", "Camera photo cancelled")
        }
    }

    fun launchCamera(cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>) {
        try {
            photoUri = createImageUri()
            Log.d("ImagePicker", "Created photo URI: $photoUri")
            cameraLauncher.launch(photoUri)
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error launching camera", e)
        }
    }

    fun checkAndRequestPermission(
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
        onPermissionGranted: () -> Unit
    ) {
        Log.d("ImagePicker", "checkAndRequestPermission() called")
        
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        val hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        Log.d("ImagePicker", "Permission check for $permission: $hasPermission")
        
        if (hasPermission) {
            Log.d("ImagePicker", "Permission already granted")
            onPermissionGranted()
        } else {
            Log.d("ImagePicker", "Requesting permission: $permission")
            permissionLauncher.launch(permission)
        }
    }
}

@Composable
fun rememberImagePickerState(
    onImageSelected: (ByteArray) -> Unit
): ImagePickerState {
    val context = LocalContext.current
    return remember(context) {
        ImagePickerState(context, onImageSelected)
    }
}

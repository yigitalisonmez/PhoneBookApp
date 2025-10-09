package com.example.phonebookapp.presentation.profile.components

import android.graphics.BlurMaskFilter
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileAvatar(
    imageUrl: String?,
    firstName: String,
    isEditMode: Boolean,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iosLightBlue = Color(0xFF007AFF)
    
    var dominantColor by remember { mutableStateOf(iosLightBlue) }
    var hasProfileImage by remember { mutableStateOf(false) }
    var isColorExtracted by remember { mutableStateOf(false) }

    // Görselden dominant renk çıkar
    suspend fun extractDominantColor(url: String) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Palette için gerekli
                .build()
            
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap
            
            bitmap?.let {
                withContext(Dispatchers.Default) {
                    val palette = Palette.from(it).generate()
                    
                    // En baskın rengi bul (vibrant, muted, dark vibrant vs.)
                    val color = palette.vibrantSwatch?.rgb
                        ?: palette.lightVibrantSwatch?.rgb
                        ?: palette.darkVibrantSwatch?.rgb
                        ?: palette.mutedSwatch?.rgb
                        ?: palette.lightMutedSwatch?.rgb
                        ?: palette.darkMutedSwatch?.rgb
                    
                    color?.let { colorInt ->
                        dominantColor = Color(colorInt)
                        hasProfileImage = true
                        isColorExtracted = true
                        Log.d("ProfileAvatar", "Dominant color extracted: $dominantColor")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileAvatar", "Error extracting dominant color", e)
            isColorExtracted = true // Hata durumunda da true yap ki gölge gösterilsin
        }
    }
    
    // Görsel URL değiştiğinde rengi güncelle
    LaunchedEffect(imageUrl) {
        isColorExtracted = false // Reset
        if (!imageUrl.isNullOrEmpty()) {
            extractDominantColor(imageUrl)
        } else {
            dominantColor = iosLightBlue
            hasProfileImage = false
            isColorExtracted = true
        }
    }

    Box(
        modifier = modifier
            .size(160.dp)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Custom shadow layer - sadece renk extract edildikten sonra göster
        if (isColorExtracted) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = 6.dp)
            ) {
                drawIntoCanvas { canvas ->
                    val paint = Paint().asFrameworkPaint()
                    val shadowColor = if (hasProfileImage) {
                        dominantColor.copy(alpha = 0.65f)
                    } else {
                        iosLightBlue.copy(alpha = 0.5f)
                    }
                    paint.color = shadowColor.toArgb()
                    paint.maskFilter = BlurMaskFilter(60f, BlurMaskFilter.Blur.NORMAL)
                    
                    canvas.nativeCanvas.drawCircle(
                        size.width / 2,
                        size.height / 2,
                        size.width / 2,
                        paint
                    )
                }
            }
        }
        
        // Avatar content
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    if (hasProfileImage) {
                        Color.Transparent
                    } else {
                        iosLightBlue.copy(alpha = 0.15f)
                    }
                )
                .clickable { 
                    if (isEditMode) {
                        onAvatarClick()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(imageUrl)
                            .build()
                    ),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initial = firstName.firstOrNull()?.uppercase() ?: ""
                Text(
                    text = initial,
                    fontSize = 48.sp,
                    color = iosLightBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

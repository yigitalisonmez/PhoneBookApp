package com.example.phonebookapp.presentation.ui.success

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.*
import com.example.phonebookapp.R
import kotlinx.coroutines.delay

@Composable
fun SuccessScreen(
    onDismiss: () -> Unit,
    title: String = "All Done!",
    subtitle: String = "New contact saved",
    emoji: String = "🎉"
) {
    var showDialog by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(3000) // 3 saniye sonra otomatik kapan
        showDialog = false
        onDismiss()
    }
    
    if (showDialog) {
        Dialog(
            onDismissRequest = { 
                showDialog = false
                onDismiss()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Lottie Animation
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.done_lottie)
                    )
                    val progress by animateLottieCompositionAsState(
                        composition,
                        iterations = 1,
                        isPlaying = true
                    )
                    
                    LottieAnimation(
                        composition,
                        progress = { progress },
                        modifier = Modifier.size(200.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Title
                    Text(
                        text = title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2E),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Subtitle with emoji
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = subtitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF2C2C2E),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = emoji,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

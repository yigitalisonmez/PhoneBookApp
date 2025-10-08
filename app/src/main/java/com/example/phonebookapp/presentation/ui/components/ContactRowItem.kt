package com.example.phonebookapp.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonebookapp.domain.model.Contact
import kotlin.math.roundToInt

@Composable
fun ContactRowItem(
    contact: Contact,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 120.dp.toPx() }
    
    // Animate offset for smooth transitions
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(300),
        label = "swipe_offset"
    )
    
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Background swipe actions (like SwiftUI)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterEnd)
                .padding(start = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            // Edit action
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0075FF))
                    .clickable { onEdit() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Delete action
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFFF3B30))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Main contact row (swipeable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .background(Color.White)
                .clickable(onClick = onClick)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // Snap to position based on swipe distance
                            offsetX = if (offsetX < -swipeThreshold) {
                                -swipeThreshold * 2 // Show actions
                            } else {
                                0f // Hide actions
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = (offsetX + dragAmount.x).coerceIn(-swipeThreshold * 2, 0f)
                        offsetX = newOffset
                    }
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEDFAFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.firstName.firstOrNull()?.uppercase()?.toString() ?: "",
                    color = Color(0xFF0075FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.firstName + " " + contact.lastName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



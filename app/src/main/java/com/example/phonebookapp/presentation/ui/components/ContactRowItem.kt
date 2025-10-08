package com.example.phonebookapp.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.phonebookapp.domain.model.Contact
import com.example.phonebookapp.presentation.ui.common.LocalScreenDimensions
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteContactBottomSheet(
    contactName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Delete Contact",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Are you sure you want to delete this contact?",
                fontSize = 15.sp,
                color = Color(0xFF3D3D3D),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Butonlar yan yana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // No Button (solda)
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "No",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }

                // Yes Button (sağda)
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF202020)
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Yes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactRowItem(
    contact: Contact,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isActive: Boolean = false,
    onSwipeStart: () -> Unit = {},
    onSwipeEnd: () -> Unit = {}
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    
    // Eğer bu contact aktif değilse, offsetX'i 0 yap
    LaunchedEffect(isActive) {
        if (!isActive && offsetX != 0f) {
            offsetX = 0f
        }
    }
    var showDeleteBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val screenDimensions = LocalScreenDimensions.current
    val swipeThreshold = with(density) { 100.dp.toPx() }
    val maxSwipeDistance = with(density) { 160.dp.toPx() }
    
    // Artık screenDimensions.width ve screenDimensions.height kullanabilirsin!
    // Örnek: val screenWidth = screenDimensions.width
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(300),
        label = "swipe_offset"
    )
    
    // Delete Bottom Sheet
    if (showDeleteBottomSheet) {
        DeleteContactBottomSheet(
            contactName = "${contact.firstName} ${contact.lastName}",
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    showDeleteBottomSheet = false
                    offsetX = 0f
                }
            },
            onConfirm = {
                scope.launch {
                    sheetState.hide()
                    showDeleteBottomSheet = false
                    offsetX = 0f
                    onDelete()
                }
            },
            sheetState = sheetState
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (offsetX != 0f) {
                    offsetX = 0f
                    onSwipeEnd()
                } else {
                    onClick()
                }
            }
    ) {
        // Action buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.End
        ) {
            // Edit action (mavi - solda)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0075FF))
                    .clickable { 
                        offsetX = 0f
                        onSwipeEnd()
                        onEdit() 
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Delete action (kırmızı - sağda)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFFF3B30))
                    .clickable { 
                        showDeleteBottomSheet = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Main contact row (swipeable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            // Swipe başladığında bu contact'ı aktif yap
                            onSwipeStart()
                        },
                        onDragEnd = {
                            if (offsetX < -swipeThreshold) {
                                offsetX = -maxSwipeDistance
                            } else {
                                offsetX = 0f
                                onSwipeEnd()
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = (offsetX + dragAmount.x).coerceIn(-maxSwipeDistance, 0f)
                        offsetX = newOffset
                    }
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEDFAFF)),
                contentAlignment = Alignment.Center
            ) {
                if (!contact.imageUrl.isNullOrEmpty()) {
                    // Profil fotoğrafı varsa göster
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(contact.imageUrl)
                                .build()
                        ),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Profil fotoğrafı yoksa initial göster
                    Text(
                        text = contact.firstName.firstOrNull()?.uppercase()?.toString() ?: "",
                        color = Color(0xFF0075FF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
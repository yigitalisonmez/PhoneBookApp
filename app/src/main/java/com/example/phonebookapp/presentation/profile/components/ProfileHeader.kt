package com.example.phonebookapp.presentation.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileHeader(
    isEditMode: Boolean,
    onCancelClick: () -> Unit,
    onDoneClick: () -> Unit,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(top = 8.dp)
    ) {
        if (isEditMode) {
            // Edit mode - Cancel, title, Done
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancelClick) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF007AFF),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W400,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.4).sp
                    )
                }
                
                Text(
                    text = "Edit Contact",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W600,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF1D1D1F),
                    letterSpacing = (-0.4).sp
                )
                
                TextButton(onClick = onDoneClick) {
                    Text(
                        text = "Done",
                        color = Color(0xFF007AFF),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W600,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.4).sp
                    )
                }
            }
        } else {
            // View mode - Back button on left, 3 dots on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF007AFF)
                    )
                }
                
                IconButton(onClick = onMoreClick) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFF1D1D1F)
                    )
                }
            }
        }
    }
}

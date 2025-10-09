package com.example.phonebookapp.presentation.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.phonebookapp.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ProfileActionMenu(
    visible: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    screenWidth: Dp,
    modifier: Modifier = Modifier
) {
    if (visible) {
        Box(modifier = modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = 0.dp)
                    .width(screenWidth * 0.5f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditClick() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1D1D1F),
                            fontWeight = FontWeight.Normal
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = null,
                            tint = Color(0xFF1D1D1F),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(
                        color = Color(0xFFE5E5E5),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDeleteClick() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFF3B30),
                            fontWeight = FontWeight.Normal
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.trash),
                            contentDescription = null,
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

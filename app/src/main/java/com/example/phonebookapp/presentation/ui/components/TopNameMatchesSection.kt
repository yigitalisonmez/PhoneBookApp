package com.example.phonebookapp.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.phonebookapp.domain.model.Contact
import com.example.phonebookapp.ui.theme.*

@Composable
fun TopNameMatchesSection(
    contacts: List<Contact>,
    searchQuery: String,
    onContactClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Search Results Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                // "TOP NAME MATCHES" Header - Card içinde
                Text(
                    text = "TOP NAME MATCHES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = IosDarkGrey,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // İnce divider
                HorizontalDivider(
                    color = IosLightGrey,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                contacts.forEachIndexed { index, contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onContactClick(contact.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Picture or Initial
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IosLightBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!contact.imageUrl.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(LocalContext.current)
                                            .data(contact.imageUrl)
                                            .build()
                                    ),
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = contact.firstName.firstOrNull()?.uppercase() ?: "",
                                    fontSize = 16.sp,
                                    color = IosBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Contact Info
                        Column {
                            Text(
                                text = "${contact.firstName} ${contact.lastName}".trim(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = contact.phoneNumber,
                                fontSize = 14.sp,
                                color = IosDarkGrey
                            )
                        }
                    }
                    
                    if (index < contacts.size - 1) {
                        HorizontalDivider(
                            color = IosLightGrey,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

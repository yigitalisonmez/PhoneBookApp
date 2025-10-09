package com.example.phonebookapp.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonebookapp.R
import com.example.phonebookapp.ui.theme.*

@Composable
fun EmptyContactsState(onCreateContact: () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val topBarHeight = with(density) { 152.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    val offsetY = -(topBarHeight / 2) / density.density
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = offsetY.dp),  // Dinamik offset
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vector icon (SVG'den dönüştürülmüş) - Yukarıda
            Image(
                painter = painterResource(id = R.drawable.person),
                contentDescription = "No Contacts",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // "No Contacts" başlığı - Yukarıda
            Text(
                text = "No Contacts",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosDarkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Açıklama metni - TAM ORTADA (referans nokta)
            Text(
                text = "Contacts you've added will appear here.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // "Create New Contact" butonu - Altta
            TextButton(
                onClick = onCreateContact,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Create New Contact",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IosBlue
                )
            }
        }
    }
}

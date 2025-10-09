package com.example.phonebookapp.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonebookapp.R
import com.example.phonebookapp.ui.theme.IosBackground

@Composable
fun NoResultsState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IosBackground)
            .offset(y = (-50).dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // No Results Icon with gray circle background
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = Color(0xFFD1D1D1),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.no_results),
                contentDescription = "No Results",
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // "No Results" Text
        Text(
            text = "No Results",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Explanatory Text
        Text(
            text = "The user you are looking for could not be found.",
            fontSize = 16.sp,
            color = Color(0xFF3D3D3D),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        }
    }
}

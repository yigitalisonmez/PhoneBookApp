package com.example.phonebookapp.presentation.ui.common

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ScreenDimensions(
    val width: Dp,
    val height: Dp
)

val LocalScreenDimensions = compositionLocalOf { 
    ScreenDimensions(
        width = 0.dp,
        height = 0.dp
    )
}

package com.example.phonebookapp.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.phonebookapp.ui.theme.IosPlaceholderGrey

@Composable
fun IosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    leadingIcon: (@Composable (() -> Unit))? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { 
            Text(
                text = label,
                color = IosPlaceholderGrey
            ) 
        },
        modifier = modifier
            .fillMaxWidth(),
        singleLine = singleLine,
        enabled = enabled,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            disabledContainerColor = Color.White,
            disabledTextColor = Color.Black,
            disabledPlaceholderColor = IosPlaceholderGrey
        )
    )
}

@Composable
fun IosPhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val filtered: (String) -> Unit = { input ->
        val sanitized = input.filterIndexed { index, c ->
            when {
                c in '0'..'9' -> true
                c == '+' && index == 0 -> true
                else -> false
            }
        }
        onValueChange(sanitized)
    }

    IosTextField(
        value = value,
        onValueChange = filtered,
        label = label,
        modifier = modifier,
        keyboardType = KeyboardType.Phone
    )
}
package com.example.phonebookapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.phonebookapp.presentation.navigation.NavGraph
import com.example.phonebookapp.presentation.ui.common.LocalScreenDimensions
import com.example.phonebookapp.presentation.ui.common.ScreenDimensions
import com.example.phonebookapp.ui.theme.PhoneBookAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PhoneBookAppTheme {
                PhonebookApp()
            }
        }
    }
}

@Composable
fun PhonebookApp() {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val screenDimensions = ScreenDimensions(
        width = configuration.screenWidthDp.dp,
        height = configuration.screenHeightDp.dp
    )

    CompositionLocalProvider(LocalScreenDimensions provides screenDimensions) {
        Surface {
            NavGraph(navController = navController)
        }
    }
}
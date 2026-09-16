package com.example.solarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.solarapp.ui.navigation.AppNavigation
import com.example.solarapp.ui.theme.SolarAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolarAppTheme {
                AppNavigation()
            }
        }
    }
}

package com.example.solarapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.solarapp.ui.navigation.AppNavigation
import com.example.solarapp.ui.theme.SolarAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolarAppTheme {
                AppNavigation()
            }
        }
    }
}

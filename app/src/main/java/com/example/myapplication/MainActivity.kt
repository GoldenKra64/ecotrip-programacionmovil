package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.EcoTripNavHost
import com.example.myapplication.presentation.viewmodel.EcoTripViewModel
import com.example.myapplication.presentation.viewmodel.EcoTripViewModelFactory
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: EcoTripViewModel = viewModel(
                    factory = EcoTripViewModelFactory(this)
                )
                EcoTripNavHost(navController = navController, viewModel = viewModel)
            }
        }
    }
}

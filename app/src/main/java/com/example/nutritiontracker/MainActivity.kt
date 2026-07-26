package com.example.nutritiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nutritiontracker.ui.navigation.NutritionApp
import com.example.nutritiontracker.ui.theme.NutritionTrackerTheme
import com.example.nutritiontracker.camera.CameraController


// Camera code was references from Google Codelab: Getting Started with CameraX
//https://developer.android.com/codelabs/camerax-getting-started?authuser=6#1
class MainActivity : ComponentActivity() {
    private lateinit var cameraController: CameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        cameraController = CameraController(this)

        setContentView(R.layout.activity_main)

        setContent {
            NutritionTrackerTheme {
                NutritionApp(cameraController)
            }
        }

    }

}


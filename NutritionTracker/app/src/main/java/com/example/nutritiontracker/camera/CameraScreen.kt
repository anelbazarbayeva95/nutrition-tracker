package com.example.nutritiontracker.camera

import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

//CameraX with Jetpack Compose was referenced from
//https://medium.com/androiddevelopers/getting-started-with-camerax-in-jetpack-compose-781c722ca0c4
//https://christianstowers.medium.com/android-jetpack-compose-camerax-2b7c996474b0
@Composable
fun CameraScreen(cameraController: CameraController){
    val lifecycle: LifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    Box(modifier = Modifier.fillMaxSize()){
        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = { previewView }
        )

        Box (
            modifier = Modifier
                .size(250.dp, 150.dp)
                .align(Alignment.Center)
                .border(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(.3f),
                    width = 2.dp
                )
        )
    }

    if (cameraController.allPermissionsGranted())
        cameraController.startCamera(
            previewView,
            lifecycle)
    else cameraController.requestPermissions(
        previewView,
        lifecycle)
}

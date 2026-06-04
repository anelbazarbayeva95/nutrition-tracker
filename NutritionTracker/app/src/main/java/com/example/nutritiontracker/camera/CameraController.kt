package com.example.nutritiontracker.camera

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

val options = BarcodeScannerOptions.Builder()
    .setBarcodeFormats(
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_UPC_A,
        Barcode.FORMAT_UPC_E,
        Barcode.FORMAT_CODE_128)
    .build()


// Camera code was references from Google Codelab: Getting Started with CameraX
//https://developer.android.com/codelabs/camerax-getting-started?authuser=6#1
class CameraController(
    private val activity: ComponentActivity,
    private var onBarcodeScanned: ((String) -> Unit)? = null) {

    private val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
    private var pendingPreviewView: PreviewView? = null
    private var pendingLifecycleOwner: LifecycleOwner? = null
    val scanner = BarcodeScanning.getClient(options)
    private var hasScanned = false

    fun barcodeScannedReturnHome(callback: (String) -> Unit){
        onBarcodeScanned = callback
    }

    private val activityResultLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in cameraPermissions && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    activity,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val previewView = pendingPreviewView
                val lifecycleOwner = pendingLifecycleOwner
                if (previewView != null && lifecycleOwner != null){
                    startCamera(previewView, lifecycleOwner)
                }
            }
        }

    fun requestPermissions(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        pendingPreviewView = previewView
        pendingLifecycleOwner = lifecycleOwner
        activityResultLauncher.launch(cameraPermissions)
    }

    fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        hasScanned = false

        previewView.visibility = PreviewView.VISIBLE

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Image analysis code was referenced from:
            // https://developer.android.com/media/camera/camerax/analyze
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analyzer ->
                    analyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        barcodeScanner(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try{
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, analysis
                )

            } catch (exc: Exception){
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(activity))
    }

    // BarcodeScanner code was referenced from:
    // https://developers.google.com/ml-kit/vision/barcode-scanning/android
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun barcodeScanner(imageProxy: ImageProxy){
        val mediaImage = imageProxy.image
        if (hasScanned){
            imageProxy.close()
            return
        }

        if (mediaImage != null){
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        } else {
            imageProxy.close()
            return
        }


        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener{ barcodes ->
                for(barcode in barcodes){
                    val rawValue = barcode.rawValue ?: continue

                    when (barcode.valueType){
                        Barcode.TYPE_PRODUCT -> {
                            Log.i("MLKit", "GTIN/UPC Detected: $rawValue")

                            Toast.makeText(
                                activity,
                                "Barcode Successfully Scanned!",
                                Toast.LENGTH_LONG
                            ).show()

                            hasScanned = true
                            onBarcodeScanned?.invoke(rawValue)

                            break
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("MLKit", "Scan Failed")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

}


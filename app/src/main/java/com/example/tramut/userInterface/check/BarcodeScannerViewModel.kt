package com.example.checkincompose

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors

class BarcodeScannerViewModel : ViewModel() {

    private val _scannedId = MutableStateFlow("")
    val scannedId: StateFlow<String> = _scannedId

    // Single thread for camera analysis to avoid blocking UI
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    fun bindCameraPreview(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // 1. Preview Use Case (The visual feed)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // 2. Image Analysis Use Case (The scanning logic)
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcodeValue ->
                        // Only update if we haven't found one yet (avoids rapid firing)
                        if (_scannedId.value.isEmpty()) {
                            _scannedId.value = barcodeValue
                        }
                    })
                }

            try {
                cameraProvider.unbindAll() // Unbind previous use cases
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("BARCODE_VM", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun resetScannedId() {
        _scannedId.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}

// Helper class to process image frames
class BarcodeAnalyzer(private val onBarcodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_EAN_13
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { code ->
                            onBarcodeDetected(code)
                            return@addOnSuccessListener // Stop after first match
                        }
                    }
                }
                .addOnCompleteListener {
                    // Must close the image to allow the next frame to be processed
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
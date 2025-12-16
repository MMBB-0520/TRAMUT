package com.example.checkincompose

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tramut.userInterface.check.CheckInTopBar


@Composable
fun BarcodeScannerScreen(
    onScanSuccess: (String) -> Unit,
    onManualInputClicked: () -> Unit,
    onBackClicked: () -> Unit = {},
    viewModel: BarcodeScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scannedId by viewModel.scannedId.collectAsState()

    // --- Permission Handling ---
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCamPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCamPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(scannedId) {
        if (scannedId.isNotEmpty()) {
            onScanSuccess(scannedId)
            viewModel.resetScannedId()
        }
    }

    Scaffold(
        topBar = {
            CheckInTopBar(title = "Check-In", onBackClicked = onBackClicked)
        }
    ) { paddingValues ->

        // ROOT CONTAINER: Use Box to layer elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Respect TopBar height
        ) {
            // LAYER 1: The Camera Preview (Background)
            if (hasCamPermission) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        viewModel.bindCameraPreview(context, lifecycleOwner, previewView)
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission required.")
                }
            }

            // LAYER 2: The Scanner Overlay (Centered Frame)
            ScannerOverlay()

            // LAYER 3: The Floating Button (Pinned to Bottom)
            Button(
                onClick = onManualInputClicked,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
                    .width(200.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Input Manually",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ScannerOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Simple visual guide box with thick rounded corners
        Box(
            modifier = Modifier
                .size(280.dp)
                .border(
                    width = 4.dp, // Thicker border
                    color = Color.Black,
                    shape = RoundedCornerShape(24.dp)
                )
        )

    }
}
package com.example.alert_eye.ui

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.koin.androidx.compose.koinViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraPreviewScreen(
    modifier: Modifier = Modifier,
    viewModel: MonitoringViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()
        
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        // We use front camera for driver monitoring
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            
        val safetyAnalyzer = SafetyAnalyzer(
            context = context,
            onFaceResult = { result, width, height ->
                viewModel.processFaceResult(result, width, height)
            },
            onObjectResult = { objResult ->
                viewModel.processObjectResult(objResult)
            }
        )
        
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), safetyAnalyzer)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        FaceOverlay(
            result = uiState.faceResult,
            imageWidth = uiState.imageWidth,
            imageHeight = uiState.imageHeight,
            modifier = Modifier.fillMaxSize()
        )

        if (uiState.isDrowsy || uiState.isDistracted || uiState.isYawning || uiState.isUsingPhone || uiState.isDrinking) {
            val alertText = when {
                uiState.isDrowsy -> "DROWSINESS DETECTED!"
                uiState.isDistracted -> "PLEASE LOOK AT THE ROAD!"
                uiState.isUsingPhone -> "PUT THE PHONE DOWN!"
                uiState.isDrinking -> "UNSAFE DRINKING BEHAVIOR!"
                uiState.isYawning -> "FATIGUE WARNING (YAWNING)"
                else -> ""
            }
            
            val alertColor = when {
                uiState.isDrowsy || uiState.isUsingPhone || uiState.isDrinking -> Color.Red
                uiState.isDistracted -> Color(0xFFFFA500) // Orange
                uiState.isYawning -> Color.Yellow
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(alertColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alertText,
                    color = if (uiState.isYawning) Color.Black else Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    cameraProviderFuture.addListener({
        continuation.resume(cameraProviderFuture.get())
    }, ContextCompat.getMainExecutor(this))
}

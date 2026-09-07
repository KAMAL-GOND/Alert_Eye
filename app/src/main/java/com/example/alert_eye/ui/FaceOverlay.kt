package com.example.alert_eye.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max

@Composable
fun FaceOverlay(
    result: FaceLandmarkerResult?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (result == null || result.faceLandmarks().isEmpty()) return@Canvas

        // Scale factors to map image coordinates to screen coordinates
        val scaleFactor = max(size.width / imageWidth, size.height / imageHeight)
        val xOffset = (size.width - imageWidth * scaleFactor) / 2f
        val yOffset = (size.height - imageHeight * scaleFactor) / 2f

        for (landmark in result.faceLandmarks()[0]) {
            val x = landmark.x() * imageWidth * scaleFactor + xOffset
            val y = landmark.y() * imageHeight * scaleFactor + yOffset

            drawPoints(
                points = listOf(Offset(x, y)),
                pointMode = PointMode.Points,
                color = Color.Green,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
    }
}

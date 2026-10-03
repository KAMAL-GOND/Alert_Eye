package com.example.alert_eye.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

class SafetyAnalyzer(
    context: Context,
    private val onFaceResult: (FaceLandmarkerResult, Int, Int) -> Unit,
    private val onObjectResult: (ObjectDetectorResult) -> Unit
) : ImageAnalysis.Analyzer {

    private var faceLandmarker: FaceLandmarker? = null
    private var objectDetector: ObjectDetector? = null

    init {
        // Init Face Landmarker
        val faceBaseOptions = BaseOptions.builder()
            .setModelAssetPath("face_landmarker.task")
            .build()
        
        val faceOptions = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(faceBaseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumFaces(1)
            .setResultListener { result, _ ->
                onFaceResult(result, imageWidth, imageHeight)
            }
            .setErrorListener { it.printStackTrace() }
            .build()

        faceLandmarker = FaceLandmarker.createFromOptions(context, faceOptions)

        // Init Object Detector
        val objBaseOptions = BaseOptions.builder()
            .setModelAssetPath("efficientdet_lite0.tflite")
            .build()
            
        val objOptions = ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(objBaseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setMaxResults(3)
            .setScoreThreshold(0.4f)
            .setResultListener { result, _ ->
                onObjectResult(result)
            }
            .setErrorListener { it.printStackTrace() }
            .build()
            
        objectDetector = ObjectDetector.createFromOptions(context, objOptions)
    }

    private var imageWidth = 0
    private var imageHeight = 0
    private var lastAnalyzeTime = 0L
    private val THROTTLE_TIMEOUT_MS = 100L // Cap at 10 FPS to save CPU and battery

    override fun analyze(imageProxy: ImageProxy) {
        val currentTimeMs = System.currentTimeMillis()
        if (currentTimeMs - lastAnalyzeTime < THROTTLE_TIMEOUT_MS) {
            imageProxy.close()
            return
        }
        lastAnalyzeTime = currentTimeMs

        val bitmap = imageProxy.toBitmap()
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        
        // Optimize: Only create a new rotated bitmap if necessary
        val rotatedBitmap = if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
        } else {
            bitmap
        }
        
        imageWidth = rotatedBitmap.width
        imageHeight = rotatedBitmap.height

        val mpImage: MPImage = BitmapImageBuilder(rotatedBitmap).build()
        
        // MediaPipe requires strictly monotonically increasing timestamps
        val frameTimestampMs = imageProxy.imageInfo.timestamp / 1_000_000
        
        try {
            faceLandmarker?.detectAsync(mpImage, frameTimestampMs)
            objectDetector?.detectAsync(mpImage, frameTimestampMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        imageProxy.close()
    }
}

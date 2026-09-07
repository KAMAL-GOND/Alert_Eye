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

    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
        
        imageWidth = rotatedBitmap.width
        imageHeight = rotatedBitmap.height

        val mpImage: MPImage = BitmapImageBuilder(rotatedBitmap).build()
        val timestampMs = System.currentTimeMillis()
        
        faceLandmarker?.detectAsync(mpImage, timestampMs)
        objectDetector?.detectAsync(mpImage, timestampMs)
        
        imageProxy.close()
    }
}

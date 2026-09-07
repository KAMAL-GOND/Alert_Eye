package com.example.alert_eye.ui

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import kotlin.math.pow
import kotlin.math.sqrt

class SafetyEngine {

    // Counters
    private var closedFramesCount = 0
    private var yawningFramesCount = 0
    private var distractedFramesCount = 0
    private var phoneFramesCount = 0
    private var drinkFramesCount = 0

    // Thresholds (these will be tuned in Phase 9)
    private val EAR_THRESHOLD = 0.2f
    private val CLOSED_FRAMES_THRESHOLD = 15 // ~0.5s

    private val MAR_THRESHOLD = 0.5f // Mouth Aspect Ratio
    private val YAWNING_FRAMES_THRESHOLD = 30 // ~1s

    // Simple heuristic for head pose (Yaw)
    private val YAW_DISTRACTION_THRESHOLD = 2.0f // Ratio heavily skewed
    private val DISTRACTED_FRAMES_THRESHOLD = 45 // ~1.5s looking away
    
    // Object Detection Thresholds
    private val PHONE_FRAMES_THRESHOLD = 30 // ~1s
    private val DRINK_FRAMES_THRESHOLD = 45 // ~1.5s

    var isDrowsy = false
        private set
    
    var isYawning = false
        private set

    var isDistracted = false
        private set

    var isUsingPhone = false
        private set
        
    var isDrinking = false
        private set

    fun processFaceResult(result: FaceLandmarkerResult) {
        if (result.faceLandmarks().isEmpty()) {
            return
        }

        val landmarks = result.faceLandmarks()[0]

        val leftEAR = calculateEyeAspectRatio(landmarks, LEFT_EYE_INDICES)
        val rightEAR = calculateEyeAspectRatio(landmarks, RIGHT_EYE_INDICES)
        val averageEAR = (leftEAR + rightEAR) / 2.0f

        if (averageEAR < EAR_THRESHOLD) {
            closedFramesCount++
            if (closedFramesCount >= CLOSED_FRAMES_THRESHOLD) {
                isDrowsy = true
            }
        } else {
            closedFramesCount = 0
            isDrowsy = false
        }

        // 2. Yawning Detection
        val mar = calculateMouthAspectRatio(landmarks)
        if (mar > MAR_THRESHOLD) {
            yawningFramesCount++
            if (yawningFramesCount >= YAWNING_FRAMES_THRESHOLD) {
                isYawning = true
            }
        } else {
            yawningFramesCount = 0
            isYawning = false
        }

        // 3. Distraction Detection (Head Pose proxy)
        val isLookingAway = estimateHeadPoseDistraction(landmarks)
        if (isLookingAway) {
            distractedFramesCount++
            if (distractedFramesCount >= DISTRACTED_FRAMES_THRESHOLD) {
                isDistracted = true
            }
        } else {
            distractedFramesCount = 0
            isDistracted = false
        }
    }

    fun processObjectResult(result: ObjectDetectorResult) {
        var phoneDetectedThisFrame = false
        var drinkDetectedThisFrame = false

        for (detection in result.detections()) {
            val categories = detection.categories()
            if (categories.isNotEmpty()) {
                val categoryName = categories[0].categoryName()
                if (categoryName == "cell phone") {
                    phoneDetectedThisFrame = true
                }
                if (categoryName == "bottle" || categoryName == "cup") {
                    drinkDetectedThisFrame = true
                }
            }
        }

        // Phone Logic
        if (phoneDetectedThisFrame) {
            phoneFramesCount++
            if (phoneFramesCount >= PHONE_FRAMES_THRESHOLD) {
                isUsingPhone = true
            }
        } else {
            phoneFramesCount = 0
            isUsingPhone = false
        }

        // Drink Logic
        if (drinkDetectedThisFrame) {
            drinkFramesCount++
            if (drinkFramesCount >= DRINK_FRAMES_THRESHOLD) {
                isDrinking = true
            }
        } else {
            drinkFramesCount = 0
            isDrinking = false
        }
    }

    private fun calculateEyeAspectRatio(landmarks: List<NormalizedLandmark>, indices: IntArray): Float {
        // EAR = (||p2-p6|| + ||p3-p5||) / (2 * ||p1-p4||)
        val p1 = landmarks[indices[0]]
        val p2 = landmarks[indices[1]]
        val p3 = landmarks[indices[2]]
        val p4 = landmarks[indices[3]]
        val p5 = landmarks[indices[4]]
        val p6 = landmarks[indices[5]]

        val dist1 = distance(p2, p6)
        val dist2 = distance(p3, p5)
        val dist3 = distance(p1, p4)

        return ((dist1 + dist2) / (2.0 * dist3)).toFloat()
    }

    private fun calculateMouthAspectRatio(landmarks: List<NormalizedLandmark>): Float {
        // P1: Left corner, P2: Right corner
        // P3: Top lip, P4: Bottom lip
        val p1 = landmarks[61]  // Left mouth corner
        val p2 = landmarks[291] // Right mouth corner
        val p3 = landmarks[13]  // Top inner lip
        val p4 = landmarks[14]  // Bottom inner lip

        val width = distance(p1, p2)
        val height = distance(p3, p4)

        if (width == 0.0) return 0f
        return (height / width).toFloat()
    }

    private fun estimateHeadPoseDistraction(landmarks: List<NormalizedLandmark>): Boolean {
        // Simple heuristic: compare distance from nose to left cheek vs nose to right cheek
        val nose = landmarks[1]
        val leftCheek = landmarks[234]
        val rightCheek = landmarks[454]

        val leftDist = distance(nose, leftCheek)
        val rightDist = distance(nose, rightCheek)

        // If ratio is extremely skewed, head is turned significantly
        val ratio = leftDist / rightDist
        return ratio > YAW_DISTRACTION_THRESHOLD || ratio < (1.0 / YAW_DISTRACTION_THRESHOLD)
    }

    private fun distance(p1: NormalizedLandmark, p2: NormalizedLandmark): Double {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        val dz = p1.z() - p2.z()
        return sqrt((dx * dx + dy * dy + dz * dz).toDouble())
    }

    companion object {
        // MediaPipe Face Mesh Indices for eyes
        // P1(Outer), P2(Top Outer), P3(Top Inner), P4(Inner), P5(Bottom Inner), P6(Bottom Outer)
        // Adjust these standard indices as necessary based on exact 478-mesh specs.
        private val LEFT_EYE_INDICES = intArrayOf(33, 160, 158, 133, 153, 144)
        private val RIGHT_EYE_INDICES = intArrayOf(362, 385, 387, 263, 373, 380)
    }
}

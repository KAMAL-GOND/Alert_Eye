package com.example.alert_eye

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alert_eye.ui.CameraPreviewScreen
import com.example.alert_eye.ui.auth.LoginScreen
import com.example.alert_eye.ui.auth.SignupScreen
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import com.example.alert_eye.ui.contacts.ContactScreen
import com.example.alert_eye.ui.theme.Alert_EyeTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Alert_EyeTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                val startDestination = if (auth.currentUser != null) "monitoring" else "login"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            onNavigateToSignup = { navController.navigate("signup") },
                            onLoginSuccess = {
                                navController.navigate("monitoring") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("signup") {
                        SignupScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            onSignupSuccess = {
                                navController.navigate("monitoring") {
                                    popUpTo("login") { inclusive = true }
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("monitoring") {
                        var hasCameraPermission by remember {
                            mutableStateOf(
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            )
                        }

                        val permissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission(),
                            onResult = { isGranted ->
                                hasCameraPermission = isGranted
                            }
                        )

                        LaunchedEffect(Unit) {
                            if (!hasCameraPermission) {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }

                        @OptIn(ExperimentalMaterial3Api::class)
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopAppBar(
                                    title = { Text("AlertEye Monitoring") },
                                    actions = {
                                        IconButton(onClick = { navController.navigate("contacts") }) {
                                            Icon(Icons.Default.Person, contentDescription = "Contacts")
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            if (hasCameraPermission) {
                                CameraPreviewScreen(modifier = Modifier.padding(innerPadding))
                            } else {
                                Text(
                                    text = "Camera permission is required for driver monitoring.",
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                    composable("contacts") {
                        ContactScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
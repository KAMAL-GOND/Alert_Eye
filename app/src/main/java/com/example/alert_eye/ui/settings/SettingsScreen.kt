package com.example.alert_eye.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Sensitivity", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: AI Detection Sensitivity
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI Detection Sensitivity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Drowsiness (EAR) Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Eye Closure (EAR Threshold):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format("%.2f", settings.earThreshold),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = settings.earThreshold,
                        onValueChange = { viewModel.updateEarThreshold(it) },
                        valueRange = 0.15f..0.30f,
                        steps = 14
                    )
                    Text(
                        text = "Standard: 0.20. Higher values trigger drowsiness alerts earlier when eyelids drop.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Yawning (MAR) Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Yawn Mouth Opening (MAR):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format("%.2f", settings.marThreshold),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = settings.marThreshold,
                        onValueChange = { viewModel.updateMarThreshold(it) },
                        valueRange = 0.35f..0.70f,
                        steps = 6
                    )
                    Text(
                        text = "Standard: 0.50. Lower values trigger yawning warnings on smaller mouth openings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Head Pose Distraction Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Head Pose Distraction Ratio:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format("%.1f", settings.distractionSensitivity),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = settings.distractionSensitivity,
                        onValueChange = { viewModel.updateDistractionSensitivity(it) },
                        valueRange = 1.5f..3.0f,
                        steps = 14
                    )
                    Text(
                        text = "Standard: 2.0. Determines allowable head rotation before distraction alert.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Section 2: Alert & Audio Feedback
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alert & Feedback",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Audio Alarm", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Play audio siren during high severity alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isSoundAlertEnabled,
                            onCheckedChange = { viewModel.toggleSoundAlert(it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Vibration Feedback", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Vibrate device during alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isVibrationAlertEnabled,
                            onCheckedChange = { viewModel.toggleVibrationAlert(it) }
                        )
                    }
                }
            }

            // Section 3: Emergency SOS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Emergency SOS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Impact Detection", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Trigger SOS on violent accelerometer spikes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isImpactDetectionEnabled,
                            onCheckedChange = { viewModel.toggleImpactDetection(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SOS Cancellation Countdown:",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Time allowed to cancel false alarms before SMS dispatch",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { seconds ->
                            val isSelected = settings.sosCountdownSeconds == seconds
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateSosCountdownSeconds(seconds) },
                                label = { Text("${seconds}s") }
                            )
                        }
                    }
                }
            }

            // Section 4: Detection Feature Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Safety Features",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureToggleRow(
                        title = "Drowsiness & Micro-Sleep",
                        subtitle = "Track eyelid closure rate",
                        checked = settings.isDrowsinessEnabled,
                        onCheckedChange = { viewModel.toggleDrowsiness(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    FeatureToggleRow(
                        title = "Head Distraction",
                        subtitle = "Detect when driver looks away from road",
                        checked = settings.isDistractionEnabled,
                        onCheckedChange = { viewModel.toggleDistraction(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    FeatureToggleRow(
                        title = "Yawning Warning",
                        subtitle = "Detect repeated fatigue yawning",
                        checked = settings.isYawningEnabled,
                        onCheckedChange = { viewModel.toggleYawning(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    FeatureToggleRow(
                        title = "Mobile Phone Detection",
                        subtitle = "Detect phone handling while driving",
                        checked = settings.isPhoneDetectionEnabled,
                        onCheckedChange = { viewModel.togglePhoneDetection(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    FeatureToggleRow(
                        title = "Drinking Detection",
                        subtitle = "Detect bottles and drinking cups",
                        checked = settings.isDrinkingDetectionEnabled,
                        onCheckedChange = { viewModel.toggleDrinkingDetection(it) }
                    )
                }
            }

            // Section 5: Reset Defaults
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset All Settings to Defaults")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings?") },
            text = { Text("This will restore all sensitivity thresholds, alert rules, and countdown durations to factory defaults.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FeatureToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

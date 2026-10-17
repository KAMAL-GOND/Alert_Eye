package com.example.alert_eye.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alert_eye.data.local.SafetyEventEntity
import com.example.alert_eye.data.Severity
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val events by viewModel.events.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(onClick = { viewModel.syncNow() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Now")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No safety events recorded.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    EventCard(event = event)
                }
            }
        }
    }
}

@Composable
fun EventCard(event: SafetyEventEntity) {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    val formattedDate = formatter.format(Date(event.timestamp))
    
    val severityColor = when (event.severity) {
        Severity.EMERGENCY, Severity.HIGH -> Color.Red
        Severity.MEDIUM -> Color(0xFFFFA500)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.type.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = severityColor
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Severity: ${event.severity.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (event.isSynced) {
                    Text(
                        text = "Synced (Cloud)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Local Only",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

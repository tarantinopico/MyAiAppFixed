package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.premium.DynamicGlassCard
import com.example.ui.components.premium.LiquidGlassSurface
import com.example.ui.components.premium.bounceClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceCustomizationScreen(
    onBack: () -> Unit,
    blurIntensity: Float,
    onBlurIntensityChange: (Float) -> Unit,
    animationSpeed: Float,
    onAnimationSpeedChange: (Float) -> Unit,
    currentThemeMode: Int,
    onThemeModeChange: (Int) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.bounceClick { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        "Appearance Settings", 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DynamicGlassCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Theme Mode", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            FilterChip(
                                selected = currentThemeMode == 0,
                                onClick = { onThemeModeChange(0) },
                                label = { Text("Auto") }
                            )
                            FilterChip(
                                selected = currentThemeMode == 1,
                                onClick = { onThemeModeChange(1) },
                                label = { Text("Light") }
                            )
                            FilterChip(
                                selected = currentThemeMode == 2,
                                onClick = { onThemeModeChange(2) },
                                label = { Text("Dark") }
                            )
                        }
                    }
                }
            }

            item {
                DynamicGlassCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Glassmorphism Blur Intensity", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Slider(
                            value = blurIntensity,
                            onValueChange = onBlurIntensityChange,
                            valueRange = 0f..200f
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Minimal", style = MaterialTheme.typography.labelSmall)
                            Text("Heavy Glass", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            item {
                DynamicGlassCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Animation Speed", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Slider(
                            value = animationSpeed,
                            onValueChange = onAnimationSpeedChange,
                            valueRange = 0.1f..3f
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Slow & Fluid", style = MaterialTheme.typography.labelSmall)
                            Text("Snappy", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            
            // Further tweaks could go here
        }
    }
}

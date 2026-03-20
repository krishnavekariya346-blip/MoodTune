package com.moodtune.app.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer

@Composable
fun MoodSelectionScreen(
    onMoodSelected: (String) -> Unit
) {
    var selectedMood by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Your Mood",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        MoodButton(
            text = "😊 Happy",
            isSelected = selectedMood == "happy",
            onClick = {
                selectedMood = "happy"
                onMoodSelected("happy")
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MoodButton(
            text = "😔 Sad",
            isSelected = selectedMood == "sad",
            onClick = {
                selectedMood = "sad"
                onMoodSelected("sad")
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MoodButton(
            text = "😡 Angry",
            isSelected = selectedMood == "angry",
            onClick = {
                selectedMood = "angry"
                onMoodSelected("angry")
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MoodButton(
            text = "😲 Surprise",
            isSelected = selectedMood == "surprise",
            onClick = {
                selectedMood = "surprise"
                onMoodSelected("surprise")
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MoodButton(
            text = "😐 Neutral",
            isSelected = selectedMood == "neutral",
            onClick = {
                selectedMood = "neutral"
                onMoodSelected("neutral")
            }
        )
    }
}

@Composable
fun MoodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


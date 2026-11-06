package com.headshotaipro.trainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.headshotaipro.trainer.presentation.HeadshotAiApp
import com.headshotaipro.trainer.presentation.theme.HeadshotAIProTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HeadshotAiApplication
        setContent {
            HeadshotAIProTheme {
                HeadshotAiApp(repository = app.trainingRepository)
            }
        }
    }
}

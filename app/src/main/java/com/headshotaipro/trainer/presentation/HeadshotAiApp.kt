package com.headshotaipro.trainer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.headshotaipro.trainer.domain.repository.TrainingRepository
import com.headshotaipro.trainer.presentation.screens.HeadshotArenaScreen
import com.headshotaipro.trainer.presentation.screens.PerformanceAnalyticsScreen
import com.headshotaipro.trainer.presentation.screens.SensitivityAnalyzerScreen
import com.headshotaipro.trainer.presentation.theme.CharcoalElevated
import com.headshotaipro.trainer.presentation.theme.CharcoalSurface
import com.headshotaipro.trainer.presentation.theme.CrimsonHeadshot
import com.headshotaipro.trainer.presentation.theme.CyberCyan
import com.headshotaipro.trainer.presentation.theme.MatrixGreen
import com.headshotaipro.trainer.presentation.theme.ObsidianBlack
import com.headshotaipro.trainer.presentation.theme.TextPrimary
import com.headshotaipro.trainer.presentation.theme.TextSecondary
import com.headshotaipro.trainer.presentation.viewmodel.AnalyticsViewModel
import com.headshotaipro.trainer.presentation.viewmodel.SensitivityViewModel
import com.headshotaipro.trainer.presentation.viewmodel.TrainingViewModel

enum class AppDestination(val title: String, val icon: ImageVector) {
    ARENA("AI Arena", Icons.Default.GpsFixed),
    SENSITIVITY("Sensitivity AI", Icons.Default.Tune),
    ANALYTICS("Analytics", Icons.Default.Analytics)
}

@Composable
fun HeadshotAiApp(
    repository: TrainingRepository
) {
    var currentTab by rememberSaveable { mutableStateOf(AppDestination.ARENA) }

    val trainingViewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModel.provideFactory(repository)
    )
    val sensitivityViewModel: SensitivityViewModel = viewModel(
        factory = SensitivityViewModel.provideFactory(repository)
    )
    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.provideFactory(repository)
    )

    Scaffold(
        containerColor = ObsidianBlack,
        topBar = {
            TopBrandBar()
        },
        bottomBar = {
            NavigationBar(
                containerColor = CharcoalSurface,
                tonalElevation = 8.dp
            ) {
                AppDestination.entries.forEach { dest ->
                    val selected = currentTab == dest
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.title
                            )
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonHeadshot,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CharcoalElevated,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppDestination.ARENA -> HeadshotArenaScreen(viewModel = trainingViewModel)
                AppDestination.SENSITIVITY -> SensitivityAnalyzerScreen(viewModel = sensitivityViewModel)
                AppDestination.ANALYTICS -> PerformanceAnalyticsScreen(viewModel = analyticsViewModel)
            }
        }
    }
}

@Composable
private fun TopBrandBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CharcoalSurface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "HEADSHOT",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = CrimsonHeadshot
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "AI PRO",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = CyberCyan
            )
        }

        Surface(
            color = ObsidianBlack,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.border(1.dp, MatrixGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MatrixGreen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "STANDALONE SAFE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen
                )
            }
        }
    }
}

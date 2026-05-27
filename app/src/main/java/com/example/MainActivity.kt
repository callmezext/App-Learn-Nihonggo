package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.KanaChartScreen
import com.example.ui.screens.KotobaScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.CoverScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NihongoViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: NihongoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appTheme by viewModel.currentTheme.collectAsState()
            MyApplicationTheme(appTheme = appTheme) {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: NihongoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val levels by viewModel.levels.collectAsState()
    val allKana by viewModel.allKana.collectAsState()
    val minnaVocab by viewModel.minnaVocab.collectAsState()
    val habikiVocab by viewModel.habikiVocab.collectAsState()
    val activeQuiz by viewModel.activeQuiz.collectAsState()

    // Hide bottom navigation bar during active quiz or cover screen to maximize content focus
    val showBottomBar = activeQuiz == null && currentScreen !is Screen.Cover

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Dashboard,
                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                        icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Level") },
                        modifier = Modifier.testTag("nav_btn_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.KanaChart,
                        onClick = { viewModel.navigateTo(Screen.KanaChart) },
                        icon = { Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Kana Kartu") },
                        label = { Text("Kana Kartu") },
                        modifier = Modifier.testTag("nav_btn_kana")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Kotoba,
                        onClick = { viewModel.navigateTo(Screen.Kotoba) },
                        icon = { Icon(imageVector = Icons.Default.Style, contentDescription = "Kotoba") },
                        label = { Text("Kotoba") },
                        modifier = Modifier.testTag("nav_btn_kotoba")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Stats,
                        onClick = { viewModel.navigateTo(Screen.Stats) },
                        icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "Stats & Tema") },
                        label = { Text("Stats & Tema") },
                        modifier = Modifier.testTag("nav_btn_stats")
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Cover -> {
                    CoverScreen(viewModel = viewModel)
                }
                is Screen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        levels = levels,
                        allKana = allKana,
                        minnaVocab = minnaVocab,
                        habikiVocab = habikiVocab
                    )
                }
                is Screen.KanaChart -> {
                    KanaChartScreen(
                        viewModel = viewModel,
                        allKana = allKana
                    )
                }
                is Screen.Quiz -> {
                    activeQuiz?.let { state ->
                        QuizScreen(
                            viewModel = viewModel,
                            quizState = state
                        )
                    } ?: run {
                        // Fallback state
                        viewModel.navigateTo(Screen.Dashboard)
                    }
                }
                is Screen.Kotoba -> {
                    KotobaScreen(
                        viewModel = viewModel,
                        minnaVocab = minnaVocab,
                        habikiVocab = habikiVocab
                    )
                }
                is Screen.Stats -> {
                    com.example.ui.screens.StatsScreen(
                        viewModel = viewModel,
                        allKana = allKana,
                        minnaVocab = minnaVocab,
                        habikiVocab = habikiVocab
                    )
                }
                else -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        levels = levels,
                        allKana = allKana,
                        minnaVocab = minnaVocab,
                        habikiVocab = habikiVocab
                    )
                }
            }
        }
    }
}

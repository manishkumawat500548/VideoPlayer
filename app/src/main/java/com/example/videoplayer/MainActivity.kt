package com.example.videoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.videoplayer.presentation.HomeScreen
import com.example.videoplayer.presentation.LocalMediaScreen
import com.example.videoplayer.presentation.PlayerScreen
import com.example.videoplayer.repository.VideoPlayerRepo
import com.example.videoplayer.ui.theme.VideoPlayerTheme
import com.example.videoplayer.viewmodel.VideoPlayerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = VideoPlayerRepo(applicationContext)
        setContent {
            VideoPlayerTheme {
                val navController = rememberNavController()
                val viewModel: VideoPlayerViewModel = viewModel {
                    VideoPlayerViewModel(repo)
                }
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { HomeScreen(navController) }
                    composable("localMedia") {
                        LocalMediaScreen(viewModel, navController)
                    }
                    composable("player/{videoId}", arguments = listOf(navArgument("videoId") {
                        type = NavType.LongType
                    })) {backStackEntry->
                        val videoId = backStackEntry.arguments?.getLong("videoId")?:0L
                        PlayerScreen(videoId,viewModel,navController)

                    }
                }
            }
        }
    }
}


package com.example.videoplayer.presentation

import android.app.Activity
import android.app.RecoverableSecurityException
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.videoplayer.model.LocalVideo
import com.example.videoplayer.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalMediaScreen(viewModel: VideoPlayerViewModel, navController: NavHostController) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadLocalVideos()
    }
    var videoToDelete by remember { mutableStateOf<LocalVideo?>(null) }
    var pendingDeleteVideo by remember { mutableStateOf<LocalVideo?>(null) }

    val scope = rememberCoroutineScope()

    val deleteLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                pendingDeleteVideo?.let {
                    scope.launch {
                        viewModel.removeVideoFromList(it)
                    }
                    Toast.makeText(context, "Video Deleted", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Delete cancelled", Toast.LENGTH_SHORT).show()
            }
            pendingDeleteVideo = null
        }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Local Media") }, navigationIcon = {
            IconButton(onClick = {navController.navigateUp()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        })
    }) { padding ->

        when {
            viewModel.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            viewModel.localVideos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("No video found")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                )
                {
                    items(viewModel.localVideos) { video ->
                        VideoRow(
                            video = video,
                            onClick = { navController.navigate("player/${video.id}")
                            },
                            onDelete = { videoToDelete = video })
                    }
                }
            }
        }

    }
    videoToDelete?.let { video ->
        AlertDialog(
            onDismissRequest = { videoToDelete = null },
            title = { Text("Delete video") },
            text = { Text("kya aap \"${video.title}\" ko permanently delte karna chate hain ? \nn yeh action undo nahi ho sakta!") },
            confirmButton = {
                TextButton(onClick = {
                    videoToDelete = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            val deleteRequest = MediaStore.createDeleteRequest(
                                context.contentResolver, listOf(video.uri)
                            )
                            pendingDeleteVideo = video
                            deleteLauncher.launch(
                                IntentSenderRequest.Builder(deleteRequest.intentSender).build()
                            )
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Delete failed:${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        scope.launch {
                            try {
                                var delete = withContext(Dispatchers.IO) {
                                    context.contentResolver.delete(video.uri, null, null) > 0
                                }
                                if (delete) {
                                    viewModel.removeVideoFromList(video)
                                    Toast.makeText(context, "Video Deleted", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (securityException: SecurityException) {
                                val recoverableException =
                                    securityException as? RecoverableSecurityException
                                recoverableException?.let {
                                    pendingDeleteVideo = video

                                    deleteLauncher.launch(
                                        IntentSenderRequest.Builder(it.userAction.actionIntent.intentSender)
                                            .build()
                                    )
                                } ?: run {
                                    Toast.makeText(
                                        context,
                                        "Cannot delete this video",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        scope.launch {
                            val success = viewModel.deleteVideo(video)
                            if (success) {
                                Toast.makeText(context, "Vidoe Deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }, dismissButton = {
                TextButton(onClick = { videoToDelete = null }) {
                    Text("Cancel")
                }
            }
        )

    }
}
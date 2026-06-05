package com.example.videoplayer.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.videoplayer.model.LocalVideo
import com.example.videoplayer.repository.VideoPlayerRepo

class VideoPlayerViewModel(private val videoPlayerRepo: VideoPlayerRepo): ViewModel() {

    var localVideos by mutableStateOf<List<LocalVideo>>(emptyList())
    var isLoading by mutableStateOf(false)

    suspend fun loadLocalVideos(){
        isLoading = true
        localVideos = videoPlayerRepo.getLocalVideo()
        isLoading = false
    }

    fun getVideoUri(id: Long): Uri = videoPlayerRepo.getVideoById(id)

    suspend fun deleteVideo(video: LocalVideo): Boolean{
        val success = videoPlayerRepo.deleteVideo(video.uri)

        if (success){
            localVideos = localVideos.filter { it.id != video.id }
        }
        return success
    }

    fun removeVideoFromList(video: LocalVideo){
        localVideos = localVideos.filter { it.id != video.id}
    }

}
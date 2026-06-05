package com.example.videoplayer.model

import android.net.Uri


data class LocalVideo (
    val id: Long,
    val title: String,
    val uri: Uri,
    val duration: Long,
    val size: Long,
    val dataAdded: Long,
    val minType: String?
)
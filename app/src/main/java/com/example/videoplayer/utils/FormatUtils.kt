package com.example.videoplayer.utils

fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600

    return if (hours > 0) {
        String.format("%d:02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

fun formatFileSize(byte: Long): String {
    val kb = byte / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1028.0

    return when {
        gb >= 1-> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        else -> String.format("%.2f KB", kb)
    }
}
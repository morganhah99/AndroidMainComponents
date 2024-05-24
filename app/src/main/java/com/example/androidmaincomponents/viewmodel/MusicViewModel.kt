package com.example.androidmaincomponents.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MusicViewModel: ViewModel() {

    var currentSong = mutableStateOf("Pink + White")
    var currentArtist = mutableStateOf("Frank Ocean")


    val musicDetailsMap = mutableStateMapOf(
        "Frank Ocean" to "Playing: Pink + White",
        "Tory Lanez" to "Playing: The Color Violet",
        "21 Savage" to "Playing: No Heart"
    )

    fun updateMusicDetail(artist: String) {

    }

}
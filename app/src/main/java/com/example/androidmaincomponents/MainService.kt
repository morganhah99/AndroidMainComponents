package com.example.androidmaincomponents

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.widget.Toast

class MainService : Service() {
    private var musicPlayer: MediaPlayer? = null
    private val songList = listOf(R.raw.pink, R.raw.color_violet, R.raw.no_heart)
    private var currentSongIndex = 0

    companion object {
        const val ACTION_PLAY = "com.example.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.ACTION_RESUME"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.ACTION_PREVIOUS"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        musicPlayer = MediaPlayer.create(this, R.raw.pink)
        musicPlayer!!.isLooping = true
    }

    private fun initializePlayer(songIndex: Int) {
        musicPlayer?.release() // Release any previous MediaPlayer
        musicPlayer = MediaPlayer.create(this, songList[songIndex])
        musicPlayer!!.isLooping = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                Toast.makeText(this, "Music Service started by user.", Toast.LENGTH_SHORT).show()
                musicPlayer!!.start()
            }
            ACTION_PAUSE -> {
                Toast.makeText(this, "Music Service paused by user.", Toast.LENGTH_SHORT).show()
                musicPlayer!!.pause()
            }
            ACTION_RESUME -> {
                Toast.makeText(this, "Music Service resumed by user.", Toast.LENGTH_SHORT).show()
                musicPlayer!!.start()
            }
            ACTION_NEXT -> {
                Toast.makeText(this, "Playing Next song", Toast.LENGTH_SHORT).show()
                playNextSong()
            }
            ACTION_PREVIOUS -> {
                Toast.makeText(this, "Playing Previous song", Toast.LENGTH_SHORT).show()
                playPreviousSong()
            }

        }
        return START_STICKY
    }

    private fun playNextSong() {
        currentSongIndex += 1
        if (currentSongIndex >= songList.size) {
            currentSongIndex = 0
        }
        initializePlayer(currentSongIndex)
        musicPlayer!!.start()
    }

    private fun playPreviousSong() {
        currentSongIndex -= 1
        if (currentSongIndex < 0) {
            currentSongIndex = songList.size - 1
        }
        initializePlayer(currentSongIndex)
        musicPlayer!!.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        musicPlayer!!.stop()
        musicPlayer!!.release()
        Toast.makeText(this, "Music Service destroyed by user.", Toast.LENGTH_SHORT).show()
    }
}

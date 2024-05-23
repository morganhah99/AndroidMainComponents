package com.example.androidmaincomponents

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.widget.Toast

class MainService : Service() {
    var musicPlayer: MediaPlayer? = null
    val songList = listOf(R.raw.pink, R.raw.color_violet)
    private var currentSongIndex = 0

    companion object {
        const val ACTION_PLAY = "com.example.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.ACTION_RESUME"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        musicPlayer = MediaPlayer.create(this, R.raw.color_violet)
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
                Toast.makeText(this, "Music Service started by user.", Toast.LENGTH_LONG).show()
                musicPlayer!!.start()
            }
            ACTION_PAUSE -> {
                Toast.makeText(this, "Music Service paused by user.", Toast.LENGTH_LONG).show()
                musicPlayer!!.pause()
            }
            ACTION_RESUME -> {
                Toast.makeText(this, "Music Service resumed by user.", Toast.LENGTH_LONG).show()
                musicPlayer!!.start()
            }
            ACTION_NEXT -> {
                Toast.makeText(this, "Playing Next song", Toast.LENGTH_LONG).show()
                playNextSong()
            }

        }
        return START_STICKY
    }

    private fun playNextSong() {
        currentSongIndex = (currentSongIndex + 1) % songList.size
        initializePlayer(currentSongIndex)
        musicPlayer!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer!!.stop()
        musicPlayer!!.release()
        Toast.makeText(this, "Music Service destroyed by user.", Toast.LENGTH_LONG).show()
    }
}

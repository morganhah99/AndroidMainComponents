package com.example.androidmaincomponents

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.widget.Toast

class MainService : Service() {
    var musicPlayer: MediaPlayer? = null

    /*
    * To bind/attach it with another component of android app
    * */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        musicPlayer = MediaPlayer.create(this, R.raw.song)
        musicPlayer!!.isLooping = true
    }

    /*
    * Start the service and maintain the process
    * */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Music Service started by user.", Toast.LENGTH_LONG).show()
        musicPlayer!!.start()
        return START_STICKY
    }

    /*
    * Destroy the service and it's reference
    * */
    override fun onDestroy() {
        super.onDestroy()
        musicPlayer!!.stop()
        Toast.makeText(this, "Music Service destroyed by user.", Toast.LENGTH_LONG).show()
    }

}
package com.example.androidmaincomponents.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import com.example.androidmaincomponents.MainService



fun musicButtonHelper(context: Context, action: String) {
    when {
        isMyServiceRunning(MainService::class.java, context) -> {
            context.startService(Intent(context, MainService::class.java).apply {
                this.action = when (action) {
                    MainService.ACTION_PLAY -> MainService.ACTION_RESUME
                    MainService.ACTION_PAUSE -> MainService.ACTION_PAUSE
                    MainService.ACTION_NEXT -> MainService.ACTION_NEXT
                    MainService.ACTION_PREVIOUS -> MainService.ACTION_PREVIOUS
                    else -> MainService.ACTION_PLAY
                }
            })
        }

        else -> {
            context.startService(Intent(context, MainService::class.java).apply {
                this.action = MainService.ACTION_PLAY
            })
        }
    }

}

private fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

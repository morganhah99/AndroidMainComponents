package com.example.androidmaincomponents

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidmaincomponents.ui.theme.AndroidMainComponentsTheme
import android.app.ActivityManager
import android.content.Context
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {


    private val contactRepository by lazy { ContactRepository(this) }
    private val contacts = mutableStateListOf<ContactRepository.Contact>()


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidMainComponentsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        MusicControllerButtons()
                        ContactList()
                    }
                }
            }
        }

        //Initialize Broadcast
        val filter = IntentFilter()
        filter.addAction("com.example.androidmaincomponents.MainBroadcastReceiver")
        registerReceiver(MainBroadcastReceiver(), filter, RECEIVER_EXPORTED)

        val intent = Intent("com.example.androidmaincomponents.MainBroadcastReceiver")
        sendBroadcast(intent)

        val rootView = findViewById<View>(android.R.id.content)
        contacts.addAll(contactRepository.loadContacts(this, rootView))

    }

    @Composable
    fun ContactList() {
        LazyColumn {
            items(contacts) { contact ->
                ContactCardView(contact = contact)
            }

        }
    }

    @Composable
    fun ContactCardView(contact: ContactRepository.Contact) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Contact ID: ${contact.contactID}")
                Text(text = "Name: ${contact.name}")
                Text(text = "Phone: ${contact.phoneNumber}")
                Text(text = "Email: ${contact.email ?: "N/A"}")
            }
        }
    }

    @Composable
    fun MusicControllerButtons() {
        var isPlaying by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val blackColor = Color(0xFF000000)

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            IconButton(onClick = {
                musicButtonHelper(context, MainService.ACTION_PREVIOUS)
            }) {
                Image(
                    painter = painterResource(R.drawable.previous_song),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = blackColor),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                musicButtonHelper(context, if (isPlaying) MainService.ACTION_PAUSE else MainService.ACTION_PLAY)
                isPlaying = !isPlaying
            }) {
                if (isPlaying) {
                    Image(
                        painter = painterResource(R.drawable.pause_button),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = blackColor),
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.play_arrow),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = blackColor),
                        modifier = Modifier.size(48.dp)
                    )

                }

            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                musicButtonHelper(context, MainService.ACTION_NEXT)
            }) {
                Image(
                    painter = painterResource(R.drawable.next_song),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = blackColor),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }

    private fun musicButtonHelper(context: Context, action: String) {
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ContactRepository.REQUEST_READ_CONTACTS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            contacts.addAll(contactRepository.getContactList(this))
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

}



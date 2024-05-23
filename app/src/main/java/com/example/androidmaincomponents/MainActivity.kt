package com.example.androidmaincomponents

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androidmaincomponents.ui.theme.AndroidMainComponentsTheme
import com.google.android.material.snackbar.Snackbar
import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.provider.ContactsContract
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

    private val REQUEST_READ_CONTACTS: Int = 1231

    private val contacts = mutableListOf<Contact>()


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

        loadContacts()
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
    fun ContactCardView(contact: Contact) {
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
            IconButton(onClick = { /*TODO*/ }) {
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


    private fun loadContacts() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val contacts = getContactList(this)
            Log.d("Contacts", contacts.joinToString(separator = "\n"))
//            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
        } else {
            requestContactsPermission()
            Toast.makeText(this, "Permission!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestContactsPermission() {
        // Check if the permission has already been granted
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission has already been granted, do something with the contact list
            val contacts = getContactList(this)
            Log.d("Contacts", contacts.joinToString(separator = "\n"))
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
            // Do something with the contact list
        } else {
            // Permission has not been granted, request it
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_CONTACTS
                )
            ) {
                // Explain why the app needs the permission
                // You can show a dialog or a Snackbar here
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "The app needs permission to access your contacts.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("OK") {
                    // Request the permission
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS
                    )
                }.show()
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_CONTACTS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission has been granted, do something with the contact list
            val contacts = getContactList(this)
            Log.d("Contacts", contacts.joinToString(separator = "\n"))
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
            // Do something with the contact list
        } else {
            // Permission has been denied
            // You can show a dialog or a Snackbar here to explain why the app needs the permission
        }
    }


    @SuppressLint("Range")
    fun getContactList(context: Context): List<Contact> {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null, null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber =
                    it.getInt(it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
                if (hasPhoneNumber) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    var email: String? = null
                    val emailCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    emailCursor?.use { ec ->
                        if (ec.moveToNext()) {
                            email =
                                ec.getString(ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                        }
                    }
                    phoneCursor?.use { pc ->
                        while (pc.moveToNext()) {
                            val phoneNumber =
                                pc.getString(pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            val contactID =
                                pc.getString(pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                            val contact = Contact(name, phoneNumber, contactID, email)
                            contacts.add(contact)
                        }
                    }
                    phoneCursor?.close()
                } else {
                    val contact = Contact(name, null, null, null)
                    contacts.add(contact)
                }
            }
        }
        cursor?.close()
        return contacts
    }

    data class Contact(
        val name: String, val phoneNumber: String?, val contactID: String?, val email: String?
    )


}



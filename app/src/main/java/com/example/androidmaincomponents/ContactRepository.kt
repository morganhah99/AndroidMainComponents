package com.example.androidmaincomponents

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class ContactRepository(private val context: Context) {

    companion object {
        const val REQUEST_READ_CONTACTS: Int = 1231
    }

    private val contacts = mutableStateListOf<Contact>()

    data class Contact(
        val name: String, val phoneNumber: String?, val contactID: String?, val email: String?
    )

    fun loadContacts(activity: MainActivity, rootView: View): List<Contact> {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val contacts = getContactList(context)
            Log.d("Contacts", contacts.joinToString(separator = "\n"))
            // Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
        } else {
            requestContactsPermission(activity, rootView)
            Toast.makeText(context, "Permission needed!", Toast.LENGTH_SHORT).show()
        }
        return contacts

    }




    private fun requestContactsPermission(activity: MainActivity, rootView: View) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Log.d("Contacts", contacts.joinToString(separator = "\n"))
            Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.READ_CONTACTS
                )
            ) {
                Snackbar.make(
                    rootView,
                    "The app needs permission to access your contacts.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("OK") {
                    ActivityCompat.requestPermissions(
                        activity, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS
                    )
                }.show()
            } else {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS
                )
            }
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


}
package com.sarrm.android.utils

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract


class CheckNumberContacts {

    companion object {


        fun isFromContacts(context: Context, number: String): Boolean {

            var res: String? = null
            try {
                val resolver = context.contentResolver
                val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
                val c = resolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)

                if (c != null) {
                    if (c.moveToFirst()) {
                        res = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    }
                    if (!c.isClosed)
                        c.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return res != null
        }

        fun getNameForNumber(context: Context, number: String): String? {

            var res: String? = null
            try {
                val resolver = context.contentResolver
                val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
                val c = resolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)

                if (c != null) {
                    if (c.moveToFirst()) {
                        res = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    }
                    if (!c.isClosed)
                        c.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return res
        }

        fun getPhotoURIForNumber(context: Context, number: String): String? {

            var res: String? = null
            try {
                val resolver = context.contentResolver
                val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
                val c = resolver.query(uri, arrayOf(ContactsContract.PhoneLookup.PHOTO_URI), null, null, null)

                if (c != null) {
                    if (c.moveToFirst()) {
                        res = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                    }
                    if (!c.isClosed)
                        c.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return res
        }

    }
}
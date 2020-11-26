package com.android.sarrm.watchers

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

class PhoneCallDetector(context: Context) : PhoneStateListener() {
    private var ongoingCall = false

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                ongoingCall = true
            }
        }
    }
}

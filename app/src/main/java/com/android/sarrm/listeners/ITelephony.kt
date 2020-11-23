package com.android.sarrm.listeners

interface ITelephony {

    fun endCall(): Boolean

    fun answerRingingCall()

    fun silenceRinger()
}
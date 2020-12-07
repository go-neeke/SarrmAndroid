package com.android.sarrm.listener

interface ITelephony {

    fun endCall(): Boolean

    fun answerRingingCall()

    fun silenceRinger()
}
package com.sarrm.android.listener

interface ITelephony {

    fun endCall(): Boolean

    fun answerRingingCall()

    fun silenceRinger()
}
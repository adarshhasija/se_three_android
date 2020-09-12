package com.starsearth.three.managers

import android.content.Context
import android.os.Bundle
import android.util.Log

import com.google.firebase.analytics.FirebaseAnalytics
import com.starsearth.three.BuildConfig
import com.starsearth.three.application.StarsEarthApplication

class AnalyticsManager(private val mContext: Context) {

    private var firebaseAnalytics: FirebaseAnalytics? = null
    companion object {
        var BACK_PRESSED = "back_button_pressed"
        var HOME_BUTTON_TAPPED = "home_button_tapped"
    }



    init {
        updateAnalytics()
    }

    fun remoteConfigUpdated() {
        updateAnalytics()
    }

    private fun updateAnalytics() {
        if (!BuildConfig.DEBUG) {
            val remoteConfigAnalytics = (mContext as StarsEarthApplication).remoteConfigAnalytics
            if (remoteConfigAnalytics.equals("all", ignoreCase = true)) {
                if (firebaseAnalytics == null) {
                    initializeFirebaseAnalytics()
                }
            } else if (remoteConfigAnalytics.equals("firebase", ignoreCase = true)) {
                if (firebaseAnalytics == null) {
                    initializeFirebaseAnalytics()
                }
            } else {
                firebaseAnalytics = null
            }
        }
    }

    private fun initializeFirebaseAnalytics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(mContext)
    }

    fun logActionEvent(eventName: String, bundle: Bundle) {
        firebaseAnalytics?.logEvent(eventName, bundle)
    }

    fun updateUserAnalyticsInfo(userId: String) {
        updateAnalyticsUserId(userId)
        //updateUserProperties()
    }

    fun updateAnalyticsUserId(userId: String) {
        firebaseAnalytics?.setUserId(userId)
    }



    fun sendAnalyticsForSaveChatTapped(logSize: Int) {
        val bundle = Bundle()
        bundle.putInt("log_size", logSize)
        logActionEvent("se3_save_chat_tapped", bundle)
    }

    fun sendAnalyticsForClearChatTapped(logSize: Int) {
        val bundle = Bundle()
        bundle.putInt("log_size", logSize)
        logActionEvent("se3_clear_chat_tapped", bundle)
    }

    fun sendAnalyticsForClearChatYesTapped(logSize: Int) {
        val bundle = Bundle()
        bundle.putInt("log_size", logSize)
        logActionEvent("se3_clear_chat_yes", bundle)
    }

    fun sendAnalyticsForTalkTapped() {
        val bundle = Bundle()
        logActionEvent("se3_talk_tapped", bundle)
    }

    fun sendAnalyticsForTypeTapped() {
        val bundle = Bundle()
        logActionEvent("se3_typing_tapped", bundle)
    }

    fun sendAnalyticsTypingDoneTapped() {
        val bundle = Bundle()
        logActionEvent("se3_typing_done_tapped", bundle)
    }

    fun sendAnalyticsTypingTickTapped() {
        val bundle = Bundle()
        logActionEvent("se3_typing_tick_tapped", bundle)
    }

    fun sendAnalyticsForTypingCancelled(reason : String) {
        val bundle = Bundle()
        bundle.putString("reason", reason)
        logActionEvent("se3_typing_cancelled", bundle)
    }

    fun sendAnalyticsForTalkingCancelled(reason : String) {
        val bundle = Bundle()
        bundle.putString("reason", reason)
        logActionEvent("se3_talking_cancelled", bundle)
    }

    fun sendAnalyticsForTimerFinished() {
        val bundle = Bundle()
        logActionEvent("se3_timer_finished", bundle)
    }

    fun sendAnalyticsForRowSelection() {
        val bundle = Bundle()
        logActionEvent("se3_row_selected", bundle)
    }

    fun sendAnalyticsForCameraReturn(text : String) {
        val bundle = Bundle()
        bundle.putString("text", text.subSequence(0, if (text.length >= 20) { 20 } else { text.length -1 } ).toString())
        logActionEvent("se3_android_cam_ret", bundle)
    }

    fun sendAnalyticsForAction(action: String) {
        val bundle = Bundle()
        bundle.putString("state", action)
        logActionEvent("se3_android_swipe_up", bundle)
    }
}

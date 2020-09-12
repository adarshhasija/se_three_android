package com.starsearth.three.application

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.starsearth.three.domain.FirebaseRemoteConfigWrapper
import com.starsearth.three.managers.AnalyticsManager
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by faimac on 11/28/16.
 */
class StarsEarthApplication : Application() {
    var firebaseRemoteConfigWrapper: FirebaseRemoteConfigWrapper? = null
        private set
    var analyticsManager: AnalyticsManager? = null
        private set
    var textToSpeech: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        firebaseRemoteConfigWrapper = FirebaseRemoteConfigWrapper(applicationContext)
        analyticsManager = AnalyticsManager(applicationContext)

        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val ttsLang = textToSpeech?.setLanguage(Locale.US)

                if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language is not supported!")
                } else {
                    Log.i("TTS", "Language Supported.")
                }
                Log.i("TTS", "Initialization success.")
            } else {
                Toast.makeText(applicationContext, "TTS Initialization failed!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    val remoteConfigAnalytics: String
        get() = firebaseRemoteConfigWrapper!!["analytics"]

    fun sayThis(text : String?) {
        val speechStatus = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "se3")
        if (speechStatus == TextToSpeech.ERROR) {
            Toast.makeText(applicationContext, getString(com.starsearth.three.R.string.error_saying_text), Toast.LENGTH_SHORT).show()
        }
    }

    fun vibrate(context: Context, type: String) {
        var timeMillis : Long = 0
        if (type == "MC_DOT" || type == "RESULT_SUCCESS" || type == "RESULT_FAILURE") {
            timeMillis = 50
        }
        else if (type == "MC_DASH") {
            timeMillis = 750
        }
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v!!.vibrate(VibrationEffect.createOneShot(timeMillis, VibrationEffect.DEFAULT_AMPLITUDE))
            if (type == "RESULT_SUCCESS" || type == "RESULT_FAILURE") {
                Timer("SecondVibration", false).schedule(timeMillis + 100) {
                    v.vibrate(VibrationEffect.createOneShot(timeMillis, VibrationEffect.DEFAULT_AMPLITUDE))
                }

                if (type == "RESULT_FAILURE") {
                    //Three short vibrations for failure
                    Timer("ThirdVibration", false).schedule(timeMillis + (timeMillis + 100) + 100) {
                        v.vibrate(VibrationEffect.createOneShot(timeMillis, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            }
        } else {
            //deprecated in API 26
            v!!.vibrate(100)
        }
    }
}
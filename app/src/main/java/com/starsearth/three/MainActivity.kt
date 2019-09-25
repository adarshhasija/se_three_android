package com.starsearth.three

import android.Manifest
import android.content.Context
import android.os.Bundle
import com.starsearth.three.fragments.ChatModeFragment
import com.starsearth.three.fragments.TypingFragment
import android.view.inputmethod.InputMethodManager
import com.starsearth.three.fragments.TalkingFragment
import android.content.DialogInterface
import com.starsearth.three.domain.ChatListItem
import com.starsearth.three.fragments.lists.ChatListItemFragment
import java.text.SimpleDateFormat
import java.util.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import android.R.attr.data
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.starsearth.three.application.StarsEarthApplication
import com.starsearth.three.managers.AnalyticsManager
import kotlinx.android.synthetic.main.fragment_chat_mode.*


class MainActivity : AppCompatActivity(),
                    ChatModeFragment.OnChatModeFragmentInteractionListener,
                    TalkingFragment.OnFragmentInteractionListener,
                    ChatListItemFragment.OnChatListFragmentInteractionListener,
                    TypingFragment.OnTypingFragmentInteractionListener {

    override fun checkPermissionsForSpeechToText() {
        checkPermission()
    }

    override fun onChatLogStarted() {
        val chatModeFragment = supportFragmentManager?.findFragmentByTag(ChatModeFragment.TAG)
        (chatModeFragment as? ChatModeFragment)?.chatLogStarted()
    }

    override fun onListFragmentInteraction(chatListItem: ChatListItem) {
        (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForRowSelection()

        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audio.getStreamVolume(AudioManager.STREAM_SYSTEM)
        val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)

        if (currentVolume < (maxVolume/2)) {
            dialogOK(getString(com.starsearth.three.R.string.error), getString(com.starsearth.three.R.string.volume_low_error))
        }
        val speechStatus = textToSpeech?.speak(chatListItem.message, TextToSpeech.QUEUE_FLUSH, null)
        if (speechStatus == TextToSpeech.ERROR) {
            dialogOK(getString(com.starsearth.three.R.string.error), getString(com.starsearth.three.R.string.error_saying_text))
        }
    }


    override fun displayErrorDialog(title: String, message: String) {
        dialogOK(title, message)
    }

    override fun onTalkingCompleted(finalText: String) {
        val dateFormat = SimpleDateFormat("h:mm a")
        val chatListItem = ChatListItem(finalText, dateFormat.format(Date()).toString(), "", "talking")
        val chatModeFragment = supportFragmentManager?.findFragmentByTag(ChatModeFragment.TAG)
        (chatModeFragment as? ChatModeFragment)?.addChatListItem(chatListItem)
        supportFragmentManager?.popBackStack()
    }

    override fun speakingCancelled() {
        supportFragmentManager?.popBackStack()
    }

    override fun onDoneTapped(finalText: String) {
        val view = this.currentFocus
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)

        supportFragmentManager?.popBackStack()

        val dateFormat = SimpleDateFormat("h:mm a")
        val chatListItem = ChatListItem(finalText, dateFormat.format(Date()).toString(), "", "typing")
        val chatModeFragment = supportFragmentManager?.findFragmentByTag(ChatModeFragment.TAG)
        (chatModeFragment as? ChatModeFragment)?.addChatListItem(chatListItem)

        val speechStatus = textToSpeech?.speak(finalText, TextToSpeech.QUEUE_FLUSH, null)
        if (speechStatus == TextToSpeech.ERROR) {
            dialogOK(getString(com.starsearth.three.R.string.error), getString(com.starsearth.three.R.string.error_saying_text))
        }
    }

    override fun onTypeButtonTapped() {
        val typingFragment : Fragment = TypingFragment.newInstance("","")
        openNewFragment(typingFragment, TypingFragment.TAG)
    }

    override fun onTalkButtonTapped() {
        val talkingFragment : Fragment = TalkingFragment.newInstance("","")
        openNewFragment(talkingFragment, TalkingFragment.TAG)
    }

    private fun openNewFragment(fragment: Fragment, tag: String) {
        getSupportFragmentManager()?.beginTransaction()
            ?.replace(R.id.fragment_container_main, fragment, tag)
            ?.addToBackStack(tag)
            ?.commit()
    }

    private fun dialogOK(title: String, message: String) {
        AlertDialog.Builder(this@MainActivity)
            .setTitle(title)
            .setMessage(message)

            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                // Continue with delete operation
                dialog.cancel()
            })
            .show()
    }

    private var textToSpeech: TextToSpeech? = null
    //private lateinit var mChatModeFragment : ChatModeFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        val chatModeFragment = ChatModeFragment.newInstance("","")
        getSupportFragmentManager()?.beginTransaction()
            ?.replace(R.id.fragment_container_main, chatModeFragment, ChatModeFragment.TAG)
            ?.addToBackStack(ChatModeFragment.TAG)
            ?.commit()
    }

    override fun onBackPressed() {
        val fragmentCount = supportFragmentManager.fragments.size
        if (fragmentCount > 0) {
            val lastFragment = supportFragmentManager?.fragments?.get(fragmentCount - 1)
            if (lastFragment is TypingFragment) {
                (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTypingCancelled(AnalyticsManager.BACK_PRESSED)
            }
            if (lastFragment is TalkingFragment) {
                (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTalkingCancelled(AnalyticsManager.BACK_PRESSED)
            }
        }
        else {
            finish()
        }

        super.onBackPressed()

    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        val fragmentsSize = supportFragmentManager.fragments.size
        val lastFragment = supportFragmentManager?.fragments?.get(fragmentsSize - 1)
        if (lastFragment is TypingFragment) {
            (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTypingCancelled(AnalyticsManager.HOME_BUTTON_TAPPED)
            supportFragmentManager.popBackStack() //only for typing and talking fragments
        }
        if (lastFragment is TalkingFragment) {
            (application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTalkingCancelled(AnalyticsManager.HOME_BUTTON_TAPPED)
            supportFragmentManager.popBackStack() //only for typing and talking fragments
        }

    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val requiredPermission = Manifest.permission.RECORD_AUDIO

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (PermissionChecker.checkCallingOrSelfPermission(this, requiredPermission) == PermissionChecker.PERMISSION_DENIED) {
                requestPermissions(arrayOf(requiredPermission), ChatModeFragment.REQUEST_AUDIO)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ChatModeFragment.REQUEST_AUDIO -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    val chatModeFragment = supportFragmentManager?.findFragmentByTag(ChatModeFragment.TAG)
                    (chatModeFragment as? ChatModeFragment)?.btnTalk?.setBackgroundColor(Color.BLUE)

                } else {
                    // permission denied, boo! Disable the button
                    val chatModeFragment = supportFragmentManager?.findFragmentByTag(ChatModeFragment.TAG)
                    (chatModeFragment as? ChatModeFragment)?.btnTalk?.setBackgroundColor(Color.GRAY)
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}

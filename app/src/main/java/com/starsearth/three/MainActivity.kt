package com.starsearth.three

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.starsearth.three.application.StarsEarthApplication
import com.starsearth.three.domain.Action
import com.starsearth.three.domain.ChatListItem
import com.starsearth.three.fragments.*
import com.starsearth.three.fragments.lists.ActionListFragment
import com.starsearth.three.fragments.lists.ChatListItemFragment
import com.starsearth.three.managers.AnalyticsManager
import kotlinx.android.synthetic.main.fragment_chat_mode.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(),
                    ChatModeFragment.OnChatModeFragmentInteractionListener,
                    TalkingFragment.OnFragmentInteractionListener,
                    ChatListItemFragment.OnChatListFragmentInteractionListener,
                    ActionsFragment.OnActionsFragmentInteractionListener,
                    ActionFragment.OnActionFragmentInteractionListener,
                    ActionListFragment.OnActionListFragmentInteractionListener,
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
        (application as? StarsEarthApplication)?.sayThis(chatListItem.message)
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
        imm.hideSoftInputFromWindow(view?.getWindowToken(), 0)

        supportFragmentManager?.popBackStack()

        val dateFormat = SimpleDateFormat("h:mm a")
        val chatListItem = ChatListItem(finalText, dateFormat.format(Date()).toString(), "", "typing")
        val chatModeFragment = supportFragmentManager?.findFragmentByTag(ChatModeFragment.TAG)
        (chatModeFragment as? ChatModeFragment)?.addChatListItem(chatListItem)

        (application as? StarsEarthApplication)?.sayThis(finalText)
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

    private fun dialogForManualEntry() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle("Enter Letters or Numbers")
        alertDialog.setMessage("No special characters")

        val input = EditText(this@MainActivity)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        alertDialog.setView(input)

        alertDialog.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
                val text = input.text.toString()//.uppercase()
            /*    if (text.length > 6) {
                    dialogOK("Text too long", "Must be 6 characters max")
                }
                else {  */
                    openActionFromActionsListScreenWithManualInput(text)
                //}
            })

        alertDialog.setNegativeButton("CANCEL",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        alertDialog.show()
    }

    private fun handSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            openActionFromActionScreenManualInput("MANUAL", it)
        }
    }

    private var textToSpeech: TextToSpeech? = null
    val CAMERA_ACTIVITY = 100
    //private lateinit var mChatModeFragment : ChatModeFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

     /*   textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
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
        })  */

        //val chatModeFragment = ChatModeFragment.newInstance("","")
        //val actionFragment = ActionFragment.newInstance()
        val actionListFragment = ActionListFragment.newInstance()
        getSupportFragmentManager()?.beginTransaction()
            ?.replace(R.id.fragment_container_main, actionListFragment, ActionListFragment.TAG)
            //?.addToBackStack(ActionsFragment.TAG)
            ?.commit()

        //to activate the sharing capability, uncomment the intent filter under MainActivity in Manifest file
        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                handSendText(intent)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if ((application as? StarsEarthApplication)?.textToSpeech?.isSpeaking == true) {
            (application as? StarsEarthApplication)?.textToSpeech?.stop()
        }
    }

  /*  override fun onBackPressed() {
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

    }   */

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                val extras = data?.extras
                val cameraText = extras?.getString("text")
                //val actionsFragment = supportFragmentManager.findFragmentByTag(ActionsFragment.TAG)
                //(actionsFragment as? ActionsFragment)?.cameraResultReceived(cameraText)
                //(applicationContext as? StarsEarthApplication)?.sayThis(cameraText)
                (applicationContext as? StarsEarthApplication)?.vibrate(
                    this,
                    "RESULT_SUCCESS"
                )
                if (cameraText.isNullOrEmpty() == false) {
                    val actionFragment : Fragment = ActionFragment.newInstance("MANUAL", cameraText)
                    openNewFragment(actionFragment, ActionFragment.TAG)
                }
                else {
                    dialogOK("Error","Nothing received from cammera. Please try again")
                }
                //val actionsFragment : Fragment = ActionsFragment.newInstance(cameraText) //This is old
                //openNewFragment(actionsFragment, ActionsFragment.TAG)
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

    override fun openCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, CAMERA_ACTIVITY)
    }

    fun openCameraActivityFromActionsList(mode: Action.Companion.ROW_TYPE) {
        val bundle = Bundle()
        bundle.putSerializable(Action.Companion.ROW_TYPE.ROW_TYPE_KEY.toString(), mode)
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtras(bundle)
        startActivityForResult(intent, CAMERA_ACTIVITY)
    }

    override fun openAction(alphanimeric: String) {
        val actionsFragment : Fragment = ActionsFragment.newInstance(alphanimeric)
        openNewFragment(actionsFragment, ActionsFragment.TAG)
    }

    override fun openFromActionScreen(screen: String) {
        //Currently only being used to open chat mode
        val chatModeFragment = ChatModeFragment.newInstance("","")
        openNewFragment(chatModeFragment, ChatModeFragment.TAG)
    }

    override fun openActionFromActionScreen(action: String) {
        if (action == "TIME" || action == "BATTERY_LEVEL") {
            val actionFragment = ActionFragment.newInstance(action)
            openNewFragment(actionFragment, ActionFragment.TAG)
        }
        else if (action == "DATE") {
            val calendar = Calendar.getInstance()
            val date = calendar[Calendar.DATE]
            val weekday_name: String = SimpleDateFormat("EEEE", Locale.ENGLISH).format(System.currentTimeMillis())
            val final = date.toString() + " " + weekday_name //We want to say the full weekday name
            openActionFromActionsListScreenWithManualInput(final)
        }
        else if (action == "CAMERA") {
            val intent = Intent(this, CameraActivity::class.java)
            startActivityForResult(intent, CAMERA_ACTIVITY)
        }
        else if (action == "CHAT_MODE") {
            val chatModeFragment = ChatModeFragment.newInstance("","")
            openNewFragment(chatModeFragment, ChatModeFragment.TAG)
        }
        else if (action == "SETTINGS") {
            val settingsFragment = SettingsFragment.newInstance()
            openNewFragment(settingsFragment, SettingsFragment.TAG)
        }
    }

    override fun openActionFromActionScreenManualInput(action: String, inputText: String) {
        val re = Regex("[^A-Za-z0-9\n ]")
        var filteredInputText = re.replace(inputText, "")
        filteredInputText = filteredInputText.replace("\\s+".toRegex(), " ") //multiple spaces between charaacters
        filteredInputText = filteredInputText.replace("\\n+".toRegex(), " ") //newlines.
        //filteredInputText = filteredInputText.replace(" ", "␣", true)
        val actionFragment = ActionFragment.newInstance(action, filteredInputText)
        openNewFragment(actionFragment, ActionFragment.TAG)
    }

    override fun openDialogForManualEntryFromActionFragment() {
        dialogForManualEntry()
    }

    override fun fromActionFragmentFullTextButtonTapped(
        text: String,
        startIndexForHighlighting: Int,
        endIndexForHighlighting: Int
    ) {
        val fullTextFragment = FullTextFragment.newInstance(text, startIndexForHighlighting, endIndexForHighlighting)
        openNewFragment(fullTextFragment, FullTextFragment.TAG)
    }

    override fun onActionListItemInteraction(action: Action) {
        if (action.rowType == Action.Companion.ROW_TYPE.TIME_12HR) {
            //Analytics call is in ActionFragment
            openActionFromActionsListScreen("TIME")
        }
        else if (action.rowType == Action.Companion.ROW_TYPE.DATE) {
            //Analytics call is in ActionFragment
            openActionFromActionsListScreen("DATE")
        }
        else if (action.rowType == Action.Companion.ROW_TYPE.BATTERY_LEVEL) {
            //Analytics call is in ActionFragment
            openActionFromActionsListScreen("BATTERY_LEVEL")
        }
        else if (action.rowType == Action.Companion.ROW_TYPE.MANUAL) {
            //Analytics call is in ActionFragment
            dialogForManualEntry()
        }
        else if (action.rowType == Action.Companion.ROW_TYPE.CAMERA_OCR) {
            (application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                "action_CAMERA_OCR"
            )
            openCameraActivityFromActionsList(action.rowType)
        }
    }

    override fun openActionFromActionsListScreen(action: String) {
        openActionFromActionScreen(action) //using this function as it already exists. If the ActionFragment gets deleted, move the logic from 'openActionFromActionsScreen' to heres
    }

    override fun openActionFromActionsListScreenWithManualInput(inputText: String) {
        openActionFromActionScreenManualInput("MANUAL", inputText)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        //(supportFragmentManager?.fragments?.last() as? ActionFragment)?.autoPlay()
    }

}

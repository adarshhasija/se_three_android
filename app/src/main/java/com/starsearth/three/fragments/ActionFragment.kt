package com.starsearth.three.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.starsearth.three.R
import com.starsearth.three.application.StarsEarthApplication
import com.starsearth.three.domain.Action
import com.starsearth.three.domain.Braille
import com.starsearth.three.domain.MorseCode
import com.starsearth.three.utils.CustomVibrationPatternsUtils
import com.starsearth.two.listeners.SeOnTouchListener
import kotlinx.android.synthetic.main.fragment_action.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_INPUT_ACTION = "input-action"
private const val ARG_INPUT_TEXT = "input-text"

/**
 * A simple [Fragment] subclass.
 * Use the [ActionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActionFragment : Fragment(), SeOnTouchListener.OnSeTouchListenerInterface {
    // TODO: Rename and change types of parameters
    private lateinit var mContext : Context
    var mView : View? = null //When we return to this fragment from another fragment we are losing the data. So we are using this to store it
    private var mInputAction: Action.Companion.ROW_TYPE? = null
    private var mInputText : String? = null
    private val morseCode = MorseCode()
    private var mMorseCodeIndex = -1
    private var listener: OnActionFragmentInteractionListener? = null
    private var mInstructionsStringArray : ArrayList<String>? = arrayListOf()
    var brailleStringIndex = -1
    var isAutoPlayOn = false
    var alphanumericArrayIndex = -1 //in the case of braille
    var alphanumericArrayForBraille : ArrayList<String>? = ArrayList()
    var alphanumericFullStringSplitBySpaceAsArray : ArrayList<String> = ArrayList()
    var alphanumericFullStringSplitBySpaceAsArrayIndex : Int = -1
    var braille = Braille()
    var isBrailleSwitchedToHorizontal = false

    lateinit var mainHandler: Handler
    var isRunnablePosted = false

    private val updateTextTask = object : Runnable {
        override fun run() {
            if (mMorseCodeIndex >= tvMorseCode.text.length
                && alphanumericArrayIndex >= alphanumericArrayForBraille?.size?.minus(1) ?: 0
                && alphanumericFullStringSplitBySpaceAsArrayIndex >= (alphanumericFullStringSplitBySpaceAsArray.size - 1)) {
                stopAutoplay()
                return
            }
            if (braille.isMidpointReachedForNumber(tvMorseCode.text.length, brailleStringIndex)) {
                TimeUnit.MILLISECONDS.sleep(250)
            }
            goToNextCharacter()
            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getString(ARG_INPUT_ACTION)?.let {
                if (it == "TIME") {
                    mInputAction = Action.Companion.ROW_TYPE.TIME_12HR
                }
                else if (it == "DATE") {
                    mInputAction = Action.Companion.ROW_TYPE.DATE
                }
                else if (it == "BATTERY_LEVEL") {
                    mInputAction = Action.Companion.ROW_TYPE.BATTERY_LEVEL
                }
                else if (it == "MANUAL") {
                    mInputAction = Action.Companion.ROW_TYPE.MANUAL
                }
            }
            it.getString(ARG_INPUT_TEXT)?.let {
                mInputText = it
            }
            return
        }
        //Should only reach here if there is no input action
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            if (mView == null) {
                // view will be initialize for the first time .. you can out condition for that if data is not null then do not initialize view again.
                mView = inflater.inflate(R.layout.fragment_action, container, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Inflate the layout for this fragment
        return mView//inflater.inflate(R.layout.fragment_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnReplay?.setOnClickListener {
            isAutoPlayOn = !isAutoPlayOn
            if (isAutoPlayOn) {
                autoPlay()
            }
            else {
                stopAutoplay()
            }
        }
        btnNextCharacter?.setOnClickListener {
            if (mMorseCodeIndex < 0) mMorseCodeIndex = -1
            if (alphanumericArrayIndex < 0) alphanumericArrayIndex = 0
            if (alphanumericFullStringSplitBySpaceAsArrayIndex < 0) alphanumericFullStringSplitBySpaceAsArrayIndex = 0
            goToNextCharacter()
            if (mMorseCodeIndex >= 0) {
                btnReset?.visibility = View.VISIBLE
            }
            else {
                btnReset?.visibility = View.GONE
            }
        }
        btnSwitchReadDirection?.setOnClickListener {
            isBrailleSwitchedToHorizontal = !isBrailleSwitchedToHorizontal
            if (isBrailleSwitchedToHorizontal) {
                btnSwitchReadDirection?.text = "Read up down"
            }
            else {
                btnSwitchReadDirection?.text = "Read sideways"
            }
        }
        btnFullText?.setOnClickListener {
            var text = ""
            var startIndexForHighlighting = 0
            var endIndexForHighlighting = 0
            for (word in alphanumericFullStringSplitBySpaceAsArray) {
                text += word
                text += " "
            }
            text = text.trim()
            for (i in alphanumericFullStringSplitBySpaceAsArray.indices) {
                if (i < alphanumericFullStringSplitBySpaceAsArrayIndex) {
                    startIndexForHighlighting += alphanumericFullStringSplitBySpaceAsArray[i].length //Need to increment by length of  the word that was completed
                    startIndexForHighlighting++ //account for space after the word
                }
            }
            startIndexForHighlighting += alphanumericArrayIndex //account for exactly where we are in the word
            endIndexForHighlighting = startIndexForHighlighting + 1
            listener?.fromActionFragmentFullTextButtonTapped(text, startIndexForHighlighting, endIndexForHighlighting)
        }
        btnReset?.setOnClickListener {
            reset()
        }


        if (mInputAction == Action.Companion.ROW_TYPE.TIME_12HR) {
            //TIME
            (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                "action_TIME"
            )

            val dateFormat: DateFormat = SimpleDateFormat("hh:mm aa")
            val dateString: String = dateFormat.format(Date()).toString()
            tvAlphanumerics?.text = dateString
            val dotsDashesMap = CustomVibrationPatternsUtils.getCurrentTimeInDotsDashes()
            tvMorseCode?.text = dotsDashesMap.get("FINAL_STRING") as? String
            mInstructionsStringArray = dotsDashesMap.get("FINAL_INSTRUCTIONS") as? ArrayList<String>
        }
        else if (mInputAction == Action.Companion.ROW_TYPE.DATE) {
            //DATE
            (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                "action_DATE"
            )
            val calendar = Calendar.getInstance()
            val date = calendar[Calendar.DATE]
            val weekday_name: String = SimpleDateFormat("EEEE", Locale.ENGLISH).format(System.currentTimeMillis())
            //val final = date.toString() + weekday_name.toUpperCase().subSequence(0, 2)
            val final = date.toString() + " " + weekday_name.toUpperCase()
            tvAlphanumerics?.text = final
            val dotsDashesMap = CustomVibrationPatternsUtils.getDateAndDayInDotsDashes()
            tvMorseCode?.text = dotsDashesMap.get("FINAL_STRING") as? String
            mInstructionsStringArray = dotsDashesMap.get("FINAL_INSTRUCTIONS") as? ArrayList<String>
        }
        else if (mInputAction == Action.Companion.ROW_TYPE.BATTERY_LEVEL) {
            //BATTERY_LEVEL
            (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                "action_BATTERY"
            )
            val bm = context!!.getSystemService(BATTERY_SERVICE) as BatteryManager
            val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toString() + "%"
            tvAlphanumerics?.text = batLevel
            val dotsDashesMap = context?.let { CustomVibrationPatternsUtils.getBatteryLevelInDotsAndDashes(it) }
            tvMorseCode?.text = dotsDashesMap?.get("FINAL_STRING") as? String
            mInstructionsStringArray = dotsDashesMap?.get("FINAL_INSTRUCTIONS") as? ArrayList<String>
            if (tvMorseCode?.text?.isNullOrEmpty() == true) {
                tvAlphanumerics?.text = "There was an error. Please go back to the previous screen and reopen this screen"
                return
            }
        }
        else if (mInputAction == Action.Companion.ROW_TYPE.MANUAL) {
            (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                "action_MANUAL"
            )
            if (alphanumericFullStringSplitBySpaceAsArray.isEmpty()) {
                //We are not coming back from another fragment. First time setup
                val splitList = mInputText?.split("\\s".toRegex())?.toTypedArray()
                splitList?.let {
                    if (it.size > 1) btnFullText?.visibility = View.VISIBLE //Means its more than 1 word
                    for (item in it) alphanumericFullStringSplitBySpaceAsArray.add(item)
                    mInputText = alphanumericFullStringSplitBySpaceAsArray.first()
                    alphanumericArrayForBraille?.addAll(braille.convertAlphanumericToBraille(mInputText ?: "") ?: ArrayList())
                    tvAlphanumerics?.text = mInputText
                    tvMorseCode?.text = alphanumericArrayForBraille?.first()
                    mainHandler = Handler(Looper.getMainLooper())
                    autoPlay()
                }
            }
        }
        else {
            val instruction = "Are you trying to read the text on a door?\nSwipe up to open camera"
            tvInstructions?.text = instruction
            //view.contentDescription = instruction
        }

        view.setOnTouchListener(SeOnTouchListener(this))

        //TimeUnit.SECONDS.sleep(1);
        //if (tvMorseCode?.text?.isNullOrEmpty() == false) {
        //    autoPlay()
        //}
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("MORSE_CODE_INDEX", mMorseCodeIndex)
        outState.putInt("BRAILLE_STRING_INDEX", brailleStringIndex)
        outState.putInt("ALPHANUMBERIC_ARRAY_INDEX", alphanumericArrayIndex)
        outState.putInt("ALPHANUMERIC_FULL_STRING_ARRAY_INDEX", alphanumericFullStringSplitBySpaceAsArrayIndex)
        outState.putBoolean("BRAILLE_SWITCHED_TO_HORIZONTAL", isBrailleSwitchedToHorizontal)
        outState.putString("INPUT_TEXT", mInputText)
    }

    fun reset() {
        tvFashText?.text = ""
        mMorseCodeIndex = -1
        brailleStringIndex = -1
        alphanumericArrayIndex = -1
        alphanumericFullStringSplitBySpaceAsArrayIndex = -1
        mInputText = alphanumericFullStringSplitBySpaceAsArray[0]
        tvAlphanumerics?.text = mInputText
        //tvMorseCode reset
        alphanumericArrayForBraille?.clear()
        alphanumericArrayForBraille?.addAll(braille.convertAlphanumericToBraille(mInputText ?: "") ?: ArrayList())
        tvMorseCode?.text = alphanumericArrayForBraille?.get(0)
        //
        btnReset?.visibility = View.GONE
        btnReplay?.visibility = View.VISIBLE
        btnReplay?.text = "Replay"
        btnNextCharacter?.visibility = View.VISIBLE
        //btnSwitchReadDirection?.visibility = View.VISIBLE
        if (alphanumericFullStringSplitBySpaceAsArray.size > 1) btnFullText?.visibility = View.VISIBLE

        val text = tvMorseCode?.text.toString()
        val spannable: Spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvMorseCode.setText(spannable, TextView.BufferType.SPANNABLE)
        val textAlpha = tvAlphanumerics?.text.toString()
        val spannableAlpha: Spannable = SpannableString(textAlpha)
        spannableAlpha.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            textAlpha.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvAlphanumerics.setText(spannableAlpha, TextView.BufferType.SPANNABLE)
    }


    private fun flashVibrationDescription(text: String) {
        tvFashText?.text = text
        tvFashText?.alpha = 0f
        tvFashText?.visibility = View.VISIBLE

        //Only needed during autoplay
        tvFashText?.animate()
            ?.alpha(1f)
            ?.setDuration(400)
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    //tvFashText?.visibility = View.GONE
                }
            })
    }

    private fun flashAlphanumericLabelChange() {
        tvAlphanumerics?.alpha = 0f
        tvAlphanumerics?.visibility = View.VISIBLE

        //Only needed during autoplay
        tvAlphanumerics?.animate()
            ?.alpha(1f)
            ?.setDuration(250)
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    //tvMorseCode?.visibility = View.GONE
                }
            })
    }

    private fun flashBrailleGridChange() {
        tvMorseCode?.alpha = 0f
        tvMorseCode?.visibility = View.VISIBLE

        //Only needed during autoplay
        tvMorseCode?.animate()
            ?.alpha(1f)
            ?.setDuration(250)
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    //tvMorseCode?.visibility = View.GONE
                }
            })
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnActionFragmentInteractionListener) {
            mContext = context
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    fun setMorseCodeText(alphanimericStr: String) {
        var mcString = ""
        for (alphanumeric in alphanimericStr) {
            mcString += morseCode.alphabetToMCMap[alphanumeric.toString()] + "|"
        }
        tvMorseCode?.text = mcString
        tvMorseCode?.textSize = 20f
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_action, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_time) {
            listener?.openActionFromActionScreen("TIME")
            return true
        }
        if (id == R.id.action_date) {
            listener?.openActionFromActionScreen("DATE")
            return true
        }
        if (id == R.id.action_deaf) {
            listener?.openActionFromActionScreen("CHAT_MODE")
            return true;
        }

        return super.onOptionsItemSelected(item)
    }

    fun mcScroll() {
        val text = tvMorseCode.text
        if (mMorseCodeIndex < 0 || mMorseCodeIndex >= text.length) {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_SUCCESS"
            ) //Indicates there is nothing more here
            return
        }
        val spannable: Spannable = SpannableString(text)

        val morseCodeString = tvMorseCode.text
        val brailleStringIndex = braille.getNextIndexForBrailleTraversal(morseCodeString.length, mMorseCodeIndex, isBrailleSwitchedToHorizontal)
        if (brailleStringIndex == -1) {
            return
        }
        if (/*mMorseCodeIndex*/brailleStringIndex > -1) {
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                /*mMorseCodeIndex*/brailleStringIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (/*mMorseCodeIndex*/brailleStringIndex > -1 && /*mMorseCodeIndex*/brailleStringIndex < text.length) {
            spannable.setSpan(
                ForegroundColorSpan(Color.GREEN),
                /*mMorseCodeIndex*/brailleStringIndex,
                /*mMorseCodeIndex*/brailleStringIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (/*mMorseCodeIndex*/brailleStringIndex > -1 && /*mMorseCodeIndex*/brailleStringIndex + 1 < text.length) {
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                /*mMorseCodeIndex*/brailleStringIndex + 1,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvMorseCode.setText(spannable, TextView.BufferType.SPANNABLE)
        if (text[/*mMorseCodeIndex*/brailleStringIndex] == '.' || text[/*mMorseCodeIndex*/brailleStringIndex] == 'x') {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext, "MC_DOT")
        }
        else if (text[/*mMorseCodeIndex*/brailleStringIndex] == '-' || text[/*mMorseCodeIndex*/brailleStringIndex] == 'o') {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext, "MC_DASH")
        }
        else if (text[/*mMorseCodeIndex*/brailleStringIndex] == '|') {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_SUCCESS"
            )
        }
     /*   mInstructionsStringArray?.get(mMorseCodeIndex)?.let {
            flashVibrationDescription(it)
        }   */
        CustomVibrationPatternsUtils.getInfoTextForBraille(morseCodeString.toString(), brailleStringIndex)?.let {
            flashVibrationDescription(it)
        }

        //Highlighting alphanumeric portion
        if (mInputAction == Action.Companion.ROW_TYPE.TIME_12HR
            || mInputAction == Action.Companion.ROW_TYPE.DATE
            || mInputAction == Action.Companion.ROW_TYPE.BATTERY_LEVEL) {
            //If its TIME or DATE or BATTERY LEVEL, it is custom vibrations. No need to highlight alphanumerics
            return
        }

     /*   var numberOfPipes = 0
        var index = 0
        while (index < mMorseCodeIndex) {
            if (text[index] == '|') numberOfPipes++
            index++
        }   */

        val spannableAlphanumeric: Spannable = SpannableString(tvAlphanumerics.text)

        spannableAlphanumeric.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            /*numberOfPipes*/alphanumericArrayIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (/*numberOfPipes*/alphanumericArrayIndex < tvAlphanumerics.text.length) {
            spannableAlphanumeric.setSpan(
                ForegroundColorSpan(Color.GREEN),
                /*numberOfPipes*/alphanumericArrayIndex,
                /*numberOfPipes*/alphanumericArrayIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (/*numberOfPipes*/alphanumericArrayIndex + 1 < tvAlphanumerics.text.length) {
            spannableAlphanumeric.setSpan(
                ForegroundColorSpan(Color.BLACK),
                /*numberOfPipes*/alphanumericArrayIndex + 1,
                tvAlphanumerics.text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvAlphanumerics.setText(spannableAlphanumeric, TextView.BufferType.SPANNABLE)
    }

    interface OnActionFragmentInteractionListener {
        // TODO: Update argument type and name
        fun openActionFromActionScreen(action: String)
        fun openActionFromActionScreenManualInput(action: String, inputText: String)
        fun openDialogForManualEntryFromActionFragment()
        fun fromActionFragmentFullTextButtonTapped(text: String, startIndexForHighlighting: Int, endIndexForHighlighting: Int)
    }

    companion object {
        val TAG = "ACTION_FRAGMENT"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ActionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = ActionFragment()

        @JvmStatic
        fun newInstance(inputAction: String) =
            ActionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_INPUT_ACTION, inputAction)
                }
            }

        @JvmStatic
        fun newInstance(inputAction: String, inputText: String) =
            ActionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_INPUT_ACTION, inputAction)
                    putString(ARG_INPUT_TEXT, inputText)
                }
            }
    }

    override fun gestureTap() {
        //if (tvAlphanumerics?.text?.isEmpty() == false) {
        //    if (mInputAction == Action.Companion.ROW_TYPE.DATE) {
        //        val calendar = Calendar.getInstance()
        //        val date = calendar[Calendar.DATE]
        //        val weekday_name: String = SimpleDateFormat("EEEE", Locale.ENGLISH).format(System.currentTimeMillis())
        //        val final = date.toString() + " " + weekday_name //We want to say the full weekday name
        //        (mContext.applicationContext as? StarsEarthApplication)?.sayThis(final)
        //    }
        //    else {
        //        (mContext.applicationContext as? StarsEarthApplication)?.sayThis(tvAlphanumerics?.text?.toString())
        //    }
        //}
    }

    override fun gestureSwipeUp() {
      /*  if (mInputAction == null) {
            //That means we are in camera mode and user can swipe up to call the camera
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_SUCCESS"
            )
            (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                "action_CAMERA"
            )
            listener?.openActionFromActionScreen("CAMERA")
        }   */
    }

    override fun gestureSwipeLeft() {
      /*  if (mInputAction != null) {
            //This should only be allowed when reading morse code

            if (tvAlphanumerics?.text?.isEmpty() == false && tvMorseCode.text.isNullOrBlank() == false) {
                if (mMorseCodeIndex >= 0) mMorseCodeIndex--
                mcScroll()
            }
            else {
                (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                    mContext,
                    "RESULT_FAILURE"
                )
            }
        }   */

    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTextTask)
        isRunnablePosted = false
    }

    override fun onResume() {
        super.onResume()
        if (isRunnablePosted == false) {
            mainHandler.post(updateTextTask) //This waas commmented out as it was causing a bug
            isRunnablePosted = true
        }
    }

    fun autoPlay() {
        alphanumericFullStringSplitBySpaceAsArrayIndex = 0
        alphanumericArrayIndex = 0
        mMorseCodeIndex = -1
        brailleStringIndex = -1
        tvMorseCode?.text = alphanumericArrayForBraille?.first()
        isAutoPlayOn = true
        btnReplay?.visibility = View.VISIBLE
        btnReplay?.text = "Stop Replay"
        btnReset?.visibility = View.GONE
        btnNextCharacter?.visibility = View.GONE
        btnSwitchReadDirection?.visibility = View.GONE
        btnFullText?.visibility = View.GONE
        mainHandler.post(updateTextTask)
        isRunnablePosted = true
    }

    fun stopAutoplay() {
        isAutoPlayOn = false
        mainHandler.removeCallbacks(updateTextTask)
        isRunnablePosted = false
        reset()
    }

    fun goToNextCharacter() {
        if (mInputAction != null) {
            //This should only be allowed when reading morse code
            //We have to be in reading mode
            //we have to have morse code text
            if (tvAlphanumerics.text.isNullOrBlank() == false && tvMorseCode.text.isNullOrBlank() == false) {
                if (mMorseCodeIndex < 0)  {
                    mMorseCodeIndex = 0
                }
                else if (mMorseCodeIndex < tvMorseCode.text.length) {
                    mMorseCodeIndex++
                }
                else if (mMorseCodeIndex >= tvMorseCode.text.length) {
                    if (alphanumericArrayIndex < alphanumericArrayForBraille?.size?.minus(1) ?: 0) {
                        //move to next letter
                        alphanumericArrayIndex++
                        tvMorseCode?.text = alphanumericArrayForBraille?.get(alphanumericArrayIndex)
                        flashBrailleGridChange()
                        mMorseCodeIndex = 0
                    }
                    else if (alphanumericFullStringSplitBySpaceAsArrayIndex < (alphanumericFullStringSplitBySpaceAsArray.size - 1)) {
                        //we have reached the end of the word
                        //move to the next word
                        alphanumericFullStringSplitBySpaceAsArrayIndex++
                        mInputText = alphanumericFullStringSplitBySpaceAsArray[alphanumericFullStringSplitBySpaceAsArrayIndex]
                        alphanumericArrayIndex = 0
                        alphanumericArrayForBraille?.clear()
                        alphanumericArrayForBraille?.addAll(braille.convertAlphanumericToBraille(mInputText ?: "") ?: ArrayList())
                        tvAlphanumerics?.text = mInputText
                        tvMorseCode?.text = alphanumericArrayForBraille?.get(alphanumericArrayIndex)
                        flashAlphanumericLabelChange()
                        flashBrailleGridChange()
                        mMorseCodeIndex = 0
                    }
                }
                mcScroll()
            }
            else {
                (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                    mContext,
                    "RESULT_FAILURE"
                )
            }
        }
    }

    override fun gestureSwipeRight() {
        //goToNextCharacter()
    }

    override fun gestureSwipeLeft2Fingers() {

    }

    override fun gestureSwipeRight2Fingers() {

    }

    override fun gestureLongPress() {

    }
}
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
    private var listener: OnActionFragmentInteractionListener? = null
    private var mInstructionsStringArray : ArrayList<String>? = arrayListOf()
    var isAutoPlayOn = false
    //private var mMorseCodeIndex = -1
    //var mArrayBrailleGridsForCharsInWord : ArrayList<BrailleCell> = ArrayList()
    //var mArrayBrailleGridsForCharsInWordIndex = 0 //in the case of braille
    //var mAlphanumericHighlightStartIndex = 0 //Cannot use braille grids array index as thats not a 1-1 relation
    //var mArrayWordsInString : ArrayList<String> = ArrayList()
    //var mArrayWordsInStringIndex : Int = 0
    var TIME_DIFF_MILLIS : Long = 1000
    var braille = Braille()
    var isBrailleSwitchedToHorizontal = false

    lateinit var mainHandler: Handler
    var isRunnablePosted = false

    private val updateTextTask = object : Runnable {
        override fun run() {
            val morseCodeString = tvBraille.text
            val brailleStringIndex = braille.getNextIndexForBrailleTraversal(morseCodeString.length, braille.mIndex, isBrailleSwitchedToHorizontal)

            if (braille.isEndOfEntireTextReached(brailleStringIndex)) {
                pauseAutoPlayAndReset()
                return
            }
         /*   if (braille.isMidpointReachedForNumber(tvMorseCode.text.length, brailleStringIndex)) {
                TimeUnit.MILLISECONDS.sleep(250)
            }*/
            braille.mIndex++
            goToNextCharacter()
            mainHandler.postDelayed(this, TIME_DIFF_MILLIS)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPreviousCharacter?.setOnClickListener {
            braille.mIndex--
            if (braille.mIndex >= 0) {
                btnReset?.visibility = View.VISIBLE
            }
            else {
                btnReset?.visibility = View.GONE
            }
            goToPreviousCharacter()
        }
        btnPlayPause?.setOnClickListener {
            isAutoPlayOn = !isAutoPlayOn
            if (isAutoPlayOn) {
                setupForAutoPlay()
            }
            else {
                pauseAutoPlay()
            }
            playPauseButtonTappedUIChange()
        }
        btnNextCharacter?.setOnClickListener {
            braille.mIndex++
            if (braille.mIndex >= 0) {
                btnReset?.visibility = View.VISIBLE
            }
            else {
                btnReset?.visibility = View.GONE
            }
            goToNextCharacter()
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
            val map = braille.getStartAndEndIndexInFullStringOfHighlightedPortion()
            listener?.fromActionFragmentFullTextButtonTapped(map["text"] as String, map["start_index"] as Int, map["end_index"] as Int)
        }
        btnReset?.setOnClickListener {
            removeHighlighting()
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
            tvBraille?.text = dotsDashesMap.get("FINAL_STRING") as? String
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
            tvBraille?.text = dotsDashesMap.get("FINAL_STRING") as? String
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
            tvBraille?.text = dotsDashesMap?.get("FINAL_STRING") as? String
            mInstructionsStringArray = dotsDashesMap?.get("FINAL_INSTRUCTIONS") as? ArrayList<String>
            if (tvBraille?.text?.isNullOrEmpty() == true) {
                tvAlphanumerics?.text = "There was an error. Please go back to the previous screen and reopen this screen"
                return
            }
        }
        else if (mInputAction == Action.Companion.ROW_TYPE.MANUAL) {
            (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                "action_MANUAL"
            )
            if (braille.mArrayWordsInString.isEmpty()) {
                //We are not coming back from another fragment. First time setup
                val splitList = mInputText?.split("\\s".toRegex())?.toTypedArray()
                splitList?.let {
                    btnFullText?.visibility = View.VISIBLE //Means its more than 1 word
                    for (item in it) braille.mArrayWordsInString.add(item)
                    mInputText = braille.mArrayWordsInString.first()
                    braille.mArrayBrailleGridsForCharsInWord.addAll(braille.convertAlphanumericToBrailleWithContractions(mInputText ?: "") ?: ArrayList())
                    tvAlphanumerics?.text = mInputText
                    tvBraille?.text = braille.mArrayBrailleGridsForCharsInWord.first().brailleDots
                    mainHandler = Handler(Looper.getMainLooper())
                    //autoPlay()
                }
            }
        }
        else {
            val instruction = "Are you trying to read the text on a door?\nSwipe up to open camera"
            tvInstructions?.text = instruction
            //view.contentDescription = instruction
        }

        view.setOnTouchListener(SeOnTouchListener(this))

        val preferences = context!!.getSharedPreferences("SE_THREE", Context.MODE_PRIVATE)
        TIME_DIFF_MILLIS = preferences.getLong(CustomVibrationPatternsUtils.STRING_FOR_SHARED_PREFERENCES, 1000)

        //TimeUnit.SECONDS.sleep(1);
        //if (tvMorseCode?.text?.isNullOrEmpty() == false) {
        //    autoPlay()
        //}
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
                mInputText = it.trim()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("MORSE_CODE_INDEX", braille.mIndex)
        outState.putInt("ALPHANUMBERIC_ARRAY_INDEX", braille.mArrayBrailleGridsForCharsInWordIndex)
        outState.putInt("ALPHANUMERIC_FULL_STRING_ARRAY_INDEX", braille.mArrayWordsInStringIndex)
        outState.putBoolean("BRAILLE_SWITCHED_TO_HORIZONTAL", isBrailleSwitchedToHorizontal)
        outState.putString("INPUT_TEXT", mInputText)
    }

    fun reset() {
        braille.reset()
        mInputText = braille.mArrayWordsInString[0]
        tvAlphanumerics?.text = mInputText
        tvBraille?.text = braille.mArrayBrailleGridsForCharsInWord.first().brailleDots

        tvFashText?.text = ""
        btnReset?.visibility = View.GONE
        btnPlayPause?.visibility = View.VISIBLE
        btnPlayPause?.setImageResource(R.drawable.play_arrow_fill0_wght400_grad0_opsz48)
        btnPlayPause?.contentDescription = "Play Button"
        btnPreviousCharacter?.visibility = View.VISIBLE
        btnNextCharacter?.visibility = View.VISIBLE
        //btnSwitchReadDirection?.visibility = View.VISIBLE
        btnFullText?.visibility = View.VISIBLE //Wanted to remove the if condition but was craashing
        removeHighlighting()
    }

    private fun removeHighlighting() {
        val text = tvBraille?.text.toString()
        val spannable: Spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvBraille.setText(spannable, TextView.BufferType.SPANNABLE)
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
        tvBraille?.alpha = 0f
        tvBraille?.visibility = View.VISIBLE

        //Only needed during autoplay
        tvBraille?.animate()
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
        tvBraille?.text = mcString
        tvBraille?.textSize = 20f
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
    /*    if (mMorseCodeIndex < 0 || mMorseCodeIndex >= text.length) {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_SUCCESS"
            ) //Indicates there is nothing more here
            removeHighlighting()
            return
        }   */
        val morseCodeString = tvBraille.text
        val spannable: Spannable = SpannableString(morseCodeString)
        val brailleStringIndex = braille.getNextIndexForBrailleTraversal(morseCodeString.length, braille.mIndex, isBrailleSwitchedToHorizontal)
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
        if (/*mMorseCodeIndex*/brailleStringIndex > -1 && /*mMorseCodeIndex*/brailleStringIndex < morseCodeString.length) {
            spannable.setSpan(
                ForegroundColorSpan(Color.GREEN),
                /*mMorseCodeIndex*/brailleStringIndex,
                /*mMorseCodeIndex*/brailleStringIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (/*mMorseCodeIndex*/brailleStringIndex > -1 && /*mMorseCodeIndex*/brailleStringIndex + 1 < morseCodeString.length) {
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                /*mMorseCodeIndex*/brailleStringIndex + 1,
                morseCodeString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvBraille.setText(spannable, TextView.BufferType.SPANNABLE)
        if (morseCodeString[/*mMorseCodeIndex*/brailleStringIndex] == '.' || morseCodeString[/*mMorseCodeIndex*/brailleStringIndex] == 'x') {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext, "MC_DOT")
        }
        else if (morseCodeString[/*mMorseCodeIndex*/brailleStringIndex] == '-' || morseCodeString[/*mMorseCodeIndex*/brailleStringIndex] == 'o') {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext, "MC_DASH")
        }
        else if (morseCodeString[/*mMorseCodeIndex*/brailleStringIndex] == '|') {
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
            view?.announceForAccessibility(it)
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
        val startIndex = braille.mAlphanumericHighlightStartIndex
        val exactWord = braille.mArrayBrailleGridsForCharsInWord[braille.mArrayBrailleGridsForCharsInWordIndex].english
        val endIndex = startIndex + exactWord.length
        spannableAlphanumeric.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            /*numberOfPipes*/startIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (/*numberOfPipes*/startIndex < tvAlphanumerics.text.length) {
            spannableAlphanumeric.setSpan(
                ForegroundColorSpan(Color.GREEN),
                /*numberOfPipes*/startIndex,
                /*numberOfPipes*/endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (/*numberOfPipes*/endIndex < tvAlphanumerics.text.length) {
            spannableAlphanumeric.setSpan(
                ForegroundColorSpan(Color.BLACK),
                /*numberOfPipes*/endIndex,
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
        if (isRunnablePosted == false && isAutoPlayOn == true) {
            mainHandler.post(updateTextTask) //This was commented out as it was causing a bug
            isRunnablePosted = true
        }
    }

    fun playPauseButtonTappedUIChange() {
        if (isAutoPlayOn == true) {
            btnPlayPause?.setImageDrawable(context?.resources?.getDrawable(R.drawable.pause_fill0_wght400_grad0_opsz48))
            btnPlayPause?.contentDescription = "Pause Button"
            btnPreviousCharacter?.visibility = View.GONE
            btnNextCharacter?.visibility = View.GONE
            btnFullText?.visibility = View.GONE
            btnReset?.visibility = View.GONE
        }
        else {
            btnPlayPause?.setImageDrawable(context?.resources?.getDrawable(R.drawable.play_arrow_fill0_wght400_grad0_opsz48))
            btnPlayPause?.contentDescription = "Play Button"
            btnPreviousCharacter?.visibility = View.VISIBLE
            btnNextCharacter?.visibility = View.VISIBLE
            btnFullText?.visibility = View.VISIBLE
            btnReset?.visibility = View.GONE
        }
    }

    fun setupForAutoPlay() {
        if (braille.mIndex <= -1) {
            //Means that it is a fresh autoplay not a paused one. Reset indexes
            reset()
            tvBraille?.text = braille.mArrayBrailleGridsForCharsInWord.first().brailleDots
        }
        isAutoPlayOn = true
        btnPlayPause?.visibility = View.VISIBLE
        btnPlayPause?.setImageResource(R.drawable.pause_fill0_wght400_grad0_opsz48)
        btnPlayPause?.contentDescription = "Pause Button"
        btnReset?.visibility = View.GONE
        btnPreviousCharacter?.visibility = View.GONE
        btnNextCharacter?.visibility = View.GONE
        btnSwitchReadDirection?.visibility = View.GONE
        btnFullText?.visibility = View.GONE
        mainHandler.post(updateTextTask)
        isRunnablePosted = true
    }

    fun pauseAutoPlay() {
        isAutoPlayOn = false
        mainHandler.removeCallbacks(updateTextTask)
        isRunnablePosted = false
    }

    fun pauseAutoPlayAndReset() {
        pauseAutoPlay()
        reset()
    }

    fun goToPreviousCharacter() {
        val morseCodeString = tvBraille.text
        val brailleStringIndex = braille.getNextIndexForBrailleTraversal(morseCodeString.length, braille.mIndex, isBrailleSwitchedToHorizontal)
        if /*(mMorseCodeIndex >= (tvBraille.text.length - 1)
            && mArrayBrailleGridsForCharsInWordIndex >= (mArrayBrailleGridsForCharsInWord.size - 1)
            && mArrayWordsInStringIndex >= (mArrayWordsInString.size - 1))*/(braille.isEndOfEntireTextReached(brailleStringIndex)) {
            val alphanumericString = tvAlphanumerics.text.toString()
            val brailleString = tvBraille.text.toString()
            braille.setIndexesForEndOfStringEndOfBrailleGrid(alphanumericString, brailleString)
            //mMorseCodeIndex = braille.getIndexInStringOfLastCharacterInTheGrid(brailleString)
            //mArrayBrailleGridsForCharsInWordIndex = mArrayBrailleGridsForCharsInWord.size - 1
            //mArrayWordsInStringIndex = mArrayWordsInString.size - 1
            //val exactWord = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].english
            //mAlphanumericHighlightStartIndex = tvAlphanumerics.text.length - exactWord.length
        }
        else if /*(mMorseCodeIndex <= -1
            && mArrayBrailleGridsForCharsInWordIndex <= 0
            && mArrayWordsInStringIndex <= 0)*/(braille.isPositionBehindText()) {
            //Reached the very beginning of the string
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_SUCCESS"
            ) //Indicates there is nothing more here
            removeHighlighting()
            return
        }
        else if /*(mMorseCodeIndex <= -1
            && mArrayBrailleGridsForCharsInWordIndex <= 0)*/(braille.isStartOfWordReachedWhileMovingBack()) {
            //we have reached the end of the word
            //move to the previous word
            //mArrayWordsInStringIndex--
            //mInputText = mArrayWordsInString[mArrayWordsInStringIndex]
            //mArrayBrailleGridsForCharsInWord.clear()
            //mArrayBrailleGridsForCharsInWord.addAll(braille.convertAlphanumericToBrailleWithContractions(mInputText ?: "") ?: ArrayList())
            //mArrayBrailleGridsForCharsInWordIndex = mArrayBrailleGridsForCharsInWord.size - 1
            //tvAlphanumerics?.text = mInputText
            //tvBraille?.text = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].brailleDots
            //flashAlphanumericLabelChange()
            //flashBrailleGridChange()
            //val brailleString = tvBraille.text.toString()
            //mMorseCodeIndex = braille.getIndexInStringOfLastCharacterInTheGrid(brailleString)
            //val exactWord = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex].english
            //mAlphanumericHighlightStartIndex = (mInputText?.length ?: 0) - exactWord.length

            val brailleString = tvBraille.text.toString()
            mInputText = braille.goToPreviousWord(brailleString)
            tvAlphanumerics?.text = mInputText
            tvBraille?.text = braille.mArrayBrailleGridsForCharsInWord[braille.mArrayBrailleGridsForCharsInWordIndex].brailleDots
            flashAlphanumericLabelChange()
            flashBrailleGridChange()
        }
        else if (braille.mIndex <= -1) {
            //move to previous character
            braille.goToPreviousCharacter(tvBraille.text.toString())
            tvBraille?.text = braille.mArrayBrailleGridsForCharsInWord[braille.mArrayBrailleGridsForCharsInWordIndex].brailleDots
            flashBrailleGridChange()
        }
        mcScroll()
    }

    fun goToNextCharacter() {
        val brailleStringIndex = braille.getNextIndexForBrailleTraversal(tvBraille.text.length, braille.mIndex, isBrailleSwitchedToHorizontal)

        if (braille.isPositionBehindText())  {
            if (isAutoPlayOn == false) btnReset?.visibility = View.VISIBLE
            braille.resetIndexes()
        }
        else if /*(brailleStringIndex == -1
            && mArrayBrailleGridsForCharsInWordIndex >= (mArrayBrailleGridsForCharsInWord.size - 1)
            && mArrayWordsInStringIndex >= (mArrayWordsInString.size - 1))*/(braille.isEndOfEntireTextReached(brailleStringIndex)) {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_SUCCESS"
            ) //Indicates there is nothing more here
            removeHighlighting()
            return
        }
        else if /*(brailleStringIndex == -1
            && mArrayBrailleGridsForCharsInWordIndex >= (mArrayBrailleGridsForCharsInWord.size - 1))*/(braille.isEndOfWordReachedWhileMovingForward(brailleStringIndex)) {
            //we have reached the end of the word
            //move to the next word
            if (isAutoPlayOn == true) {
                TimeUnit.MILLISECONDS.sleep(TIME_DIFF_MILLIS/4 /*250*/)
            }
            //mArrayWordsInStringIndex++
            //mInputText = mArrayWordsInString[mArrayWordsInStringIndex]
            //mArrayBrailleGridsForCharsInWordIndex = 0
            //mArrayBrailleGridsForCharsInWord.clear()
            //mArrayBrailleGridsForCharsInWord.addAll(braille.convertAlphanumericToBrailleWithContractions(mInputText ?: "") ?: ArrayList())
            //tvAlphanumerics?.text = mInputText
            //tvBraille?.text = mArrayBrailleGridsForCharsInWord.get(mArrayBrailleGridsForCharsInWordIndex).brailleDots
            //flashAlphanumericLabelChange()
            //flashBrailleGridChange()
            //mMorseCodeIndex = 0
            //mAlphanumericHighlightStartIndex = 0

            mInputText = braille.goToNextWord()
            tvAlphanumerics?.text = mInputText
            tvBraille?.text = braille.mArrayBrailleGridsForCharsInWord.get(braille.mArrayBrailleGridsForCharsInWordIndex).brailleDots
            flashAlphanumericLabelChange()
            flashBrailleGridChange()
        }
        else if (brailleStringIndex == -1) {
            braille.goToNextCharacter()
            tvBraille?.text = braille.mArrayBrailleGridsForCharsInWord[braille.mArrayBrailleGridsForCharsInWordIndex].brailleDots
            flashBrailleGridChange()
        }
        mcScroll()
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
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
    var isAutoPlayOn = false
    var mArrayBrailleGridsForCharsInWord : ArrayList<String> = ArrayList()
    var mArrayBrailleGridsForCharsInWordIndex = 0 //in the case of braille
    var mArrayWordsInString : ArrayList<String> = ArrayList()
    var mArrayWordsInStringIndex : Int = 0
    var braille = Braille()
    var isBrailleSwitchedToHorizontal = false

    lateinit var mainHandler: Handler
    var isRunnablePosted = false

    private val updateTextTask = object : Runnable {
        override fun run() {
            val morseCodeString = tvMorseCode.text
            val brailleStringIndex = braille.getNextIndexForBrailleTraversal(morseCodeString.length, mMorseCodeIndex, isBrailleSwitchedToHorizontal)

            if (mMorseCodeIndex > 0 && brailleStringIndex == -1 //This combination means we are looking at the ending of the braille grid, not the start
                && mArrayBrailleGridsForCharsInWordIndex >= (mArrayBrailleGridsForCharsInWord.size - 1)
                && mArrayWordsInStringIndex >= (mArrayWordsInString.size - 1)) {
                pauseAutoPlayAndReset()
                return
            }
            if (braille.isMidpointReachedForNumber(tvMorseCode.text.length, brailleStringIndex)) {
                TimeUnit.MILLISECONDS.sleep(250)
            }
            mMorseCodeIndex++
            goToNextCharacter()
            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPreviousCharacter?.setOnClickListener {
            mMorseCodeIndex--
            if (mMorseCodeIndex >= 0) {
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
                autoPlay()
            }
            else {
                pauseAutoPlay()
            }
            playPauseButtonTappedUIChange()
        }
        btnNextCharacter?.setOnClickListener {
            mMorseCodeIndex++
            if (mMorseCodeIndex >= 0) {
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
            var text = ""
            var startIndexForHighlighting = 0
            var endIndexForHighlighting = 0
            for (word in mArrayWordsInString) {
                text += word
                text += " "
            }
            text = text.trim()
            for (i in mArrayWordsInString.indices) {
                if (i < mArrayWordsInStringIndex) {
                    startIndexForHighlighting += mArrayWordsInString[i].length //Need to increment by length of  the word that was completed
                    startIndexForHighlighting++ //account for space after the word
                }
            }
            startIndexForHighlighting += mArrayBrailleGridsForCharsInWordIndex //account for exactly where we are in the word
            endIndexForHighlighting = if (mMorseCodeIndex > -1) {
                //Means we have started traversing and there is something to highlight
                startIndexForHighlighting + 1
            }
            else {
                startIndexForHighlighting
            }
            listener?.fromActionFragmentFullTextButtonTapped(text, startIndexForHighlighting, endIndexForHighlighting)
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
            if (mArrayWordsInString.isEmpty()) {
                //We are not coming back from another fragment. First time setup
                val splitList = mInputText?.split("\\s".toRegex())?.toTypedArray()
                splitList?.let {
                    btnFullText?.visibility = View.VISIBLE //Means its more than 1 word
                    for (item in it) mArrayWordsInString.add(item)
                    mInputText = mArrayWordsInString.first()
                    mArrayBrailleGridsForCharsInWord.addAll(braille.convertAlphanumericToBraille(mInputText ?: "") ?: ArrayList())
                    tvAlphanumerics?.text = mInputText
                    tvMorseCode?.text = mArrayBrailleGridsForCharsInWord.first()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("MORSE_CODE_INDEX", mMorseCodeIndex)
        outState.putInt("ALPHANUMBERIC_ARRAY_INDEX", mArrayBrailleGridsForCharsInWordIndex)
        outState.putInt("ALPHANUMERIC_FULL_STRING_ARRAY_INDEX", mArrayWordsInStringIndex)
        outState.putBoolean("BRAILLE_SWITCHED_TO_HORIZONTAL", isBrailleSwitchedToHorizontal)
        outState.putString("INPUT_TEXT", mInputText)
    }

    fun reset() {
        tvFashText?.text = ""
        mMorseCodeIndex = -1
        mArrayBrailleGridsForCharsInWordIndex = 0
        mArrayWordsInStringIndex = 0
        mInputText = mArrayWordsInString[0]
        tvAlphanumerics?.text = mInputText
        //tvMorseCode reset
        mArrayBrailleGridsForCharsInWord.clear()
        mArrayBrailleGridsForCharsInWord.addAll(braille.convertAlphanumericToBraille(mInputText ?: "") ?: ArrayList())
        tvMorseCode?.text = mArrayBrailleGridsForCharsInWord.get(0)
        //
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
    /*    if (mMorseCodeIndex < 0 || mMorseCodeIndex >= text.length) {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_SUCCESS"
            ) //Indicates there is nothing more here
            removeHighlighting()
            return
        }   */
        val morseCodeString = tvMorseCode.text
        val spannable: Spannable = SpannableString(morseCodeString)
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

        tvMorseCode.setText(spannable, TextView.BufferType.SPANNABLE)
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
            /*numberOfPipes*/mArrayBrailleGridsForCharsInWordIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (/*numberOfPipes*/mArrayBrailleGridsForCharsInWordIndex < tvAlphanumerics.text.length) {
            spannableAlphanumeric.setSpan(
                ForegroundColorSpan(Color.GREEN),
                /*numberOfPipes*/mArrayBrailleGridsForCharsInWordIndex,
                /*numberOfPipes*/mArrayBrailleGridsForCharsInWordIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (/*numberOfPipes*/mArrayBrailleGridsForCharsInWordIndex + 1 < tvAlphanumerics.text.length) {
            spannableAlphanumeric.setSpan(
                ForegroundColorSpan(Color.BLACK),
                /*numberOfPipes*/mArrayBrailleGridsForCharsInWordIndex + 1,
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

    fun autoPlay() {
        if (mMorseCodeIndex <= -1) {
            //Means that it is a fresh autoplay not a paused one. Reset indexes
            mArrayWordsInStringIndex = 0
            mArrayBrailleGridsForCharsInWordIndex = 0
            mMorseCodeIndex = -1
            tvMorseCode?.text = mArrayBrailleGridsForCharsInWord?.first()
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
        if (tvAlphanumerics.text.isNullOrBlank() == false && tvMorseCode.text.isNullOrBlank() == false) {
            if (mMorseCodeIndex >= (tvMorseCode.text.length - 1)
                && mArrayBrailleGridsForCharsInWordIndex >= (mArrayBrailleGridsForCharsInWord.size - 1)
                && mArrayWordsInStringIndex >= (mArrayWordsInString.size - 1)) {
                val brailleString = tvMorseCode.text.toString()
                mMorseCodeIndex = braille.getIndexInStringOfLastCharacterInTheGrid(brailleString)
                mArrayBrailleGridsForCharsInWordIndex = mArrayBrailleGridsForCharsInWord.size - 1
                mArrayWordsInStringIndex = mArrayWordsInString.size - 1
            }
            else if (mMorseCodeIndex <= -1
                && mArrayBrailleGridsForCharsInWordIndex <= 0
                && mArrayWordsInStringIndex <= 0) {
                //Reached the very beginning of the string
                (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                    mContext,
                    "RESULT_SUCCESS"
                ) //Indicates there is nothing more here
                removeHighlighting()
                return
            }
            else if (mMorseCodeIndex <= -1
                && mArrayBrailleGridsForCharsInWordIndex <= 0) {
                //we have reached the end of the word
                //move to the previous word
                mArrayWordsInStringIndex--
                mInputText = mArrayWordsInString[mArrayWordsInStringIndex]
                mArrayBrailleGridsForCharsInWord.clear()
                mArrayBrailleGridsForCharsInWord.addAll(braille.convertAlphanumericToBraille(mInputText ?: "") ?: ArrayList())
                mArrayBrailleGridsForCharsInWordIndex = mArrayBrailleGridsForCharsInWord.size - 1
                tvAlphanumerics?.text = mInputText
                tvMorseCode?.text = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex]
                flashAlphanumericLabelChange()
                flashBrailleGridChange()
                val brailleString = tvMorseCode.text.toString()
                mMorseCodeIndex = braille.getIndexInStringOfLastCharacterInTheGrid(brailleString)
            }
            else if (mMorseCodeIndex <= -1) {
                //move to next letter
                mArrayBrailleGridsForCharsInWordIndex--
                tvMorseCode?.text = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex]
                flashBrailleGridChange()
                val brailleString = tvMorseCode.text.toString()
                mMorseCodeIndex = braille.getIndexInStringOfLastCharacterInTheGrid(brailleString)
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

    fun goToNextCharacter() {
        if (tvAlphanumerics.text.isNullOrBlank() == false && tvMorseCode.text.isNullOrBlank() == false) {
            val brailleStringIndex = braille.getNextIndexForBrailleTraversal(tvMorseCode.text.length, mMorseCodeIndex, isBrailleSwitchedToHorizontal)

            if (mMorseCodeIndex < 0
                && mArrayBrailleGridsForCharsInWordIndex <= 0
                && mArrayWordsInStringIndex <= 0)  {
                if (isAutoPlayOn == false) btnReset?.visibility = View.VISIBLE
                mMorseCodeIndex = 0
                mArrayBrailleGridsForCharsInWordIndex = 0
                mArrayWordsInStringIndex = 0
            }
            else if (brailleStringIndex == -1
                && mArrayBrailleGridsForCharsInWordIndex >= (mArrayBrailleGridsForCharsInWord.size - 1)
                && mArrayWordsInStringIndex >= (mArrayWordsInString.size - 1)) {
                (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                    mContext,
                    "RESULT_SUCCESS"
                ) //Indicates there is nothing more here
                removeHighlighting()
                return
            }
            else if (brailleStringIndex == -1
                && mArrayBrailleGridsForCharsInWordIndex >= (mArrayBrailleGridsForCharsInWord.size - 1)) {
                //we have reached the end of the word
                //move to the next word
                mArrayWordsInStringIndex++
                mInputText = mArrayWordsInString[mArrayWordsInStringIndex]
                mArrayBrailleGridsForCharsInWordIndex = 0
                mArrayBrailleGridsForCharsInWord.clear()
                mArrayBrailleGridsForCharsInWord.addAll(braille.convertAlphanumericToBraille(mInputText ?: "") ?: ArrayList())
                tvAlphanumerics?.text = mInputText
                tvMorseCode?.text = mArrayBrailleGridsForCharsInWord.get(mArrayBrailleGridsForCharsInWordIndex)
                flashAlphanumericLabelChange()
                flashBrailleGridChange()
                mMorseCodeIndex = 0
            }
            else if (brailleStringIndex == -1) {
                //move to next character
                mArrayBrailleGridsForCharsInWordIndex++
                tvMorseCode?.text = mArrayBrailleGridsForCharsInWord[mArrayBrailleGridsForCharsInWordIndex]
                flashBrailleGridChange()
                mMorseCodeIndex = 0
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
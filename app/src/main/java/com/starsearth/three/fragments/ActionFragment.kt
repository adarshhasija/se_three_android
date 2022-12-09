package com.starsearth.three.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.starsearth.three.R
import com.starsearth.three.application.StarsEarthApplication
import com.starsearth.three.domain.Action
import com.starsearth.three.domain.MorseCode
import com.starsearth.three.utils.CustomVibrationPatternsUtils
import com.starsearth.two.listeners.SeOnTouchListener
import kotlinx.android.synthetic.main.fragment_action.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_INPUT_ACTION = "input-action"

/**
 * A simple [Fragment] subclass.
 * Use the [ActionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActionFragment : Fragment(), SeOnTouchListener.OnSeTouchListenerInterface {
    // TODO: Rename and change types of parameters
    private lateinit var mContext : Context
    private var mInputAction: Action.Companion.ROW_TYPE? = null
    private val morseCode = MorseCode()
    private var mMorseCodeIndex = -1
    private var listener: OnActionFragmentInteractionListener? = null
    private var mInstructionsStringArray : ArrayList<String>? = arrayListOf()

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
                return
            }
        }
        //Should only reach here if there is no input action
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnNextCharacter?.setOnClickListener {
            goToNextCharacter()
            if (mMorseCodeIndex >= 0) {
                btnReset?.visibility = View.VISIBLE
            }
            else {
                btnReset?.visibility = View.GONE
            }
        }
        btnReset?.setOnClickListener {
            tvFashText?.text = ""
            mMorseCodeIndex = -1
            btnReset?.visibility = View.GONE

            val text = tvMorseCode?.text.toString()
            val spannable: Spannable = SpannableString(text)
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tvMorseCode.setText(spannable, TextView.BufferType.SPANNABLE)
            tvAlphanumerics?.setTextColor(Color.BLACK)
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

        if (mMorseCodeIndex > -1) {
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                mMorseCodeIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (mMorseCodeIndex > -1 && mMorseCodeIndex < text.length) {
            spannable.setSpan(
                ForegroundColorSpan(Color.GREEN),
                mMorseCodeIndex,
                mMorseCodeIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (mMorseCodeIndex > -1 && mMorseCodeIndex + 1 < text.length) {
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                mMorseCodeIndex + 1,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvMorseCode.setText(spannable, TextView.BufferType.SPANNABLE)
        if (text[mMorseCodeIndex] == '.') {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext, "MC_DOT")
        }
        else if (text[mMorseCodeIndex] == '-') {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext, "MC_DASH")
        }
        else if (text[mMorseCodeIndex] == '|') {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_SUCCESS"
            )
        }
        mInstructionsStringArray?.get(mMorseCodeIndex)?.let {
            flashVibrationDescription(it)
        }

        //Highlighting alphanumeric portion
        if (mInputAction == Action.Companion.ROW_TYPE.TIME_12HR
            || mInputAction == Action.Companion.ROW_TYPE.DATE
            || mInputAction == Action.Companion.ROW_TYPE.BATTERY_LEVEL) {
            //If its TIME or DATE or BATTERY LEVEL, it is custom vibrations. No need to highlight alphanumerics
            return
        }

        var numberOfPipes = 0
        var index = 0
        while (index < mMorseCodeIndex) {
            if (text[index] == '|') numberOfPipes++
            index++
        }

        val spannableAlphanumeric: Spannable = SpannableString(tvAlphanumerics.text)

        spannableAlphanumeric.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            numberOfPipes,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (numberOfPipes < tvAlphanumerics.text.length) {
            spannableAlphanumeric.setSpan(
                ForegroundColorSpan(Color.GREEN),
                numberOfPipes,
                numberOfPipes + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (numberOfPipes + 1 < tvAlphanumerics.text.length) {
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                numberOfPipes + 1,
                tvAlphanumerics.text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvAlphanumerics.setText(spannableAlphanumeric, TextView.BufferType.SPANNABLE)
    }

    interface OnActionFragmentInteractionListener {
        // TODO: Update argument type and name
        fun openActionFromActionScreen(action: String)
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

    fun autoPlay() {
        while (mMorseCodeIndex < tvMorseCode.text.length) {
            goToNextCharacter()
            TimeUnit.SECONDS.sleep(1);
            //Timer("SettingUp", false).schedule(5000) {
            //    goToNextCharacter
            //}
            //Increment if index is occuring in encosed function
        }
    }

    fun goToNextCharacter() {
        if (mInputAction != null) {
            //This should only be allowed when reading morse code
            //We have to be in reading mode
            //we have to have morse code text
            if (tvAlphanumerics.text.isNullOrBlank() == false && tvMorseCode.text.isNullOrBlank() == false) {
                if (mMorseCodeIndex < tvMorseCode.text.length) mMorseCodeIndex++
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
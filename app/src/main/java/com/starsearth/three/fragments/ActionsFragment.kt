package com.starsearth.three.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.starsearth.three.R
import com.starsearth.three.application.StarsEarthApplication
import com.starsearth.three.domain.MorseCode
import com.starsearth.two.listeners.SeOnTouchListener
import kotlinx.android.synthetic.main.fragment_actions.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ActionsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ActionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActionsFragment : Fragment(), SeOnTouchListener.OnSeTouchListenerInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mContext: Context
    private val morseCode = MorseCode()
    private var mMorseCodeIndex = -1
    private var listener: OnActionsFragmentInteractionListener? = null

    val startingInstruction = "Tap 1 time and swipe up to get TIME\nTap 2 times and swipe up to get DATE\nTap 3 times and swipe up to open camera"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvInstructions?.text = startingInstruction
        view.contentDescription = startingInstruction
        view.setOnTouchListener(SeOnTouchListener(this))
    }

    fun cameraResultReceived(text: String?) {
        if (text?.isEmpty() == false) {
            (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForCameraReturn(
                text
            )
            (mContext.applicationContext as? StarsEarthApplication)?.sayThis(text)
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_SUCCESS"
            )
            tvAlphanumerics?.text = text
            tvMorseCode?.text = ""
        /*    val p: Pattern = Pattern.compile("a-zA-Z0-9 ")
            val m: Matcher = p.matcher(text)
            setMorseCodeText(
                if (m.matches() && text.length <= 6) {
                    text
                } else {
                    ""
                }
            )   */
            tvMorseCode?.textSize = 20f
            val str = "Swipe left to reset"
            tvInstructions?.text = str
            view?.contentDescription = text + "\n" + str
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnActionsFragmentInteractionListener) {
            mContext = context
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnActionsFragmentInteractionListener {
        // TODO: Update argument type and name
        fun openCameraActivity()
    }

    companion object {
        val TAG = "ACTIONS_FRAG"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CameraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActionsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun gestureTap() {
        if (tvAlphanumerics?.text?.isEmpty() == false) {
            (mContext.applicationContext as? StarsEarthApplication)?.sayThis(tvAlphanumerics?.text?.toString())
        }
        else {
            (mContext.applicationContext as? StarsEarthApplication)?.textToSpeech?.stop()
            var currentMorseCodeText = tvMorseCode.text.toString()
            currentMorseCodeText += "."
            tvMorseCode?.text = currentMorseCodeText
            val difference = 3 - currentMorseCodeText.length
            if (currentMorseCodeText.length < 1) {
                tvInstructions?.text = startingInstruction
                view?.contentDescription = startingInstruction
            }
            else if (currentMorseCodeText.length == 1) {
                val str = "Swipe up to get TIME" +
                        "\n\n" + "Tap one more time to get DATE" +
                        "\n\n" + "Tap " + difference + " times and swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }
            else if (currentMorseCodeText.length == 2) {
                val str = "Swipe up to get DATE" +
                        "\n\n" + "Tap " + difference + " time and swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }
            else {
                val str = "Swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }
        /*    if (difference > 0) {
                val str = "Tap " + difference + " times and swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }
            else {
                val str = "Swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }   */

            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext, "MC_DOT")
        }
    }

    fun setMorseCodeText(alphanimeric: String) {
        var mcString = ""
        for (alphanumeric in alphanimeric) {
            mcString += morseCode.alphabetToMCMap[alphanumeric.toString()] + "|"
        }
        tvMorseCode?.text = mcString
        tvMorseCode?.textSize = 20f
    }

    override fun gestureSwipeUp() {
        if (tvAlphanumerics?.text?.isEmpty() == true) {
            var currentMorseCodeText = tvMorseCode.text.toString()
            if (currentMorseCodeText.length == 3) {
                (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                    mContext,
                    "RESULT_SUCCESS"
                )
                (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                    "action_CAMERA"
                )
                listener?.openCameraActivity()
            }
            else if (currentMorseCodeText.length == 2) {
                //DATE
                (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                    "action_DATE"
                )
                val calendar = Calendar.getInstance()
                val date = calendar[Calendar.DATE]
                val weekday_name: String = SimpleDateFormat("EEEE", Locale.ENGLISH).format(System.currentTimeMillis())
                val final = date.toString() + weekday_name.toUpperCase().subSequence(0, 2)
                tvAlphanumerics?.text = final
                view?.contentDescription = final
                setMorseCodeText(final)

                val str = "Swipe right to scroll through the morse code\n\nOR\n\nSwipe left to reset"
                tvInstructions?.text = str
                view?.contentDescription = final + "\n" + str
            }
            else {
                //TIME
                (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction(
                    "action_TIME"
                )
                val calendar = Calendar.getInstance()
                val hr = calendar[Calendar.HOUR_OF_DAY].toString()
                val min = calendar[Calendar.MINUTE].toString()
                val final = hr + min
                tvAlphanumerics?.text = final
                view?.contentDescription = final
                setMorseCodeText(final)

                val str = "Swipe right to scroll through the morse code\n\nOR\n\nSwipe left to reset"
                tvInstructions?.text = str
                view?.contentDescription = final + "\n" + str
            }

         /*   val difference = 3 - currentMorseCodeText.length
            if (difference > 0) {
                val str = "You need to tap " + difference + " more times"
                tvInstructions?.text = str
                view?.contentDescription = str //for talkback
                (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext,"RESULT_FAILURE")
            }
            else {
                (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext,"RESULT_SUCCESS")
                (activity?.application as? StarsEarthApplication)?.analyticsManager?.sendAnalyticsForAction("action_CAMERA")
                listener?.openCameraActivity()
            }   */
        }
        else {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(
                mContext,
                "RESULT_FAILURE"
            )
            (mContext.applicationContext as? StarsEarthApplication)?.sayThis(tvInstructions?.text?.toString())
        }
    }

    override fun gestureSwipeLeft() {
        (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext, "RESULT_SUCCESS")
        if (tvAlphanumerics?.text?.isEmpty() == false) {
            mMorseCodeIndex = -1
            tvAlphanumerics?.text = ""
            tvMorseCode?.text = ""
            tvMorseCode?.textSize = 40f
            tvInstructions?.text = startingInstruction
            view?.contentDescription = startingInstruction
            (mContext.applicationContext as? StarsEarthApplication)?.sayThis(startingInstruction)
        }
        else {
            var currentMorseCodeText = tvMorseCode.text.toString()
            currentMorseCodeText = currentMorseCodeText.dropLast(1)
            tvMorseCode?.text = currentMorseCodeText
            val difference = 3 - currentMorseCodeText.length
            if (currentMorseCodeText.length >= 3) {
                val str = "Swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }
            else if (currentMorseCodeText.length == 2) {
                val str = "Swipe up to get DATE" +
                        "\n\n" + "Tap " + difference + " time and swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }
            else if (currentMorseCodeText.length == 1) {
                val str = "Swipe up to get TIME" +
                        "\n\n" + "Tap one more time to get DATE" +
                        "\n\n" + "Tap " + difference + " times and swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }
            else {
                tvInstructions?.text = startingInstruction
                view?.contentDescription = startingInstruction
            }

         /*   if (difference > 0) {
                val str = "Tap " + difference + " times and swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
                (mContext.applicationContext as? StarsEarthApplication)?.sayThis(str)
            }
            else {
                val str = "Swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
                (mContext.applicationContext as? StarsEarthApplication)?.sayThis(str)
            }   */
        }

    }

    override fun gestureSwipeRight() {
        mMorseCodeIndex++
        mcScroll()
    }

    override fun gestureLongPress() {
        mMorseCodeIndex++
        mcScroll()
    }

    override fun gestureSwipeLeft2Fingers() {
        mMorseCodeIndex--
        mcScroll()
    }

    override fun gestureSwipeRight2Fingers() {
        mMorseCodeIndex++
        mcScroll()
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
}

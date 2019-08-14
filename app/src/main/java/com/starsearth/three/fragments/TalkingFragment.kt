package com.starsearth.three.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

import java.util.*
import kotlinx.android.synthetic.main.fragment_talking.*
import android.graphics.Color
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.starsearth.three.application.StarsEarthApplication


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TalkingFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TalkingFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class TalkingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var mSpeechRecognizer: SpeechRecognizer? = null
    private lateinit var mSpeechRecognizerIntent : Intent
    private var mCountDownTimer: CountDownTimer? = null
    private var timeTakenMillis : Long = 0
    protected var mSpeechRecognizerStartListeningTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault())

        activity?.getWindow()?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.starsearth.three.R.layout.fragment_talking, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSpeechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(v: Float) {

            }

            override fun onBufferReceived(bytes: ByteArray) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(i: Int) {
                val duration = System.currentTimeMillis() - mSpeechRecognizerStartListeningTime
                if (duration < 500 && i === SpeechRecognizer.ERROR_NO_MATCH) {
                    Log.w(
                        TAG,
                        "Doesn't seem like the system tried to listen at all. duration = " + duration + "ms. This might be a bug with onError and startListening methods of SpeechRecognizer"
                    )
                    Log.w(TAG, "Going to ignore the error")
                    return
                }
            }

            override fun onResults(bundle: Bundle) {
                //getting all the matches
                val matches = bundle
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                //displaying the first match
                if (matches != null)
                    tvMain?.text = matches[0]
                    submitSpokenText()
            }

            override fun onPartialResults(bundle: Bundle) {

            }

            override fun onEvent(i: Int, bundle: Bundle) {

            }
        })

     /*   clMain?.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {

                }

                MotionEvent.ACTION_DOWN -> {
                    mSpeechRecognizer?.stopListening()
                    submitSpokenText()
                }
            }
            false
        })  */

        setupTimer(61000,1000)
        //Listener starts automatically
        mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTalkingFragmentInteractionListener")
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.getWindow()?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mCountDownTimer?.cancel() //This is so that onFinish does not trigger causing an error
    }

    private fun submitSpokenText() {
        mCountDownTimer?.cancel()
        val finalText : String? = tvMain?.text?.trim().toString()
        finalText?.let {
            if (it.length > 0) {
                listener?.onTalkingCompleted(finalText)
                return
            }
            else {
                listener?.speakingCancelled()
                return
            }
        }
        listener?.speakingCancelled()
    }

    private fun setupTimer(duration: Long, interval: Long) {
        mCountDownTimer = object : CountDownTimer(duration, interval) {

            override fun onTick(millisUntilFinished: Long) {
                timeTakenMillis = 61000 - millisUntilFinished
                if (millisUntilFinished / 1000 < 11) {
                    tvTimer?.setTextColor(Color.RED)
                }

                if (millisUntilFinished / 1000 < 10) {
                    tvTimer?.setText((millisUntilFinished / 1000 / 60).toString() + ":0" + millisUntilFinished / 1000)
                } else {
                    val mins = (millisUntilFinished / 1000).toInt() / 60
                    val seconds = (millisUntilFinished / 1000).toInt() % 60
                    tvTimer?.setText(mins.toString() + ":" + if (seconds == 0) "00" else seconds) //If seconds are 0, print double 0, else print seconds
                }


            }

            override fun onFinish() {
                (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTimerFinished()

                timeTakenMillis = timeTakenMillis + 1000 //take the last second into consideration
                submitSpokenText()
            }
        }.start()
    }

    override fun onDetach() {
        super.onDetach()
        mSpeechRecognizer?.stopListening()
        mSpeechRecognizer?.destroy()
        mCountDownTimer?.cancel()
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
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onTalkingCompleted(finalText: String)
        fun speakingCancelled()
    }

    companion object {
        val TAG = "TALKING_FRAGMENT"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TalkingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TalkingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

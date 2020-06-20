package com.starsearth.three.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.starsearth.three.R
import com.starsearth.three.application.StarsEarthApplication
import com.starsearth.two.listeners.SeOnTouchListener
import kotlinx.android.synthetic.main.fragment_actions.*


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
    private var listener: OnActionsFragmentInteractionListener? = null

    val startingInstruction = "Tap 3 times and swipe up to open camera"

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
            (mContext.applicationContext as? StarsEarthApplication)?.sayThis(text)
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext,"RESULT_SUCCESS")
            tvAlphanumerics?.text = text
            tvMorseCode?.text = ""
            tvMorseCode?.textSize = 20f
            tvBlindUsersTap?.visibility = View.VISIBLE
            tvInstructions?.text = "Swipe left to reset"
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
            if (difference > 0) {
                val str = "Tap " + difference + " times and swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }
            else {
                val str = "Swipe up to open camera"
                tvInstructions?.text = str
                view?.contentDescription = str
            }

            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext,"MC_DOT")
        }
    }

    override fun gestureSwipeUp() {
        var currentMorseCodeText = tvMorseCode.text.toString()
        val difference = 3 - currentMorseCodeText.length
        if (difference > 0) {
            val str = "You need to tap " + difference + " more times"
            tvInstructions?.text = str
            view?.contentDescription = str //for talkback
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext,"RESULT_FAILURE")
        }
        else {
            (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext,"RESULT_SUCCESS")
            listener?.openCameraActivity()
        }
    }

    override fun gestureSwipeLeft() {
        (mContext.applicationContext as? StarsEarthApplication)?.vibrate(mContext,"RESULT_SUCCESS")
        if (tvAlphanumerics?.text?.isEmpty() == false) {
            tvAlphanumerics?.text = ""
            tvMorseCode?.text = ""
            tvMorseCode?.textSize = 40f
            tvBlindUsersTap?.visibility = View.GONE
            tvInstructions?.text = startingInstruction
            view?.contentDescription = startingInstruction
            (mContext.applicationContext as? StarsEarthApplication)?.sayThis(startingInstruction)
        }
        else {
            var currentMorseCodeText = tvMorseCode.text.toString()
            currentMorseCodeText = currentMorseCodeText.dropLast(1)
            tvMorseCode?.text = currentMorseCodeText
            val difference = 3 - currentMorseCodeText.length
            if (difference > 0) {
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
            }
        }

    }

    override fun gestureLongPress() {

    }
}

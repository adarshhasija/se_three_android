package com.starsearth.three.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.starsearth.three.R
import com.starsearth.three.application.StarsEarthApplication
import com.starsearth.three.utils.CustomVibrationPatternsUtils
import kotlinx.android.synthetic.main.fragment_settings.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var TIME_DIFF_MILLIS : Long = -1

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
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnMinus?.setOnClickListener {
            if (TIME_DIFF_MILLIS <= 1000) {
                context?.let {
                    (it.applicationContext as? StarsEarthApplication)?.vibrate(it, "RESULT_FAILURE")
                }
                tvError?.visibility = View.VISIBLE
                view.announceForAccessibility(tvError.text)
                return@setOnClickListener
            }
            context?.let {
                (it.applicationContext as? StarsEarthApplication)?.vibrate(it, "MC_DOT")
            }
            tvError?.visibility = View.GONE
            TIME_DIFF_MILLIS -= 1000
            setTimeLabel()
            val preferences = context!!.getSharedPreferences("SE_THREE", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putLong(CustomVibrationPatternsUtils.STRING_FOR_SHARED_PREFERENCES, TIME_DIFF_MILLIS)
            editor.apply()
        }

        btnAdd?.setOnClickListener {
            context?.let {
                (it.applicationContext as? StarsEarthApplication)?.vibrate(it, "MC_DOT")
            }
            tvError?.visibility = View.GONE
            TIME_DIFF_MILLIS += 1000
            setTimeLabel()
            val preferences = context!!.getSharedPreferences("SE_THREE", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putLong(CustomVibrationPatternsUtils.STRING_FOR_SHARED_PREFERENCES, TIME_DIFF_MILLIS)
            editor.apply()
        }

        val preferences = context!!.getSharedPreferences("SE_THREE", Context.MODE_PRIVATE)
        TIME_DIFF_MILLIS = preferences.getLong(CustomVibrationPatternsUtils.STRING_FOR_SHARED_PREFERENCES, 1000)
        setTimeLabel()
        tvError?.visibility = View.GONE
    }

    private fun setTimeLabel() {
        val mins = ((TIME_DIFF_MILLIS/1000)/60)
        val secs = ((TIME_DIFF_MILLIS/1000)%60)
        val minsString = if (mins > 0) { mins.toString() + "m" } else { "" }
        val secsString = if (secs > 0) { secs.toString() + "s" } else { "" }
        val finalString = minsString + " " + secsString
        view?.announceForAccessibility(finalString)
        tvTime?.text = finalString
    }

    companion object {
        val TAG = "SETTINGS_FRAGMENT"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(/*param1: String, param2: String*/) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                   // putString(ARG_PARAM1, param1)
                   // putString(ARG_PARAM2, param2)
                }
            }
    }
}
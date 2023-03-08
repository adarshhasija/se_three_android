package com.starsearth.three.fragments

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
import com.starsearth.three.domain.Action
import kotlinx.android.synthetic.main.fragment_action.*
import kotlinx.android.synthetic.main.fragment_full_text.*

private const val ARG_INPUT_FULL_TEXT = "input-full-text"
private const val ARG_INPUT_START_INDEX = "input-start-index"
private const val ARG_INPUT_END_INDEX = "input-end-index"

class FullTextFragment : Fragment() {

    var mText: String = ""
    var mStartIndexForHighlighting : Int = 0
    var mEndIndexForHighlighting : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getString(ARG_INPUT_FULL_TEXT)?.let {
                mText = it
            }
            it.getInt(ARG_INPUT_START_INDEX)?.let {
                mStartIndexForHighlighting = it
            }
            it.getInt(ARG_INPUT_END_INDEX)?.let {
                mEndIndexForHighlighting = it
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_full_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mStartIndexForHighlighting < 0 || mStartIndexForHighlighting >= mText.length) {
            tvMain.text = mText
            return
        }

        val spannableAlphanumeric: Spannable = SpannableString(mText)
        spannableAlphanumeric.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            mStartIndexForHighlighting,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (mStartIndexForHighlighting < mText.length) {
            spannableAlphanumeric.setSpan(
                ForegroundColorSpan(Color.GREEN),
                mStartIndexForHighlighting,
                mEndIndexForHighlighting,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (mEndIndexForHighlighting + 1 < mText.length) {
            spannableAlphanumeric.setSpan(
                ForegroundColorSpan(Color.BLACK),
                mEndIndexForHighlighting + 1,
                mText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvMain.setText(spannableAlphanumeric, TextView.BufferType.SPANNABLE)
    }

    companion object {
        val TAG = "FULL_TEXT_FRAGMENT"
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
        fun newInstance() = FullTextFragment()

        @JvmStatic
        fun newInstance(text: String, startIndexForHighlighting: Int, endIndexForHighlighting: Int) =
            FullTextFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_INPUT_FULL_TEXT, text)
                    putInt(ARG_INPUT_START_INDEX, startIndexForHighlighting)
                    putInt(ARG_INPUT_END_INDEX, endIndexForHighlighting)
                }
            }

    }
}
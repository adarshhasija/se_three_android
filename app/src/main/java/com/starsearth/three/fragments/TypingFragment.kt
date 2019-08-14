package com.starsearth.three.fragments

import android.content.Context
import android.os.Bundle

import kotlinx.android.synthetic.main.fragment_typing.*
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.starsearth.three.R
import com.starsearth.three.application.StarsEarthApplication
import android.app.Activity
import androidx.core.content.ContextCompat.getSystemService




// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TypingFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TypingFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class TypingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnTypingFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.starsearth.three.R.layout.fragment_typing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnDone?.setOnClickListener {
            (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsTypingDoneTapped()
            submitTypedText()
        }


        etMain.postDelayed({
            etMain?.requestFocus()
            val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager!!.toggleSoftInputFromWindow(
                etMain.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, 0
            )
        }, 500)

    }

    override fun onPause() {
        super.onPause()

        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.getWindowToken(), 0);
    }

    private fun submitTypedText() {
        val finalText : String? = etMain?.text?.trim().toString()
        finalText?.let {
            if (it.length > 0) {
                listener?.onDoneTapped(it)
            }
        }

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTypingFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        //super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_typing_menu, menu);
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.getItemId()
        return if (id == R.id.actionBarDone) {
            (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsTypingTickTapped()
            submitTypedText()
            true
        } else super.onOptionsItemSelected(item)

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
    interface OnTypingFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onDoneTapped(finalText: String)
    }

    companion object {
        val TAG = "TYPING_FRAGMENT"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TypingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TypingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

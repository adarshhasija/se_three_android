package com.starsearth.three.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import kotlinx.android.synthetic.main.fragment_chat_mode.*
import android.net.ConnectivityManager
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.starsearth.three.application.StarsEarthApplication
import com.starsearth.three.domain.ChatListItem
import com.starsearth.three.fragments.lists.ChatListItemFragment
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ChatModeFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ChatModeFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ChatModeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnChatModeFragmentInteractionListener? = null
    private lateinit var mChatListItemFragment: ChatListItemFragment
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        mChatListItemFragment = ChatListItemFragment.newInstance(1)
        childFragmentManager.beginTransaction()
            .replace(com.starsearth.three.R.id.fragment_container_list, mChatListItemFragment, ChatListItemFragment.TAG)
            .commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.starsearth.three.R.layout.fragment_chat_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnType?.setOnClickListener {
            (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTypeTapped()

            listener?.onTypeButtonTapped()
        }
        btnTalk?.setOnClickListener {
            (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTalkTapped()

            val buttonDrawable = it.background
            val colorId = (buttonDrawable as ColorDrawable).color
            if (colorId == Color.BLUE) {
                context?.let {
                    if (isNetworkAvailable(it)) {
                        (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForTalkScreenOpened()
                        listener?.onTalkButtonTapped()
                        return@setOnClickListener
                    }
                    else {
                        listener?.displayErrorDialog(getString(com.starsearth.three.R.string.no_internet_connection), getString(com.starsearth.three.R.string.you_must_be_connected_to_internet))
                        return@setOnClickListener
                    }
                }

                //If something else happened
                listener?.displayErrorDialog(getString(com.starsearth.three.R.string.error), getString(com.starsearth.three.R.string.some_issue_occured))
            }
            else {
                listener?.checkPermissionsForSpeechToText()
            }

        }

        btnShare?.setOnClickListener {
            val chatLog = mChatListItemFragment.getChatLog()
            (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForSaveChatTapped(chatLog.size)

            val dateFormat = SimpleDateFormat("dd-MMMM-yyyy h:mm a")
            val dateString =  dateFormat.format(Date()).toString()
            var chatString = getString(com.starsearth.three.R.string.app_name) + " " + "Session" + " " + dateString + "\n"
            for (chatListItem in chatLog) {
                chatString += "\n" +
                                chatListItem.message + "\n" +
                        chatListItem.time + " " + chatListItem.origin + "\n" +
                        "\n"
            }

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, chatString)
                type = "text/plain"
            }
            startActivity(sendIntent)

        }
        btnClear?.setOnClickListener {
            val chatLog = mChatListItemFragment.getChatLog()
            (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForClearChatTapped(chatLog.size)
            AlertDialog.Builder(mContext)
                .setTitle(getString(com.starsearth.three.R.string.are_you_sure))
                .setMessage(com.starsearth.three.R.string.this_action_cannot_be_undone)


                .setNegativeButton(android.R.string.no, DialogInterface.OnClickListener { dialog, which ->
                    // Continue with delete operation
                    dialog.cancel()
                })
                .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                    (activity?.application as StarsEarthApplication)?.analyticsManager?.sendAnalyticsForClearChatYesTapped(chatLog.size)

                    val chatListItemFragment = childFragmentManager?.findFragmentByTag(ChatListItemFragment.TAG)
                    (chatListItemFragment as? ChatListItemFragment)?.clearList()
                    //rlStatus?.visibility = View.GONE
                    btnShare?.visibility = View.GONE
                    btnClear?.visibility = View.GONE
                })
                .show()

        }

        //At the start both should be gone
        //rlStatus?.visibility = View.GONE
        btnShare?.visibility = View.GONE
        btnClear?.visibility = View.GONE

        listener?.checkPermissionsForSpeechToText()
    }

    fun addChatListItem(chatListItem: ChatListItem) {
        val chatListItemFragment = childFragmentManager?.findFragmentByTag(ChatListItemFragment.TAG)
        (chatListItemFragment as? ChatListItemFragment)?.addChatListItem(chatListItem)
    }

    fun chatLogStarted() {
        //rlStatus?.visibility = View.VISIBLE
        //btnShare?.visibility = View.VISIBLE  //Will remain disabled until bug is resolved. Fragments destroyed when share intent is opened. onSaveInstance is not called
        btnClear?.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is OnChatModeFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnChatModeFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected
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
    interface OnChatModeFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onTypeButtonTapped()
        fun onTalkButtonTapped()
        fun displayErrorDialog(title: String, message: String)
        fun checkPermissionsForSpeechToText()
    }

    companion object {
        val TAG = "CHAT_MODE_FRAGMENT"
        val REQUEST_AUDIO = 101

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatModeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatModeFragment().apply {
               /* arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }   */
            }
    }
}

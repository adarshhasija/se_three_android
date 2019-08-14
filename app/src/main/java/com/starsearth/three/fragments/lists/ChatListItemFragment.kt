package com.starsearth.three.fragments.lists

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.starsearth.three.R
import com.starsearth.three.adapter.MyChatListItemRecyclerViewAdapter
import com.starsearth.three.domain.ChatListItem

import kotlinx.android.synthetic.main.fragment_chatlistitem_list.*
import kotlinx.android.synthetic.main.fragment_chatlistitem_list.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ChatListItemFragment.OnListFragmentInteractionListener] interface.
 */
class ChatListItemFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1

    private var listener: OnChatListFragmentInteractionListener? = null
    private var mValues : ArrayList<ChatListItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chatlistitem_list, container, false)

        // Set the adapter
        if (view.list is RecyclerView) {
            with(view) {
                view.list.layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                view.list.adapter = MyChatListItemRecyclerViewAdapter(mValues, listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.list.adapter?.let {
            if (it.itemCount > 0) {
                listener?.onChatLogStarted()
                list?.visibility = View.VISIBLE
                rlEmptyList?.visibility = View.GONE
            }
            else {
                list?.visibility = View.GONE
                rlEmptyList?.visibility = View.VISIBLE
            }
        }

    }

    fun getChatLog() : ArrayList<ChatListItem> {
        return mValues
    }

    fun addChatListItem(chatListItem: ChatListItem) {
        mValues.add(0, chatListItem)
    }

    fun clearList() {
        mValues.clear()
        //view?.list?.adapter?.notifyDataSetChanged()
        list?.visibility = View.GONE
        rlEmptyList?.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChatListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnChatListFragmentInteractionListener")
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
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnChatListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(chatListItem: ChatListItem)
        fun onChatLogStarted()
    }

    companion object {
        val TAG = "CHAT_LIST_ITEM_FRAGMENT"

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ChatListItemFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}

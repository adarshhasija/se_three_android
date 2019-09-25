package com.starsearth.three.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.starsearth.three.R
import com.starsearth.three.domain.ChatListItem
import com.starsearth.three.fragments.lists.ChatListItemFragment


import com.starsearth.three.fragments.lists.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyChatListItemRecyclerViewAdapter(
    private val mValues: ArrayList<ChatListItem>,
    private val mListener: ChatListItemFragment.OnChatListFragmentInteractionListener?
) : RecyclerView.Adapter<MyChatListItemRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as ChatListItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_chatlistitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        if (item.mode == "talking") {
            holder.mGuestView.findViewById<TextView>(R.id.content).text = item.message
            holder.mGuestView.findViewById<TextView>(R.id.tvTime).text = item.time
            holder.mGuestView.visibility = View.VISIBLE
        }
        else {
            holder.mHostView.findViewById<TextView>(R.id.content).text = item.message
            holder.mHostView.findViewById<TextView>(R.id.tvTime).text = item.time
            holder.mHostView.visibility = View.VISIBLE
        }


        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        //val mContentView: TextView
        //val mTimeView: TextView
        val mHostView: FrameLayout
        val mGuestView: FrameLayout

        init {
            //mContentView = mView.findViewById(R.id.content) as TextView
            //mTimeView = mView.findViewById(R.id.tvTime) as TextView
            mHostView = mView.findViewById(R.id.flHost) as FrameLayout
            mGuestView = mView.findViewById(R.id.flGuest) as FrameLayout
        }

        override fun toString(): String {
            return super.toString()// + " '" + mContentView.text + "'"
        }
    }
}

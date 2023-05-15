package com.starsearth.three.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.starsearth.three.R
import com.starsearth.three.domain.Content
import com.starsearth.three.fragments.lists.ActionListFragment

import com.starsearth.three.fragments.lists.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyActionRecyclerViewAdapter(
    private val values: List<Content>,
    private val mContext: Context,
    private val mListener: ActionListFragment.OnActionListFragmentInteractionListener
) : RecyclerView.Adapter<MyActionRecyclerViewAdapter.ViewHolder>(), Filterable {

    private var filteredList: List<Content> = values

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Content
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener.onActionListItemInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_action_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position]
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mView : View = view
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)

        override fun toString(): String {
            return super.toString() + " '" + tvTitle.text + "'." + " '" + tvDescription.text + "'"
        }
    }

    fun updateData(newData : ArrayList<Content>) {
        filteredList = newData
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString().lowercase()
                filteredList = if (query.isEmpty()) {
                    values
                } else {
                    values.filter { item ->
                        item.title.lowercase().contains(query)  ||
                                item.description?.lowercase()?.contains(query) == true ||
                        item.rowType.toString().lowercase().contains(query) ||
                        stringIsContainedInArray(item.tags, query)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filteredList = results?.values as? List<Content> ?: values
                notifyDataSetChanged()
            }
        }
    }

    fun stringIsContainedInArray(arrayList: ArrayList<String>, string: String): Boolean {
        val words = string.split(" ")

        for (word in words) {
            //calling this function is required.
            //eg: string = "nu", and words array contains "nursery", "rhymes", "nu" wont be contained in the array.
            //Only if "nursery" is typed will it be contained in the array
            //So need to call other function and iterate over the other array, "nursery" and "rhymes"
            //Only then will we get "nu" as part of "nursery"
            if (/*arrayList.contains(word)*/arrayContainsSubstring(arrayList, word)) {
                return true
            }
        }
        return false
    }

    fun arrayContainsSubstring(array: ArrayList<String>, substring: String): Boolean {
        for (element in array) {
            if (element.contains(substring)) {
                return true
            }
        }
        return false
    }


}
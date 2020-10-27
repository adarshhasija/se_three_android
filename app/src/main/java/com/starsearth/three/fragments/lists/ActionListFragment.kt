package com.starsearth.three.fragments.lists

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import com.starsearth.three.R
import com.starsearth.three.adapter.MyActionRecyclerViewAdapter
import com.starsearth.three.domain.Action
import com.starsearth.three.domain.ChatListItem
import kotlinx.android.synthetic.main.fragment_action_list_list.view.*

/**
 * A fragment representing a list of Items.
 */
class ActionListFragment : Fragment() {

    private var columnCount = 1
    private lateinit var mContext: Context
    private lateinit var mListener: OnActionListFragmentInteractionListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_action_list_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.list.addItemDecoration(
                    DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL)
                )

                val actionList = ArrayList<Action>()
                val action1 = Action("Text from Camera", "Are you trying to read the text on a door? Use this option to open the camera", Action.Companion.ROW_TYPE.CAMERA_OCR)
                actionList.add(action1)
                adapter = MyActionRecyclerViewAdapter(actionList, mContext, mListener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnActionListFragmentInteractionListener) {
            this.mContext = context
            this.mListener = context
        }
        else {
            throw RuntimeException(context.toString() + " must implement OnActionListFragmentInteractionListener")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_action, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_time) {
            mListener?.openActionFromActionsListScreen("TIME")
            return true
        }
        if (id == R.id.action_date) {
            mListener?.openActionFromActionsListScreen("DATE")
            return true
        }
        if (id == R.id.action_deaf) {
            mListener?.openActionFromActionsListScreen("CHAT_MODE")
            return true;
        }

        return super.onOptionsItemSelected(item)
    }

    interface OnActionListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onActionListItemInteraction(action: Action)
        fun openActionFromActionsListScreen(action: String)
    }



    companion object {

        const val TAG = "ACTION_LIST_FRAG"
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance() =
            ActionListFragment()
    }
}
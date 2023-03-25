package com.starsearth.three.fragments.lists

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import com.starsearth.three.BuildConfig
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
                val action1 = Action("Time", "12 hour format", Action.Companion.ROW_TYPE.TIME_12HR)
                actionList.add(action1)
                val action2 = Action("Date", "Date and day of the week", Action.Companion.ROW_TYPE.DATE)
                actionList.add(action2)
                val action3 = Action("Battery Level", "Of this device as a percentage", Action.Companion.ROW_TYPE.BATTERY_LEVEL)
                actionList.add(action3)
                val action4 = Action("Manual", "Enter letters or numbers are we will convert it into braille and play it as vibrations", Action.Companion.ROW_TYPE.MANUAL)
                actionList.add(action4)
                val action5 = Action("Camera", "Want to read a sign like a flat number? Point the camera at a sign. We will read it and convert it into vibrations for you.", Action.Companion.ROW_TYPE.CAMERA_OCR)
              //  actionList.add(action5)
            /*    if (BuildConfig.DEBUG) {
                    //Only for internal testing right now
                    var action5 = Action("Search with Camera", "Are you trying to find something? Use this option", Action.Companion.ROW_TYPE.CAMERA_OBJECT_DETECTION)
                    actionList.add(action5)
                } */
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
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_action_list, menu) //Uncomment this line if you need the actions here. As of now we dont need it in the overflow menu as we are putting it in the main list
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
        if (id == R.id.action_settings) {
            mListener?.openActionFromActionsListScreen("SETTINGS")
            return true;
        }

        return super.onOptionsItemSelected(item)
    }

    interface OnActionListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onActionListItemInteraction(action: Action)
        fun openActionFromActionsListScreen(action: String)
        fun openActionFromActionsListScreenWithManualInput(inputText: String)
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
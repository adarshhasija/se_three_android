package com.starsearth.three.fragments.lists

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.starsearth.three.R
import com.starsearth.three.adapter.MyActionRecyclerViewAdapter
import com.starsearth.three.domain.Content
import kotlinx.android.synthetic.main.fragment_action_list_list.*
import kotlinx.android.synthetic.main.fragment_action_list_list.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream


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
        if (view.list is RecyclerView) {
            with(view.list) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.list.addItemDecoration(
                    DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL)
                )

                val contentList = ArrayList<Content>()
                val content1 = Content("Time", "12 hour format", arrayListOf("others"), Content.Companion.ROW_TYPE.TIME_12HR)
                contentList.add(content1)
                val content2 = Content("Date", "Date and day of the week", arrayListOf("others"), Content.Companion.ROW_TYPE.DATE)
                contentList.add(content2)
                val content3 = Content("Battery Level", "Of this device as a percentage", arrayListOf("others"), Content.Companion.ROW_TYPE.BATTERY_LEVEL)
                contentList.add(content3)
            /*    val content4 = Content("Manual", "Enter letters or numbers are we will convert it into braille and play it as vibrations", Content.Companion.ROW_TYPE.MANUAL)
                contentList.add(content4)
                val action5 = Action("Camera", "Want to read a sign like a flat number? Point the camera at a sign. We will read it and convert it into vibrations for you.", Action.Companion.ROW_TYPE.CAMERA_OCR)
                actionList.add(action5)
                if (BuildConfig.DEBUG) {
                    //Only for internal testing right now
                    var action5 = Action("Search with Camera", "Are you trying to find something? Use this option", Action.Companion.ROW_TYPE.CAMERA_OBJECT_DETECTION)
                    actionList.add(action5)
                } */
                try {
                    val obj = JSONObject(loadJSONFromAsset())
                    val m_jArry = obj.getJSONArray("content")
                    for (i in 0 until m_jArry.length()) {
                        val jo_inside = m_jArry.getJSONObject(i)
                        Log.d("Details-->", jo_inside.getString("title"))
                        val id = jo_inside.getString("id")
                        val title = jo_inside.getString("title")
                        val actualContent = jo_inside.getString("content")
                        val tagsJSONArray = jo_inside.getJSONArray("tags")
                        val tags = ArrayList<String>()
                        for (j in 0 until tagsJSONArray.length()) {
                            val tagString = tagsJSONArray.getString(j)
                            tags.add(tagString)
                        }

                        //Add your values in your `ArrayList` as below:
                        val content = Content(title, actualContent, tags, Content.Companion.ROW_TYPE.CONTENT)
                        contentList.add(content)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                adapter = MyActionRecyclerViewAdapter(contentList, mContext, mListener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                (view.list.adapter as? MyActionRecyclerViewAdapter)?.filter?.filter(newText)
                return false
            }
        })
        searchView.setOnQueryTextFocusChangeListener(object : View.OnFocusChangeListener{
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 == true) {
                    showTagsListToDisplay()
                }
            }
        })

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
        if (id == R.id.action_manual) {
            mListener?.openManualEntryFromActionsListNavBar()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun loadJSONFromAsset(): String? {
        var json: String? = null
        json = try {
            val `is`: InputStream = activity!!.assets.open("Content.json")
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    private fun showTagsListToDisplay() {
        val tagsListToDisplay = getTagsListToDisplay()
        (list.adapter as? MyActionRecyclerViewAdapter)?.updateData(tagsListToDisplay)
    }

    private fun getTagsListToDisplay() : ArrayList<Content> {
        val contentList = ArrayList<Content>()
        val content1 = Content("nursery rhymes", "", arrayListOf(), Content.Companion.ROW_TYPE.TAG_FOR_SEARCH)
        contentList.add(content1)
        val content2 = Content("christmas carols", "", arrayListOf(), Content.Companion.ROW_TYPE.TAG_FOR_SEARCH)
        contentList.add(content2)
        val content3 = Content("others", "", arrayListOf(), Content.Companion.ROW_TYPE.TAG_FOR_SEARCH)
        contentList.add(content3)

        return contentList
    }

    fun userSelectedTagForSearch(tagString : String) {
        searchView?.setQuery(tagString, true)
    }

    interface OnActionListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onActionListItemInteraction(content: Content)
        fun openActionFromActionsListScreen(action: String)
        fun openActionFromActionsListScreenWithManualInput(inputText: String)
        fun openManualEntryFromActionsListNavBar()
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
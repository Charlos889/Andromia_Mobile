package ca.qc.cstj.andromia.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ca.qc.cstj.andromia.EXPLORERS_URL
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.adapters.UnitRecyclerViewAdapter
import ca.qc.cstj.andromia.models.Unit
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ListUnitFragment.OnListFragmentInteractionListener] interface.
 */
class ListUnitFragment : Fragment() {

    private var columnCount = 1

    private var listener: OnListFragmentInteractionListener? = null
    private var units : MutableList<Unit> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_unit_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = UnitRecyclerViewAdapter(units, listener, activity)
            }

            val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE)

            val userToken = preferences.getString("token", "")
            val username = preferences.getString("username", "")
            val path : String = "$EXPLORERS_URL/$username/units"

            try {
                path.httpGet().header(mapOf("Authorization" to "Bearer $userToken")).responseJson() { _, response, result ->
                    when (response.statusCode) {
                        200 -> {
                            var lstUnits: List<Unit>
                            val json = result.get()
                            val items = json.obj().get("items")
                            lstUnits = JSON.nonstrict.parse(Unit.serializer().list, items.toString())

                            units.clear()
                            units.addAll(lstUnits.toMutableList())
                            view.adapter.notifyDataSetChanged()
                        }
                        else -> {
                            Toast.makeText(activity, "Le serveur est présentement inaccessible..", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e : Exception)
            {
                Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
            }
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
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
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(unit: Unit?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                ListUnitFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}

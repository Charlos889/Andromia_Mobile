package ca.qc.cstj.andromia.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.qc.cstj.andromia.EXPLORATIONS_URL
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.adapters.ExplorationRecyclerViewAdapter
import ca.qc.cstj.andromia.models.Exploration
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ListExplorationFragment.OnListFragmentInteractionListener] interface.
 */
class ListExplorationFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null
    private var explorations : List<Exploration> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_exploration_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ExplorationRecyclerViewAdapter(explorations, listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE)
        val userToken = preferences.getString("token", "")

        EXPLORATIONS_URL.httpGet().header(mapOf("Authorization" to "Bearer $userToken")).responseJson { request, response, result ->

        }*/
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

    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListExplorationFragmentInteraction(item: Exploration?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_EXPLORATIONS = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(explorations : List<Exploration>) = ListExplorationFragment().apply {

            this.explorations = explorations

        }
    }
}

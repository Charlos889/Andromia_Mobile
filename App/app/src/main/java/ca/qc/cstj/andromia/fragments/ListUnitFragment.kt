package ca.qc.cstj.andromia.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ca.qc.cstj.andromia.EXPLORERS_URL
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.adapters.UnitRecyclerViewAdapter
import ca.qc.cstj.andromia.models.Pagination
import ca.qc.cstj.andromia.models.Unit
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.responseObject
import kotlinx.android.synthetic.main.fragment_unit_list.*
import kotlinx.serialization.json.JSON


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ListUnitFragment.OnListFragmentInteractionListener] interface.
 */
class ListUnitFragment : Fragment() {

    private var columnCount = 1

    private var listener: OnListUnitFragmentInteractionListener? = null
    private var units : MutableList<Unit> = mutableListOf()
    private var pageNumber : Int = 0
    private var pageLimit : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
            pageLimit = it.getInt(ARG_PAGE_LIMIT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_unit_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter
        rcvUnits.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }
        rcvUnits.adapter = UnitRecyclerViewAdapter(units, listener, activity)

        if(units.size == 0) {
            txvNoUnit.visibility = View.VISIBLE
        }

        val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE)

        val userToken = preferences.getString("token", "")
        val username = preferences.getString("username", "")
        val path = "$EXPLORERS_URL/$username/units"

        try {
            val parameters = mutableListOf<Pair<String, Int>>()
            parameters.add(Pair("pageLimit", 8))

            path.httpGet(parameters)
                    .header(mapOf("Authorization" to "Bearer $userToken"))
                    .responseObject<Pagination<Unit>>(json = JSON(strictMode = false)) { _, response, result ->
                when (response.statusCode) {
                    200 -> {
                        var lstUnits: List<Unit>
                        lstUnits = result.get().items.subList(,)

                        units.clear()
                        units.addAll(lstUnits.toMutableList())
                        if (units.size > 0) {
                            rcvUnits.adapter.notifyDataSetChanged()
                            txvNoUnit.visibility = View.GONE
                        }
                        else {
                            txvNoUnit.visibility = View.VISIBLE
                        }
                    }
                    // Ici, on  ne gère pas les codes d'erreurs, car comme on passe la liste des units de l'explorer
                    // dans le constructeur, cette requête ne permet que d'updater la liste, donc même s'il y a une erreur,
                    // on pourra quand même afficher une liste, donc afficher l'erreur semble inutile. (Si c'est que le serveur
                    // est down, il obtiendra l'erreur en revenant au menu principal)
                }
            }
        } catch (e : Exception) {
            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListUnitFragmentInteractionListener) {
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
    interface OnListUnitFragmentInteractionListener {
        fun onListUnitFragmentInteraction(unit: Unit?)
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_PAGE_NUMBER = "page-number"
        const val ARG_PAGE_LIMIT = "page-limit"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, units: List<Unit>, pageNumber : Int = 0, pageLimit : Int = 8) =
                ListUnitFragment().apply {
                    this.units = units.toMutableList()

                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                        putInt(ARG_PAGE_NUMBER, pageNumber)
                        putInt(ARG_PAGE_LIMIT, pageLimit)
                    }
                }
    }
}

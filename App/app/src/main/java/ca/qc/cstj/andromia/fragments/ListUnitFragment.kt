package ca.qc.cstj.andromia.fragments

import android.content.Context
import android.opengl.Visibility
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
import kotlinx.android.synthetic.main.fragment_unit_list.*
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ListUnitFragment.OnListFragmentInteractionListener] interface.
 */
class ListUnitFragment : Fragment() {

    private var columnCount = 1

    private var listener: OnListUnitFragmentInteractionListener? = null
    private var units : MutableList<Unit> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
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
            path.httpGet()
                    .header(mapOf("Authorization" to "Bearer $userToken"))
                    .responseJson() { _, response, result ->
                when (response.statusCode) {
                    200 -> {
                        val lstUnits: List<Unit>
                        val json = result.get()
                        val items = json.obj().get("items")
                        lstUnits = JSON.nonstrict.parse(Unit.serializer().list, items.toString())

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
                    401 -> {
                        listener!!.deconnexionListUnit()
                    }
                    // Ici, on  ne gère pas les autres codes d'erreurs, car comme on passe la liste des units de l'explorer
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
        fun deconnexionListUnit()
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, units: List<Unit>) =
                ListUnitFragment().apply {
                    this.units = units.toMutableList()

                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}

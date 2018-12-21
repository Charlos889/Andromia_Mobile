package ca.qc.cstj.andromia.fragments

import android.content.Context
import android.content.res.Configuration
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
import ca.qc.cstj.andromia.models.Pagination
import ca.qc.cstj.andromia.models.Unit
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.responseObject
import kotlinx.android.synthetic.main.fragment_exploration_list.*
import kotlinx.android.synthetic.main.fragment_unit_list.*
import kotlinx.serialization.json.JSON


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 */
class ListUnitFragment : Fragment() {
    private var listener: OnListUnitFragmentInteractionListener? = null
    private var units : MutableList<Unit> = mutableListOf()
    private var pagination : Pagination<Unit>? = null
    private var gridLayoutManager : GridLayoutManager? = null
    private var isLoading : Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        isLoading = false

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            gridLayoutManager = GridLayoutManager(activity, 2)
        else if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridLayoutManager = GridLayoutManager(activity, 3)

        return inflater.inflate(R.layout.fragment_unit_list, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter
        with(rcvUnits) {
            layoutManager = gridLayoutManager
            adapter = UnitRecyclerViewAdapter(units, listener, activity)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    // Pagination
                    super.onScrolled(recyclerView, dx, dy)

                    val visibleItemCount = gridLayoutManager!!.childCount
                    val totalItemCount = gridLayoutManager!!.itemCount
                    val pastVisibleItems = gridLayoutManager!!.findFirstVisibleItemPosition()

                    if (visibleItemCount + pastVisibleItems >= totalItemCount && !isLoading) {
                        // Si la première requête n'a rien retourné, alors pagination peut être null
                        if (pagination == null) {
                            getUnits(null)
                        } else if (!pageDejaChargee(pagination!!._links.last.href)) {
                            getUnits(pagination!!._links.next.href)
                        }
                    }
                }
            })
        }

        if(units.isEmpty())
            getUnits(null)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            gridLayoutManager = GridLayoutManager(activity, 2)
        else if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridLayoutManager = GridLayoutManager(activity, 3)
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

    private fun getUnits(page : String?) {

        isLoading = true

        val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE)
        val userToken = preferences.getString("token", "")
        val username = preferences.getString("username", "")


        val path = if(page == null) {
            "$EXPLORERS_URL/$username/units?pageLimit=8"
        } else {
            "$page&pageLimit=8"
        }

        path.httpGet()
            .header(mapOf("Authorization" to "Bearer $userToken"))
            .responseJson { _, response, result ->
                when (response.statusCode) {
                    200 -> {

                        val json = result.get()
                        pagination = JSON.nonstrict.parse(Pagination.serializer(Unit.serializer()), json.content)

                        units.addAll(pagination!!.items)

                        if (!units.isEmpty()) {
                            rcvUnits.adapter.notifyDataSetChanged()
                            txvNoUnit.visibility = View.GONE
                        } else {
                            txvNoUnit.visibility = View.VISIBLE
                        }
                        isLoading = false
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
    }

    private fun pageDejaChargee(page: String) : Boolean {
        // Vérifie si la page demandée a déjà été chargée
        return (pagination != null && obtenirPage(pagination!!._links.self.href) >= obtenirPage(page))
    }

    private fun obtenirPage(page: String) : Int {
        return page.substring(page.indexOf("page=") + 5, page.indexOf("&")).toInt()
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

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance() = ListUnitFragment()
    }
}

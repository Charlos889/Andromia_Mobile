package ca.qc.cstj.andromia.fragments

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.DividerItemDecoration.VERTICAL
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.qc.cstj.andromia.EXPLORATIONS_URL
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.adapters.ExplorationRecyclerViewAdapter
import ca.qc.cstj.andromia.models.Exploration
import ca.qc.cstj.andromia.models.Pagination
import ca.qc.cstj.andromia.models.Unit
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import jp.wasabeef.picasso.transformations.CropTransformation
import kotlinx.android.synthetic.main.fragment_exploration_list.*
import kotlinx.android.synthetic.main.fragment_unit_list.*
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ListExplorationFragment.OnListFragmentInteractionListener] interface.
 */
class ListExplorationFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null
    private var explorations : MutableList<Exploration> = mutableListOf()
    private var pagination : Pagination<Exploration>? = null
    private var linearLayout : LinearLayoutManager? = null
    private var isLoading = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        isLoading = false
        linearLayout = LinearLayoutManager(context)

        return inflater.inflate(R.layout.fragment_exploration_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter
        // divider entre les items
        val decoration = DividerItemDecoration(context, VERTICAL)

        with(rcvExplorations) {
            layoutManager = linearLayout
            adapter = ExplorationRecyclerViewAdapter(explorations, listener)
            addItemDecoration(decoration)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    // Pagination
                    super.onScrolled(recyclerView, dx, dy)

                    val visibleItemCount = linearLayout!!.childCount
                    val totalItemCount = linearLayout!!.itemCount
                    val pastVisibleItems = linearLayout!!.findFirstVisibleItemPosition()

                    if (visibleItemCount + pastVisibleItems >= totalItemCount && !isLoading) {
                        // Si la première requête n'a rien retourné, alors pagination peut être null
                        if (pagination == null) {
                            obtenirExplorations(null)
                        } else if (!pageDejaChargee(pagination!!._links.last.href)) {
                            obtenirExplorations(pagination!!._links.next.href)
                        }
                    }
                }
            })
        }

        if (explorations.isEmpty()) {
            txvNoExploration.visibility = View.VISIBLE
            obtenirExplorations(null)
        }
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

    private fun obtenirExplorations(page : String?) {
        // Obtient la liste des explorations de l'utilisateur
        isLoading = true
        val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE)
        val userToken = preferences.getString("token", "")
        val username = preferences.getString("username", "")

        val path = if (page == null) {
            "$EXPLORATIONS_URL/$username?pageLimit=15"
        } else {
            "$page&pageLimit=15"
        }

        path.httpGet().header(mapOf("Authorization" to "Bearer $userToken")).responseJson { _, response, result ->
            when (response.statusCode) {
                200 -> {
                    val json = result.get()
                    val paginationObj = json.obj()
                    pagination = JSON.nonstrict.parse(Pagination.serializer(Exploration.serializer()), paginationObj.toString())

                    explorations.addAll(pagination?.items!!)

                    if (explorations.size > 0) {
                        rcvExplorations.adapter.notifyDataSetChanged()
                        txvNoExploration.visibility = View.GONE
                    } else {
                        txvNoExploration.visibility = View.VISIBLE
                    }
                    isLoading = false
                }
                // Le token a expired
                401 -> {
                    listener!!.deconnexionListExploration()
                }
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
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListExplorationFragmentInteraction(item: Exploration?)
        fun deconnexionListExploration()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ListExplorationFragment()
    }
}

package ca.qc.cstj.andromia.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.adapters.RunesRecyclerViewAdapter
import ca.qc.cstj.andromia.models.Runes
import ca.qc.cstj.andromia.models.Unit
import com.github.kittinunf.fuel.android.core.Json
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_details_unit.*
import kotlinx.serialization.json.JSON
import kotlinx.serialization.stringify

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DetailsUnitFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DetailsUnitFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class DetailsUnitFragment : Fragment() {

    private var unit: Unit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details_unit, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txvNomUnit.text = unit?.name
        Picasso.with(view.context).load(unit?.imageURL).into(imgDetailUnit)

        val mapRunes : Map<String, Int> = RunesObjectToMap(unit!!.kernel!!)

        rcvKernel.layoutManager = GridLayoutManager(view.context, 4)
        rcvKernel.adapter = RunesRecyclerViewAdapter(mapRunes, false)

    }

    fun RunesObjectToMap(runes : Runes) : Map<String, Int> {
        val map = mutableMapOf<String, Int>()

        map["air"] = runes.air
        map["darkness"] = runes.darkness
        map["earth"] = runes.earth
        map["energy"] = runes.energy
        map["fire"] = runes.fire
        map["life"] = runes.life
        map["light"] = runes.light
        map["logic"] = runes.logic
        map["music"] = runes.music
        map["space"] = runes.space
        map["toxic"] = runes.toxic
        map["water"] = runes.water

        return map
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment DetailsUnitFragment.
         */
        @JvmStatic
        fun newInstance(unit: Unit?) =
                DetailsUnitFragment().apply {
                    this.unit = unit
                }
    }
}

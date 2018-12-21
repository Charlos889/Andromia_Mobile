package ca.qc.cstj.andromia.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.adapters.RunesRecyclerViewAdapter
import ca.qc.cstj.andromia.dialogs.RunesFoundDialogFragment.Companion.runes
import ca.qc.cstj.andromia.models.Runes
import kotlinx.android.synthetic.main.dialog_runes_found.view.*
import java.lang.ClassCastException
import java.lang.IllegalStateException


class RunesFoundDialogFragment : DialogFragment() {

    companion object {
        var runes : Runes? = null
        var destination : String? = null

        fun newInstance(runes : Runes?, destination : String?) : DialogFragment {
            this.runes = runes
            this.destination = destination
            return RunesFoundDialogFragment()
        }
    }


    private var listener : RunesFoundInteractionListener? = null

    interface RunesFoundInteractionListener {
        fun onRunesNegativeClick(dialog : DialogFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            listener = targetFragment as RunesFoundInteractionListener
        } catch (e : ClassCastException) {
            throw ClassCastException("Calling fragment must implement RunesFoundInteractionListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val view = activity!!.layoutInflater.inflate(R.layout.dialog_runes_found, null)
            val titleView = activity!!.layoutInflater.inflate(R.layout.txv_custom_title, null) as TextView

            setViewContent(view)

            val runesFound = checkIfRunesFound()
            val title = if(runesFound) {
                "You arrived at $destination. You found some runes while traveling !"
            } else {
                "You arrived at $destination. You found no runes while traveling.."
            }
            titleView.text = title

            if(runesFound)
                builder.setView(view)

            builder.setCustomTitle(titleView)
                    .setNegativeButton("Okay", {dialog, id ->
                        listener!!.onRunesNegativeClick(this)
                    })



            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setViewContent(view : View) {

        val rcvRunesFound : RecyclerView = view.rcvRunesFound
        val mapRunesFound = runesObjectToMap(runes!!)

        rcvRunesFound.layoutManager = GridLayoutManager(view.context, 6)
        rcvRunesFound.adapter = RunesRecyclerViewAdapter(mapRunesFound, true)
    }

    private fun checkIfRunesFound() : Boolean {
        return (runes!!.air != 0
                || runes!!.darkness != 0
                || runes!!.earth != 0
                || runes!!.energy != 0
                || runes!!.fire != 0
                || runes!!.life != 0
                || runes!!.light != 0
                || runes!!.logic != 0
                || runes!!.music != 0
                || runes!!.space != 0
                || runes!!.toxic != 0)
    }

    fun runesObjectToMap(runes : Runes) : Map<String, Int> {
        val map = mutableMapOf<String, Int>()

        if(runes.air > 0)
            map["air"] = runes.air
        if(runes.darkness > 0)
            map["darkness"] = runes.darkness
        if(runes.earth > 0)
            map["earth"] = runes.earth
        if(runes.energy > 0)
            map["energy"] = runes.energy
        if(runes.fire > 0)
            map["fire"] = runes.fire
        if(runes.life > 0)
            map["life"] = runes.life
        if(runes.light > 0)
            map["light"] = runes.light
        if(runes.logic > 0)
            map["logic"] = runes.logic
        if(runes.music > 0)
            map["music"] = runes.music
        if(runes.space > 0)
            map["space"] = runes.space
        if(runes.toxic > 0)
            map["toxic"] = runes.toxic
        if(runes.water > 0)
            map["water"] = runes.water

        return map
    }
}
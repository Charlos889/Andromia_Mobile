package ca.qc.cstj.andromia.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.support.annotation.Dimension.SP
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.adapters.RunesRecyclerViewAdapter
import ca.qc.cstj.andromia.models.ExplorationBase
import ca.qc.cstj.andromia.models.Explorer
import ca.qc.cstj.andromia.models.Runes
import ca.qc.cstj.andromia.models.Unit
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_exploration.view.*
import java.lang.ClassCastException

class CaptureUnitDialogFragment : DialogFragment() {

    companion object {
        private var unit : Unit? = null
        private var explorer : Explorer? = null
        private var exploration : ExplorationBase? = null

        fun newInstance(unit : Unit, explorer : Explorer?, exploration : ExplorationBase?) : DialogFragment {
            this.unit = unit
            this.explorer = explorer
            this.exploration = exploration
            return CaptureUnitDialogFragment()
        }
    }


    private var listener : CaptureUnitListener? = null

    interface CaptureUnitListener {
        fun onCapturePositiveClick(dialog : DialogFragment, exploration: ExplorationBase?)
        fun onCaptureNegativeClick(dialog : DialogFragment, exploration: ExplorationBase?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            listener = parentFragment as CaptureUnitListener
        } catch (e : ClassCastException) {
            throw ClassCastException("Calling fragment must implement CaptureUnitListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val view = activity!!.layoutInflater.inflate(R.layout.dialog_exploration, null)
            val titleView = activity!!.layoutInflater.inflate(R.layout.txv_custom_title, null) as TextView

            setViewContent(view)
            val canCapture = checkIfCapturePossible()
            val title = if(canCapture) {
                "You found ${unit!!.name}! Do you want to catch it?"
            } else {
                "You found ${unit!!.name} while traveling, but you don't have enough runes to catch it.."
            }

            titleView.text = title

            builder.setCustomTitle(titleView).setView(view)
            if(canCapture) {
                builder.setPositiveButton("Yes", {dialog, id ->
                            listener!!.onCapturePositiveClick(this, exploration)
                        })
                        .setNegativeButton("No", { dialog, id ->
                            listener!!.onCaptureNegativeClick(this, exploration)
                        })
            } else {
                builder.setNegativeButton("Okay", { dialog, id ->
                    listener!!.onCaptureNegativeClick(this, exploration)
                })
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setViewContent(view : View) {
        val imgUnitDialog : ImageView = view.imgDialogUnit
        val rcvKernel : RecyclerView = view.rcvKernel
        val rcvRunesExplorer : RecyclerView = view.rcvRunesExplorer
        val mapRunesKernel = runesObjectToMap(unit!!.kernel!!)
        val mapRunesExplorer = runesObjectToMap(explorer!!.runes)
        val txvLife = view.txvLife
        val txvSpeed = view.txvSpeed

        Picasso.with(view.context).load(unit!!.imageURL).into(imgUnitDialog)

        rcvKernel.layoutManager = GridLayoutManager(view.context, 5)
        rcvKernel.adapter = RunesRecyclerViewAdapter(mapRunesKernel, true)
        rcvRunesExplorer.layoutManager = GridLayoutManager(view.context, 5)
        rcvRunesExplorer.adapter = RunesRecyclerViewAdapter(mapRunesExplorer, true)

        txvLife.text = "${txvLife.text}${unit!!.life.toString()}"
        txvSpeed.text = "${txvSpeed.text}${unit!!.speed.toString()}"
    }

    private fun checkIfCapturePossible() : Boolean {
        return (explorer!!.runes.air >= unit!!.kernel!!.air
                && explorer!!.runes.darkness >= unit!!.kernel!!.darkness
                && explorer!!.runes.earth >= unit!!.kernel!!.earth
                && explorer!!.runes.energy >= unit!!.kernel!!.energy
                && explorer!!.runes.fire >= unit!!.kernel!!.fire
                && explorer!!.runes.life >= unit!!.kernel!!.life
                && explorer!!.runes.light >= unit!!.kernel!!.light
                && explorer!!.runes.logic >= unit!!.kernel!!.logic
                && explorer!!.runes.music >= unit!!.kernel!!.music
                && explorer!!.runes.space >= unit!!.kernel!!.space
                && explorer!!.runes.toxic >= unit!!.kernel!!.toxic)
    }

    fun runesObjectToMap(runes : Runes) : Map<String, Int> {
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

}
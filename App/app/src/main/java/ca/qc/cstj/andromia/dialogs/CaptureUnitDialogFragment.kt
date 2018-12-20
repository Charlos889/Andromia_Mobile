package ca.qc.cstj.andromia.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
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
            listener = targetFragment as CaptureUnitListener
        } catch (e : ClassCastException) {
            throw ClassCastException("Calling fragment must implement CaptureUnitListener")
        }
    }

    /*override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            mListener = context as CaptureUnitListener
        } catch (e : ClassCastException) {
            throw ClassCastException((context.toString() + "must implement CaptureDialogListener"))
        }
    }*/

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val view = activity!!.layoutInflater.inflate(R.layout.dialog_exploration, null)

            setViewContent(view)
            val canCapture = checkIfCapturePossible()
            val title = if(canCapture) {
                "Do you want to catch ${unit!!.name} ?"
            } else {
                "Unfortunately, you can't catch ${unit!!.name}.."
            }

            builder.setTitle(title).setView(view)
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

        rcvKernel.layoutManager = GridLayoutManager(activity, 6)
        rcvKernel.adapter = RunesRecyclerViewAdapter(mapRunesKernel, true)
        rcvRunesExplorer.layoutManager = GridLayoutManager(activity, 6)
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
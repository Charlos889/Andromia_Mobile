package ca.qc.cstj.andromia.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.models.Unit
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_exploration.view.*

class CaptureUnitDialogFragment : DialogFragment() {

    companion object {
        private var unit = Unit()

        fun newInstance(un : Unit) : DialogFragment {
            unit = un
            return CaptureUnitDialogFragment()
        }
    }


    internal lateinit var mListener : CaptureUnitListener

    interface CaptureUnitListener {
        fun onDialogPositiveClick(dialog : DialogFragment)
        fun onDialogNegativeClick(dialog : DialogFragment)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            mListener = context as CaptureUnitListener
        } catch (e : ClassCastException) {
            throw ClassCastException((context.toString() + "must implement CaptureDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val view = activity!!.layoutInflater.inflate(R.layout.dialog_exploration, null)

            builder.setTitle("Voulez-vous capturer ${unit.name} ?")
                    .setView(view)
                    .setPositiveButton("Oui", {dialog, id ->
                        mListener.onDialogPositiveClick(this)
                    })
                    .setNegativeButton("Non", { dialog, id ->
                        mListener.onDialogNegativeClick(this)
                    })

            val imgUnitDialog : ImageView = view.imgDialogUnit
            Picasso.with(view.context).load(unit.imageURL).into(imgUnitDialog)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
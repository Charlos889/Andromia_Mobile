package ca.qc.cstj.andromia.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import ca.qc.cstj.andromia.R

class PortalNotFoundDialogFragment : DialogFragment() {

    internal lateinit var mListener : PortalNotFoundListener

    interface PortalNotFoundListener {
        fun onPortalNotFoundPositiveClick(dialog : DialogFragment)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            mListener = context as PortalNotFoundListener
        } catch (e : ClassCastException) {
            throw ClassCastException((context.toString() + "must implement CaptureDialogListener"))
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Warning")
                    .setMessage("The portal you scanned could not be found..")
                    .setPositiveButton("Okay", { dialog, id ->
                        mListener.onPortalNotFoundPositiveClick(this)
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
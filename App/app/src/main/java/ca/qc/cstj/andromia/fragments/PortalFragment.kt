package ca.qc.cstj.andromia.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.qc.cstj.andromia.EXPLORATIONS_URL
import ca.qc.cstj.andromia.PORTALS_URL

import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.dialogs.CaptureUnitDialogFragment
import ca.qc.cstj.andromia.dialogs.RunesFoundDialogFragment
import ca.qc.cstj.andromia.models.Exploration
import ca.qc.cstj.andromia.models.ExplorationBase
import ca.qc.cstj.andromia.models.Explorer
import com.bumptech.glide.Glide
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.serialization.responseObject
import kotlinx.android.synthetic.main.fragment_portal.view.*
import kotlinx.serialization.json.JSON


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_UUID = "uuid"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PortalFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PortalFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PortalFragment :   Fragment()
                        ,CaptureUnitDialogFragment.CaptureUnitListener
                        ,RunesFoundDialogFragment.RunesFoundInteractionListener{


    private var uuid : String? = null
    private var explorer : Explorer? = null


    private var listener: OnFragmentInteractionListener? = null
    private val handler : Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uuid = it.getString(ARG_UUID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_portal, container, false)
        val imgBackground = view.imgBackground
        Glide.with(view.context).load(R.drawable.portal_gif).into(imgBackground)

        return view
    }

    override fun onStart() {
        super.onStart()

        doExploration(uuid)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCapturePositiveClick(dialog: DialogFragment, explorationBase: ExplorationBase?) {

        dialog.dismiss()
        saveExploration(explorationBase, true)
    }

    override fun onCaptureNegativeClick(dialog: DialogFragment, explorationBase: ExplorationBase?) {

        dialog.dismiss()
        saveExploration(explorationBase, false)
    }

    override fun onRunesNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()

        listener!!.onExplorationDone()
    }




    private fun doExploration(uuid: String?) {

        val url = "$PORTALS_URL/$uuid"
        url.httpGet().responseObject<ExplorationBase>(json = JSON(strictMode = false)){ _, response, result ->
        //url.httpGet().response{ _, response, result ->
            when(response.statusCode) {
                200 -> {
                    val explorationRespose = result.get()
                    if(explorationRespose.unit.name != null) {
                        val dialog = CaptureUnitDialogFragment.newInstance(explorationRespose.unit, explorer, result.get())
                        dialog.isCancelable = false
                        dialog.show(childFragmentManager, "Capture")
                    } else {
                        saveExploration(result.get(), false)
                    }
                }
                404 -> {

                    val builder = AlertDialog.Builder(activity)
                    val dialog = builder.setTitle("Error")
                            .setMessage("The portal that you scanned could not be found..")
                            .setNeutralButton("Okay", {dialog, id -> })
                            .create()
                    dialog.show()
                }
            }
        }
    }

    private fun saveExploration(explorationBase: ExplorationBase?, capture : Boolean) {
        explorationBase!!.capture = capture

        val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE)
        val userToken = preferences.getString("token", "")
        val jsonExploration = JSON.stringify(ExplorationBase.serializer(), explorationBase)

        EXPLORATIONS_URL.httpPost()
                .header(mapOf("Authorization" to "Bearer $userToken"))
                .jsonBody(jsonExploration)
                .responseObject<Exploration>(json = JSON(strictMode = false)){ _, response, result ->
                    when(response.statusCode) {
                        201 -> {
                            val lastExploration = result.get()

                            // Afficher les runes trouvés durant l'exploration (s'il y en a)
                            val dialogRunes = RunesFoundDialogFragment.newInstance(lastExploration.runes, lastExploration.destination.nom )
                            dialogRunes.isCancelable = false
                            dialogRunes.setTargetFragment(this,0)

                            handler.postDelayed({
                                dialogRunes.show(fragmentManager, "RunesFound")
                            }, 1000)

                        }
                        // Le token a expired
                        401 -> {
                            listener!!.deconnexionPortal()
                        }
                        else -> {
                            Log.d("error", response.toString())
                        }
                    }
                }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun onExplorationDone()
        fun deconnexionPortal()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param uuid uuid of the portal.
         * @return A new instance of fragment PortalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(uuid: String, explorer: Explorer) =
                PortalFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_UUID, uuid)
                    }
                    this.explorer = explorer
                }
    }
}

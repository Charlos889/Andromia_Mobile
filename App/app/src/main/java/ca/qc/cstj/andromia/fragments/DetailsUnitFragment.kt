package ca.qc.cstj.andromia.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.models.Unit
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_details_unit.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val UNIT = "unit"

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
        arguments?.let {
            unit = it.get(UNIT) as Unit
        }
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
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailsUnitFragment.
         */
        @JvmStatic
        fun newInstance(unit: Unit?) =
                DetailsUnitFragment().apply {
                    this.unit = unit
                }
    }
}

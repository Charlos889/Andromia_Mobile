package ca.qc.cstj.andromia.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.qc.cstj.andromia.R


import ca.qc.cstj.andromia.fragments.ListExplorationFragment.OnListFragmentInteractionListener
import ca.qc.cstj.andromia.models.Exploration
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.GrayscaleTransformation
import kotlinx.android.synthetic.main.card_exploration.view.*
import java.text.SimpleDateFormat

class ExplorationRecyclerViewAdapter(
        private val mValues: List<Exploration>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<ExplorationRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Exploration
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListExplorationFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_exploration, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        with(holder) {
            view.tag = item
            view.setOnClickListener(mOnClickListener)
            bind(item)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val txtDestination : TextView = view.txtDestination
        val txtDateExploration : TextView = view.txtDateExploration
        val imgUnitExploration : ImageView = view.imgUnitExploration

        fun bind(exploration : Exploration) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

            txtDestination.text = "Destination : ${exploration.destination.nom}"
            txtDateExploration.text = "Date : ${dateFormat.format(exploration.dateExploration)}"

            if (exploration.unit != null) {
                // Si on n'a pas capturé la unit, on veut l'afficher en gris, pour démontrer à l'utilisateur qu'il ne l'a pas capturé
                if (exploration.capture) {
                    Picasso.with(view.context).load(exploration.unit.imageURL).into(imgUnitExploration)
                } else {
                    Picasso.with(view.context).load(exploration.unit.imageURL).transform(GrayscaleTransformation()).into(imgUnitExploration)
                }
            }
        }
    }
}

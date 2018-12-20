package ca.qc.cstj.andromia.adapters


import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.fragments.ListExplorationFragment
import ca.qc.cstj.andromia.fragments.ListUnitFragment
import kotlinx.android.synthetic.main.card_unit.view.*
import ca.qc.cstj.andromia.models.Unit
import com.squareup.picasso.Picasso

/**
 * [RecyclerView.Adapter] that can display a [Unit] and makes a call to the
 * specified [OnListUnitFragmentInteractionListener].
 */
class UnitRecyclerViewAdapter(
        private val Values: List<Unit>,
        private val Listener: ListUnitFragment.OnListUnitFragmentInteractionListener?,
        private val activity : FragmentActivity?)
    : RecyclerView.Adapter<UnitRecyclerViewAdapter.ViewHolder>() {

    private val OnClickListener: View.OnClickListener

    init {
        OnClickListener = View.OnClickListener { v ->
            val item = v.tag as Unit
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            Listener?.onListUnitFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_unit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = Values[position]

        with(holder) {
            view.tag = item
            view.setOnClickListener(OnClickListener)
            bind(item)
        }
    }

    override fun getItemCount(): Int = Values.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imgUnit: ImageView = view.imgUnit
        val txtNom : TextView = view.txtNomUnit

        fun bind(unit : Unit) {
            Picasso.with(view.context).load(unit.imageURL).into(imgUnit)
            txtNom.text = unit.name
        }
    }
}

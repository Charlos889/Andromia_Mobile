package ca.qc.cstj.andromia.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.qc.cstj.andromia.DRAWABLE_PATH
import ca.qc.cstj.andromia.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_ability.view.*

class AbilitiesRecyclerViewAdapter(
        private val mValues: List<String>)
    : RecyclerView.Adapter<AbilitiesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_ability, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        with(holder) {
            view.tag = item
            bind(item)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val txtNomAbility : TextView = view.txtNomAbility
        val imgAbility : ImageView = view.imgAbility

        fun bind(ability : String) {
            txtNomAbility.text = ability

            Picasso.with(view.context).load("$DRAWABLE_PATH${ability}").into(imgAbility)
        }
    }
}
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
import kotlinx.android.synthetic.main.card_weapon.view.*

class WeaponsRecyclerViewAdapter(
        private val mValues: List<String>)
    : RecyclerView.Adapter<WeaponsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_weapon, parent, false)
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
        val txtNomWeapon : TextView = view.txtNomWeapon
        val imgWeapon : ImageView = view.imgWeapon

        fun bind(weapon : String) {
            txtNomWeapon.text = weapon

            Picasso.with(view.context).load("$DRAWABLE_PATH${weapon}").into(imgWeapon)
        }
    }
}
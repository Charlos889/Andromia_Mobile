package ca.qc.cstj.andromia.Adapters

import android.support.v7.widget.RecyclerView
import android.system.Os.bind
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import ca.qc.cstj.andromia.Objects.Units
import ca.qc.cstj.andromia.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_unit.view.*

class UnitsAdapter(private val lstUnits : List<Units>) : RecyclerView.Adapter<UnitsAdapter.ViewHolder>(){



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_unit, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = lstUnits.size

    override fun onBindViewHolder(holder: UnitsAdapter.ViewHolder, position: Int) {
        val unit = lstUnits[position]

        holder.bind(unit)
    }


    inner class ViewHolder(view : View) :RecyclerView.ViewHolder(view) {

        val imgView = view.imgUnit as ImageView

        fun bind(unit : Units) {
            Picasso.get().load("https://assets.andromia.science/img/units/23.png").into(imgView)
        }
    }
}
package ca.qc.cstj.andromia.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import ca.qc.cstj.andromia.models.Units
import ca.qc.cstj.andromia.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_unit.view.*

class UnitsRecyclerViewAdapter(private val lstUnits : List<Units>) : RecyclerView.Adapter<UnitsRecyclerViewAdapter.ViewHolder>(){



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_unit, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = lstUnits.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val unit = lstUnits[position]

        holder.bind(unit)
    }


    inner class ViewHolder(view : View) :RecyclerView.ViewHolder(view) {

        val imgUnit : ImageView = view.imgUnit

        fun bind(unit : Units) {
            Picasso.with(imgUnit.context).load("https://assets.andromia.science/img/units/23.png").placeholder(R.mipmap.ic_launcher_round).into(imgUnit)
    }
    }
}
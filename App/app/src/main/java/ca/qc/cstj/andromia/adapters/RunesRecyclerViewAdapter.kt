package ca.qc.cstj.andromia.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.qc.cstj.andromia.DRAWABLE_PATH
import ca.qc.cstj.andromia.models.Rune
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_rune.view.*


class RunesRecyclerViewAdapter(
        private val lstRunes : List<Rune>
) : RecyclerView.Adapter<RunesRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int = lstRunes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lstRunes[position]

        with(holder) {
            bind()
        }
    }


    inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view) {

        val imgRune : ImageView = view.imgRuneKernel
        val txtQuantity : TextView = view.txtQuantityRuneKernel

        fun bind(rune: Rune) {
            txtQuantity.text = rune.quantity.toString()
            Picasso.with(view.context).load("$DRAWABLE_PATH${rune.name}.png")
        }
    }
}
package ca.qc.cstj.andromia.adapters

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.qc.cstj.andromia.DRAWABLE_PATH
import ca.qc.cstj.andromia.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_rune.view.*


class RunesRecyclerViewAdapter<K, V>(
        private val Runes: Map<K, V>,
        private val smallCard : Boolean
) : RecyclerView.Adapter<RunesRecyclerViewAdapter<K, V>.ViewHolder>() {

    private val keys = Runes.keys

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view : View
        if(smallCard) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.card_small_rune, parent, false)
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.card_rune, parent, false)
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = Runes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key = keys.elementAt(position)
        val pair = Pair(key, Runes[key]!!) as Pair<String, Int>

        with(holder) {
            bind(pair)
        }
    }


    inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view) {

        val imgRune : ImageView = view.imgRuneKernel
        val txtQuantity : TextView = view.txtQuantityRuneKernel

        fun bind(pair : Pair<String, Int>) {

            val quantite = pair.second
            if (quantite > 99) {
                txtQuantity.text = "99+"
            } else {
                txtQuantity.text = quantite.toString()
            }
            Picasso.with(view.context).load("$DRAWABLE_PATH${pair.first}").into(imgRune)
        }
    }
}
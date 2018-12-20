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
import kotlinx.android.synthetic.main.card_rune_navigation.view.*

class RunesNavigationDrawerAdapter<K, V>(
        private val Runes: Map<K, V>
) : RecyclerView.Adapter<RunesNavigationDrawerAdapter<K, V>.ViewHolder>() {

    private val keys = Runes.keys

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_rune_navigation, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = Runes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key = keys.elementAt(position)
        val pair = Pair(key, Runes[key]!!)

        with(holder) {
            bind(pair)
        }
    }


    inner class ViewHolder(val view : View) : RecyclerView.ViewHolder(view) {

        val imgRune : ImageView = view.imgRuneNavigation
        val txtQuantity : TextView = view.txtQuantityRuneNavigation

        fun bind(pair : Pair<K, V>) {
            val quantite = obtenirQuantite(pair.second)
            // Pour éviter d'un problème de layout qui se produirait si on avait 4+ caractères, j'ai décidé de stopper à 99 pour le décompte
            if (quantite > 99) {
                txtQuantity.text = "99+"
            } else {
                txtQuantity.text = quantite.toString()
            }

            Picasso.with(view.context).load("$DRAWABLE_PATH${pair.first.toString()}").into(imgRune)
        }

        private fun obtenirQuantite(nombre : V) : Int {
            return nombre.toString().toInt()
        }
    }
}
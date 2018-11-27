package ca.qc.cstj.andromia.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import ca.qc.cstj.andromia.adapters.UnitsRecyclerViewAdapter
import ca.qc.cstj.andromia.models.Unit
import ca.qc.cstj.andromia.R
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_units.*

class UnitsActivity : AppCompatActivity() {

    private var units : MutableList<Unit> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_units)


        rcvUnits.layoutManager = GridLayoutManager(this, 2)
        rcvUnits.adapter = UnitsRecyclerViewAdapter(units)

        Fuel.Companion.get("https://andromia-equipe2-ichigolatortue.c9users.io/inventaires/units")
                        .responseObject<Unit> { request, response, result ->
                            when(response.statusCode) {
                                200 -> {
                                    createUnitList(result.get())
                                }
                            }
                        }



    }

    private fun createUnitList(unit: Unit) {
        units.clear()
        units.add(unit)
        rcvUnits.adapter!!.notifyDataSetChanged()

    }
}

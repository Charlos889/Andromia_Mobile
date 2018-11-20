package ca.qc.cstj.andromia.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import ca.qc.cstj.andromia.Adapters.UnitsAdapter
import ca.qc.cstj.andromia.Objects.Units
import ca.qc.cstj.andromia.R
import kotlinx.android.synthetic.main.activity_units.*

class UnitsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_units)

        val lstTests = mutableListOf<Units>()
        lstTests.add(0,Units())
        lstTests.add(1,Units())
        lstTests.add(2,Units())
        lstTests.add(3,Units())
        lstTests.add(4,Units())

        this.rcvUnits.adapter = UnitsAdapter(lstTests)
        this.rcvUnits.layoutManager = LinearLayoutManager(this)
    }
}

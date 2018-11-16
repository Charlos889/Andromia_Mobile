package ca.qc.cstj.andromia

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ca.qc.cstj.andromia.Activities.ScanActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScanActivity.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
    }
}

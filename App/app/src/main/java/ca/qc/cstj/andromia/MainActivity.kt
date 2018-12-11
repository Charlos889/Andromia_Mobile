package ca.qc.cstj.andromia

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import ca.qc.cstj.andromia.Fragments.LoginFragment
import ca.qc.cstj.andromia.Fragments.LoginFragment.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.activity_map.*

class MainActivity : AppCompatActivity(), LoginFragment.OnFragmentInteractionListener {
    override fun onLoginFragmentInteraction() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        changeFragment(LoginFragment.newInstance(), true)
    }

    private fun changeFragment(newFragment : Fragment, saveInBackstack : Boolean = true, animate:Boolean = true, tag:String = newFragment.javaClass.name) {

        try {
            val isPopped = supportFragmentManager.popBackStackImmediate(tag, 0)
            if(!isPopped && supportFragmentManager.findFragmentByTag(tag) == null) {
                // Le fragment n'est pas dans le back stack

                val transaction =  supportFragmentManager.beginTransaction()
                if(animate) {
                    transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                }
                transaction.replace(R.id.contentFrame, newFragment, tag)
                if(saveInBackstack) {
                    transaction.addToBackStack(tag)
                }
                transaction.commit()
            }
        } catch (e:IllegalStateException) {
            Log.e("MonErreur",e.toString())
        }
    }
}

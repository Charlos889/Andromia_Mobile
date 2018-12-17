package ca.qc.cstj.andromia


import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import ca.qc.cstj.andromia.Fragments.SignupFragment
import ca.qc.cstj.andromia.Fragments.LoginFragment
import ca.qc.cstj.andromia.fragments.DetailsUnitFragment
import ca.qc.cstj.andromia.fragments.ListUnitFragment
import ca.qc.cstj.andromia.models.Unit


class MainActivity : AppCompatActivity(), ListUnitFragment.OnListFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener,
                        SignupFragment.OnFragmentInteractionListener {

    override fun onSignupFragmentInteraction() {

    }

    override fun onListFragmentInteraction(unit: Unit?) {
        changeFragment(DetailsUnitFragment.newInstance(unit))
    }

    override fun onLoginFragmentInteraction() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val intent = Intent(this,ScanActivity::class.java)
        startActivity(intent)*/

        changeFragment(SignupFragment.newInstance(), false, true)

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

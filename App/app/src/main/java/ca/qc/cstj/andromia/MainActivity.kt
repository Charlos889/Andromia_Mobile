package ca.qc.cstj.andromia

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.widget.Toast
import ca.qc.cstj.andromia.dialogs.CaptureUnitDialogFragment
import ca.qc.cstj.andromia.fragments.SignupFragment
import ca.qc.cstj.andromia.fragments.DetailsUnitFragment
import ca.qc.cstj.andromia.fragments.ListUnitFragment
import ca.qc.cstj.andromia.fragments.LoginFragment
import ca.qc.cstj.andromia.fragments.MapFragment
import ca.qc.cstj.andromia.models.Explorer
import ca.qc.cstj.andromia.models.Unit
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header_navigation.*


class MainActivity : AppCompatActivity()
        , ListUnitFragment.OnListFragmentInteractionListener
        , LoginFragment.OnFragmentInteractionListener
        , MapFragment.OnFragmentInteractionListener
        , SignupFragment.OnFragmentInteractionListener
        , CaptureUnitDialogFragment.CaptureUnitListener{

    private var explorer : Explorer? = null
    private var menuOuvert = false

    override fun onListFragmentInteraction(unit: Unit?) {
        changeFragment(DetailsUnitFragment.newInstance(unit))
    }

    override fun onSignupFragmentInteraction() {

    }

    override fun onLoginFragmentInteraction() {
        changeFragment(MapFragment.newInstance(), false)
    }

    override fun utilisateurCharge(utilisateur: Explorer) {
        explorer = utilisateur
        modifierTitre(utilisateur.username)

        txtNomExplorer.text = utilisateur.username
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(tlbMain)

        ngvAndromia.setNavigationItemSelectedListener { menuItem ->
            mainLayout.closeDrawer(GravityCompat.START)

            when (menuItem.itemId) {
                R.id.nvmHome -> {
                    supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                    val fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)

                    modifierReturnButton(fragment)
                    modifierMenuOptions(fragment)
                    modifierTitre(explorer!!.username)
                }
                R.id.nvmUnits -> {
                    changeFragment(ListUnitFragment.newInstance(2))
                    modifierTitre(explorer!!.username)
                }
                R.id.nvmLogout -> {
                    val preferences = getSharedPreferences("Andromia", Context.MODE_PRIVATE).edit()
                    preferences.putString("token", "")
                    preferences.putString("username", "")
                    preferences.commit()

                    supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    modifierTitre("Andromia")
                    changeFragment(LoginFragment.newInstance(), false)
                }
            }

            true
        }

        val preferences = getSharedPreferences("Andromia", Context.MODE_PRIVATE)
        val token = preferences.getString("token", "")

        // Si l'utilisateur est déjà connecté, on l'emmène directement à la Map (utile dans les cas d'app crash/force close)
        if (token == "") {
            modifierTitre("Andromia")
            changeFragment(LoginFragment.newInstance(), false)
        } else {
            changeFragment(MapFragment.newInstance(), false)
        }
    }

    override fun onBackPressed() {
        if (mainLayout.isDrawerOpen(GravityCompat.START)) {
            mainLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        var fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)

        when (fragment) {
            is MapFragment -> {
                mainLayout.openDrawer(GravityCompat.START)
            }
            else -> {
                supportFragmentManager.popBackStackImmediate()

                fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)

                modifierReturnButton(fragment)
                modifierMenuOptions(fragment)
            }
        }

        return super.onSupportNavigateUp()
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

            modifierReturnButton(newFragment)
            modifierMenuOptions(newFragment)
            modifierNavigationDrawer(newFragment)
        } catch (e:IllegalStateException) {
            Log.e("MonErreur",e.toString())
        }
    }

    private fun modifierTitre(titre: String = "Andromia") {
        supportActionBar!!.title = titre
    }

    private fun modifierMenuOptions(fragmentActuel: Fragment) {
        menuOuvert = when (fragmentActuel) {
            is LoginFragment -> {
                false
            }
            else -> {
                true
            }
        }

        invalidateOptionsMenu()
    }

    private fun modifierNavigationDrawer(fragmentActuel: Fragment) {
        when (fragmentActuel) {
            is LoginFragment -> {
                mainLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            else -> {
                mainLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    private fun modifierReturnButton(fragmentActuel: Fragment) {
        when (fragmentActuel) {
            is MapFragment -> {
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)
            }
            is LoginFragment -> {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            }
            else -> {
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        Toast.makeText(this, "Oui", Toast.LENGTH_LONG).show()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        Toast.makeText(this, "Non", Toast.LENGTH_LONG).show()
    }
}

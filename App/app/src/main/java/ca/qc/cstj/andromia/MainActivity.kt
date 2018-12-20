package ca.qc.cstj.andromia

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import ca.qc.cstj.andromia.adapters.RunesNavigationDrawerAdapter
import ca.qc.cstj.andromia.adapters.RunesRecyclerViewAdapter
import ca.qc.cstj.andromia.dialogs.CaptureUnitDialogFragment
import ca.qc.cstj.andromia.dialogs.PortalNotFoundDialogFragment
import ca.qc.cstj.andromia.fragments.*
import ca.qc.cstj.andromia.models.Exploration
import ca.qc.cstj.andromia.models.Explorer
import ca.qc.cstj.andromia.models.Runes
import ca.qc.cstj.andromia.models.Unit
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_details_unit.*
import kotlinx.android.synthetic.main.fragment_unit_list.*
import kotlinx.android.synthetic.main.header_navigation.*


class MainActivity : AppCompatActivity()
        , ListUnitFragment.OnListUnitFragmentInteractionListener
        , LoginFragment.OnFragmentInteractionListener
        , MapFragment.OnFragmentInteractionListener
        , ListExplorationFragment.OnListFragmentInteractionListener
        , SignupFragment.OnFragmentInteractionListener
        , PortalFragment.OnFragmentInteractionListener{

    private var explorer = Explorer()
    private var menuOuvert = false
    private var progressDialog : ProgressDialog? = null

    override fun onListUnitFragmentInteraction(unit: Unit?) {
        if(unit != null)
            changeFragment(DetailsUnitFragment.newInstance(unit))
    }

    override fun onSignupFragmentInteraction() {
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        changeFragment(MapFragment.newInstance(), false)

        progressDialog = ProgressDialog(this)
        progressDialog!!.isIndeterminate = true
        progressDialog!!.setMessage("Chargement en cours...")
        progressDialog!!.show()
    }

    override fun onListExplorationFragmentInteraction(item: Exploration?) {
        if (item?.unit != null && item.capture) {
            changeFragment(DetailsUnitFragment.newInstance(item.unit))
        }
    }

    override fun ouvrirSignup() {
        changeFragment(SignupFragment.newInstance())
    }

    override fun connectionEffecutee() {
        changeFragment(MapFragment.newInstance(), false)

        progressDialog = ProgressDialog(this)
        progressDialog!!.isIndeterminate = true
        progressDialog!!.setMessage("Chargement en cours...")
        progressDialog!!.show()
    }

    override fun utilisateurCharge(utilisateur: Explorer) {
        explorer = utilisateur
        modifierTitre(utilisateur.username.toUpperCase())

        txtNomExplorer.text = utilisateur.username.toUpperCase()
        txtQtyInox.text = utilisateur.inox.amount.toString()

        progressDialog?.dismiss()
        progressDialog = null

        val mapRunes : Map<String, Int> = RunesObjectToMap(utilisateur.runes)

        rcvRunes.layoutManager = GridLayoutManager(this, 4)
        rcvRunes.adapter = RunesNavigationDrawerAdapter(mapRunes)
    }

    override fun utilisateurExistant(): Boolean {
        return explorer.username != ""
    }

    override fun retourLogin() {
        progressDialog?.dismiss()
        progressDialog = null

        val alert = AlertDialog.Builder(this)

        alert.setTitle("Une erreur est survenue")
        alert.setMessage("Veuillez réessayer plus tard")
        alert.setNeutralButton("OK") { dialog, which ->
            logout()
        }
        alert.show()
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
                    modifierTitre(explorer.username)
                }
                R.id.nvmUnits -> {
                    changeFragment(ListUnitFragment.newInstance(2, explorer.units))
                    modifierTitre(explorer.username)
                }
                R.id.nvmExplorations -> {
                    changeFragment(ListExplorationFragment.newInstance(explorer.explorations))
                }
                R.id.nvmLogout -> {
                    logout()
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

            val fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
            modifierReturnButton(fragment)
            modifierMenuOptions(fragment)
            modifierNavigationDrawer(fragment)
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

    override fun onPortalScanned(uuid: String, explorer: Explorer) {
        changeFragment(PortalFragment.newInstance(uuid, explorer), false)
    }

    override fun onExplorationDone() {
        changeFragment(MapFragment.newInstance(), false)
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
            is LoginFragment, is SignupFragment -> {
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
            is LoginFragment, is SignupFragment -> {
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

    private fun logout() {
        val preferences = getSharedPreferences("Andromia", Context.MODE_PRIVATE).edit()
        preferences.putString("token", "")
        preferences.putString("username", "")
        preferences.commit()

        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        modifierTitre("Andromia")
        changeFragment(LoginFragment.newInstance(), false)
    }

    private fun RunesObjectToMap(runes : Runes) : Map<String, Int> {
        val map = mutableMapOf<String, Int>()

        map["air"] = runes.air
        map["darkness"] = runes.darkness
        map["earth"] = runes.earth
        map["energy"] = runes.energy
        map["fire"] = runes.fire
        map["life"] = runes.life
        map["light"] = runes.light
        map["logic"] = runes.logic
        map["music"] = runes.music
        map["space"] = runes.space
        map["toxic"] = runes.toxic
        map["water"] = runes.water

        return map
    }
}

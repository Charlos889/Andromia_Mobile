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
        , PortalNotFoundDialogFragment.PortalNotFoundListener
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

        afficherProgressBar()
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

        afficherProgressBar()
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
        avertirDeconnexion()
    }

    override fun deconnexionListExploration() {
        avertirDeconnexion()
    }

    override fun deconnexionListUnit() {
        avertirDeconnexion()
    }

    override fun deconnexionPortal() {
        avertirDeconnexion()
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
                    modifierTitre(explorer.username.toUpperCase())
                }
                R.id.nvmUnits -> {
                    changeFragment(ListUnitFragment.newInstance(2, listOf()))
                    modifierTitre(explorer.username.toUpperCase())
                }
                R.id.nvmExplorations -> {
                    changeFragment(ListExplorationFragment.newInstance())
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
            afficherProgressBar()
        }
    }

    override fun onBackPressed() {
        // Si le navigation drawer est ouvert, on le ferme
        if (mainLayout.isDrawerOpen(GravityCompat.START)) {
            mainLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()

            // Update le menu
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
                // Si on clique sur le bouton Home pendant qu'on est sur MapFragment, on veut ouvrir le navigation drawer
                mainLayout.openDrawer(GravityCompat.START)
            }
            else -> {
                // Retour d'un fragment en arrière
                supportFragmentManager.popBackStackImmediate()

                fragment = supportFragmentManager.findFragmentById(R.id.contentFrame)

                modifierReturnButton(fragment)
                modifierMenuOptions(fragment)
                modifierNavigationDrawer(fragment)
            }
        }

        return super.onSupportNavigateUp()
    }

    override fun onPortalNotFoundPositiveClick(dialog: DialogFragment) {
        dialog.dismiss()
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
        // Si le menu est accessible
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
        // Si le fragment est accessible
        when (fragmentActuel) {
            is LoginFragment, is SignupFragment -> {
                mainLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            else -> {
                mainLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }

        var id = 0

        when (fragmentActuel) {
            is MapFragment -> {
                id = R.id.nvmHome
            }
            is ListUnitFragment -> {
                id = R.id.nvmUnits
            }
            is ListExplorationFragment -> {
                id = R.id.nvmExplorations
            }
        }

        for (i in 0 until ngvAndromia.menu.size()) {
            val item = ngvAndromia.menu.getItem(i)

            item.isChecked = (item.itemId == id)
        }
    }

    private fun modifierReturnButton(fragmentActuel: Fragment) {
        // Si le bouton home est accessible
        when (fragmentActuel) {
            is MapFragment -> {
                // Sur la map, on montre le hamburger plutôt que la flèche de retour
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

    private fun avertirDeconnexion() {
        progressDialog?.dismiss()
        progressDialog = null

        val alert = AlertDialog.Builder(this)

        alert.setTitle("An error has occured")
        alert.setMessage("Please reconnect")
        alert.setNeutralButton("OK") { dialog, which ->
            logout()
        }
        alert.show()
    }

    private fun logout() {
        val preferences = getSharedPreferences("Andromia", Context.MODE_PRIVATE).edit()
        preferences.putString("token", "")
        preferences.putString("username", "")
        preferences.commit()

        // On retire tous les fragments du BackStack
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

    private fun afficherProgressBar() {
        // Oui, j'ai réalisé que ProgressDialog est deprecated parce qu'il bloque l'utilisateur d'interagir avec le UI,
        // par contre, c'est exactement ce que je veux, donc ça reste la méthode la plus simple d'arriver à mon but
        progressDialog = ProgressDialog(this)
        progressDialog!!.isIndeterminate = true
        progressDialog!!.setMessage("Loading...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }
}

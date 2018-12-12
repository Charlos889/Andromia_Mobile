package ca.qc.cstj.andromia

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import ca.qc.cstj.andromia.Fragments.LoginFragment
import ca.qc.cstj.andromia.Fragments.MapFragment
import ca.qc.cstj.andromia.fragments.DetailsUnitFragment
import ca.qc.cstj.andromia.fragments.ListUnitFragment
import ca.qc.cstj.andromia.models.Explorer
import ca.qc.cstj.andromia.models.Unit
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity()
        , ListUnitFragment.OnListFragmentInteractionListener
        , LoginFragment.OnFragmentInteractionListener
        , MapFragment.OnFragmentInteractionListener {

    private var explorer : Explorer? = null
    private var menuOuvert = false

    override fun onListFragmentInteraction(unit: Unit?) {
        changeFragment(DetailsUnitFragment.newInstance(unit))
    }

    override fun onLoginFragmentInteraction() {
        supportFragmentManager.popBackStack()
        changeFragment(MapFragment.newInstance(), false)
        modifierAffichageMenu(true)
    }

    override fun utilisateurCharge(utilisateur: Explorer) {
        explorer = utilisateur
        supportActionBar!!.title = utilisateur.username
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = getSharedPreferences("Andromia", Context.MODE_PRIVATE)
        val token = preferences.getString("token", "")

        // Si l'utilisateur est déjà connecté, on l'emmène directement à la Map (utile dans les cas d'app crash/force close)
        if (token == "") {
            modifierAffichageMenu(false)
            changeFragment(LoginFragment.newInstance(), false)
        } else {
            modifierAffichageMenu(true)
            changeFragment(MapFragment.newInstance(), false)
        }

        // Apparemment les FrameLayout ne prennent pas leur taille du XML, il faut les set manuellement
        contentFrame.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            menuInflater.inflate(R.menu.menu_andromia, menu)

            if (!menuOuvert) {
                for (i in 0 until menu.size()) {
                    menu.getItem(i).isVisible = menuOuvert
                }
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.btnLogout -> {
                val preferences = getSharedPreferences("Andromia", Context.MODE_PRIVATE).edit()

                preferences.putString("token", "")
                preferences.commit()

                modifierAffichageMenu(false)
                changeFragment(LoginFragment.newInstance(), false)

                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportFragmentManager.popBackStackImmediate()
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
        } catch (e:IllegalStateException) {
            Log.e("MonErreur",e.toString())
        }
    }

    private fun modifierAffichageMenu(afficherMenu : Boolean, afficherBoutonRetour: Boolean = false) {
        menuOuvert = afficherMenu
        invalidateOptionsMenu()

        supportActionBar!!.setDisplayHomeAsUpEnabled(afficherBoutonRetour)
    }
}

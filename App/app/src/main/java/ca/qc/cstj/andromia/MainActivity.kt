package ca.qc.cstj.andromia

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.JsonWriter
import android.util.Log
import ca.qc.cstj.andromia.Fragments.LoginFragment
import ca.qc.cstj.andromia.Fragments.MapFragment
import ca.qc.cstj.andromia.fragments.DetailsUnitFragment
import ca.qc.cstj.andromia.fragments.ListUnitFragment
import ca.qc.cstj.andromia.models.Unit
import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpPost
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.json.JSONObject
import java.io.Writer


class MainActivity : AppCompatActivity()
        , ListUnitFragment.OnListFragmentInteractionListener
        , LoginFragment.OnFragmentInteractionListener
        , MapFragment.OnFragmentInteractionListener{
    override fun onListFragmentInteraction(unit: Unit?) {
        changeFragment(DetailsUnitFragment.newInstance(unit))
    }

    override fun onLoginFragmentInteraction(username: String, password: String) {
        loginUtilisateur(username, password)
    }

    override fun onMapFragmentInteraction() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = getSharedPreferences("Andromia", Context.MODE_PRIVATE)
        val token = preferences.getString("token", "")

        if (token == "") {
            changeFragment(LoginFragment.newInstance(), false, true)
        } else {
            changeFragment(MapFragment.newInstance(), false, true)
        }
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

    private fun loginUtilisateur(username: String, password: String) {
        val path = "$EXPLORERS_URL/login"
        var json = JSONObject()
        json.put("username", username)
        json.put("password", password)
        path.httpPost().body(json.toString()).header(mapOf("Content-Type" to "application/json")).responseJson { request, response, result ->
            when (response.statusCode) {
                201 -> {
                    val json = result.get()
                    val preferences = getSharedPreferences("Andromia", Context.MODE_PRIVATE).edit()

                    preferences.putString("token", json.obj().get("token").toString())
                    preferences.commit()

                    supportFragmentManager.popBackStack()
                    changeFragment(MapFragment.newInstance(), false)
                }
                else -> {
                    Log.e("Error", response.toString())
                }
            }
        }
    }
}

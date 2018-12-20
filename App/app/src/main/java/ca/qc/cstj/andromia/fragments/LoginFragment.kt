package ca.qc.cstj.andromia.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import ca.qc.cstj.andromia.EXPLORERS_URL

import ca.qc.cstj.andromia.R
import com.andreabaccega.widget.FormEditText
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpPost
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LoginFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters


    private var listener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onStart() {
        btnLogin.setOnClickListener {

            closeKeyboard()
            onLoginPressed()
        }

        btnSignup.setOnClickListener {
            listener!!.ouvrirSignup()
        }

        super.onStart()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun connectionEffecutee()
        fun ouvrirSignup()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment LoginFragment.
         */
        @JvmStatic
        fun newInstance() = LoginFragment()
    }

    private fun onLoginPressed() {

        val allFields = arrayOf<FormEditText>(edtUsername, edtPassword)

        var allValid = true
        for (field in allFields) {
            allValid = field.testValidity() && allValid
        }

        if (allValid) {
            login()
        } else {
            // EditText are going to appear with an exclamation mark and an explicative message.
        }

    }

    private fun login() {

        val username = edtUsername.text.toString()
        val password = edtPassword.text.toString()

        val path = "$EXPLORERS_URL/login"

        // Formation de l'objet de l'utilisateur
        val json = JSONObject()
        json.put("username", username)
        json.put("password", password)

        path.httpPost().body(json.toString()).header(mapOf("Content-Type" to "application/json")).responseJson { _, response, result ->
            when (response.statusCode) {
                200 -> {
                    // Sauvegarder les infos dans les SharedPreferences
                    val jsonResult = result.get()
                    val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE).edit()

                    preferences.putString("token", jsonResult.obj().get("token").toString())
                    preferences.putString("username", username)
                    // Le commit est utile pour conserver l'information des SharedPreferences en cas d'app crash (autrement, avec apply, on les perdait)
                    preferences.commit()
                    listener!!.connectionEffecutee()
                }
                401 -> {
                    edtUsername.setError("Either the username or the password you entered is incorrect")
                }
                else -> {
                    Log.e("Error", response.toString())
                }
            }
        }

    }

    private fun closeKeyboard() {
        val view = this.view
        if(view != null) {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}

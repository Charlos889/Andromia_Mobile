package ca.qc.cstj.andromia.Fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ca.qc.cstj.andromia.EXPLORERS_URL
import ca.qc.cstj.andromia.R
import kotlinx.android.synthetic.main.fragment_signup.*
import com.andreabaccega.widget.FormEditText
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpPost
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.parse
import kotlinx.serialization.stringify
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SignupFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SignupFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SignupFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    @ImplicitReflectionSerializer
    override fun onStart() {
        super.onStart()

        btnSignup.setOnClickListener {
            onSignupPressed()
        }
    }

    @ImplicitReflectionSerializer
    fun onSignupPressed() {

        val allFields = arrayOf<FormEditText>(edtUsername, edtEmail, edtPassword, edtPasswordRepeat)

        var allValid = true
        for (field in allFields) {
            allValid = field.testValidity() && allValid
            if(field == edtUsername && field.text.length > 25) {
                field.setError("Your username must not exceed 25 characters")
                allValid = false && allValid
            }
        }

        if (allValid) {
            val InfosUser : String = """{"username":"${edtUsername.text}","email":"${edtEmail.text}","password":"${edtPassword.text}"}"""
            EXPLORERS_URL.httpPost().jsonBody(InfosUser).responseJson{request, response, result ->
                when(response.statusCode) {
                    201 -> {
                        val json = result.get()
                    }
                    else -> {
                        Toast.makeText(activity,"${response.statusCode}, ${response.responseMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            // EditText are going to appear with an exclamation mark and an explicative message.
        }

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
        // TODO: Update argument type and name
        fun onSignupFragmentInteraction()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SignupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
                SignupFragment()
    }
}

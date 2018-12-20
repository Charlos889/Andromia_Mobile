package ca.qc.cstj.andromia.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.support.v4.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GestureDetectorCompat
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import ca.qc.cstj.andromia.EXPLORATIONS_URL
import ca.qc.cstj.andromia.EXPLORERS_URL
import ca.qc.cstj.andromia.PORTALS_URL
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.dialogs.CaptureUnitDialogFragment
import ca.qc.cstj.andromia.dialogs.PortalNotFoundDialogFragment
import ca.qc.cstj.andromia.dialogs.RunesFoundDialogFragment
import ca.qc.cstj.andromia.models.ExplorationBase
import ca.qc.cstj.andromia.models.Explorer
import ca.qc.cstj.andromia.models.Runes
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.serialization.responseObject
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.serialization.json.JSON
import java.lang.Exception

class MapFragment : Fragment()
                    , GestureDetector.OnGestureListener
                    , GestureDetector.OnDoubleTapListener
                    , CaptureUnitDialogFragment.CaptureUnitListener
                    , RunesFoundDialogFragment.RunesFoundInteractionListener{

    private var positionJoueur = PointF(540f, 361f)
    private var tailleImage = PointF()
    private var oldX : Float = 0f
    private var oldY : Float = 0f
    private var setAnimation = AnimatorSet()
    private var gDetector: GestureDetectorCompat? = null
    private var ScaleDetector : ScaleGestureDetector? = null
    private var listener: OnFragmentInteractionListener? = null
    private var explorerObj: Explorer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        view.setOnTouchListener { _, event ->
            when (event!!.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount == 1) {
                        val newX = event.getX(0)
                        val newY = event.getY(0)
                        val scrollX = (newX - oldX)
                        val scrollY = (newY - oldY)

                        if (verifierTranslationValide(scrollX, true)) {
                            imgMap.translationX  += scrollX
                            oldX = newX
                        }

                        if (verifierTranslationValide(scrollY, false)) {
                            imgMap.translationY += scrollY
                            oldY = newY
                        }

                        positionnerBouton()
                    } else {
                        ScaleDetector!!.onTouchEvent(event)
                    }
                }
                else -> {
                    gDetector?.onTouchEvent(event)
                    ScaleDetector!!.onTouchEvent(event)
                }
            }
        }

        return view
    }

    override fun onStart() {

        val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE)

        EXPLORERS_URL.httpGet()
                .header(mapOf("Authorization" to "Bearer ${preferences.getString("token", "")}"))
                .responseJson { request, response, result ->

                    when (response.statusCode) {
                        200 -> {
                            try {
                                val json = result.get()
                                val explorer = json.obj()
                                explorerObj = JSON.nonstrict.parse(Explorer.serializer(), explorer.toString())

                                if (explorerObj!!.explorations.isNotEmpty()) {
                                    val posJoueur = explorerObj!!.explorations.last().destination.coordonnees

                                    positionJoueur = PointF(posJoueur.x.toFloat(), posJoueur.y.toFloat())

                                    positionnerBouton()
                                }

                                listener!!.utilisateurCharge(explorerObj!!)
                            } catch (e : Exception) {
                                e.printStackTrace()
                            }
                        }
                        else -> {
                            // Si le serveur ferme pendant que l'utilisateur utilise l'application,
                            // on veut l'avertir du problème, sans nécessairement le bloquer d'utiliser l'application
                            // Sinon, on le déconnecte (ça sert à rien de le laisser continuer, on n'a pas ses infos)
                            if (listener!!.utilisateurExistant()) {
                                Toast.makeText(this.context, "Une erreur est survenue...", Toast.LENGTH_LONG).show()
                            } else {
                                listener!!.retourLogin()
                            }
                        }
                    }
                }

        gDetector = GestureDetectorCompat(this.context, this)

        gDetector?.setOnDoubleTapListener(this)
        ScaleDetector = ScaleGestureDetector(this.context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                val scale = imgMap.scaleX * detector!!.scaleFactor

                if (scale < 1) {
                    imgMap.scaleX = 1f
                    imgMap.scaleY = 1f
                }
                else if (scale > 5) {
                    imgMap.scaleX = 5f
                    imgMap.scaleY = 5f
                }
                else {
                    imgMap.scaleX = scale
                    imgMap.scaleY = scale
                }

                positionnerBouton()

                return true
            }
        })

        val optBitmap = BitmapFactory.Options()
        optBitmap.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT
        val bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.andromia, optBitmap)

        tailleImage = PointF(bmp.width.toFloat(), bmp.height.toFloat())

        imgMap.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                imgMap.viewTreeObserver.removeOnGlobalLayoutListener(this)

                positionnerBouton()
            }
        })

        btnScan.setOnClickListener {

            val scanner = IntentIntegrator.forSupportFragment(this)
            scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            scanner.setBeepEnabled(false)
            scanner.initiateScan()
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

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        oldX = p0!!.getX(0)
        oldY = p0.getY(0)
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {

    }

    override fun onDoubleTap(p0: MotionEvent?): Boolean {

        if (!setAnimation.isRunning) {
            var scaleAnimation : ObjectAnimator?
            var btnScaleAnimation : ObjectAnimator?
            var newScale : Float = 0f
            var newTransX : Float = 0f
            var newTransY : Float = 0f

            if (imgMap.scaleX == 1f && imgMap.scaleY == 1f) {
                var BaseScale = TypedValue()

                resources.getValue(R.dimen.BASE_SCALE, BaseScale, true)

                val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, imgMap.scaleX, BaseScale.float)
                val pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, imgMap.scaleY, BaseScale.float)

                val point = zoomIn(p0!!.x, p0.y, BaseScale.float)
                if (point != null) {
                    val trX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, imgMap.translationX, point.x)
                    val trY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, imgMap.translationY, point.y)

                    scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(imgMap, pvhX, pvhY, trX, trY)


                    newScale = BaseScale.float
                    newTransX = point.x
                    newTransY = point.y
                } else {
                    scaleAnimation = null
                }
            } else {
                val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, imgMap.scaleX, 1f)
                val pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, imgMap.scaleY, 1f)

                val trX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, imgMap.translationX, 0f)
                val trY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, imgMap.translationY, 0f)

                scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(imgMap, pvhX, pvhY, trX, trY)

                newScale = 1f
                newTransX = 0f
                newTransY = 0f
            }

            if (scaleAnimation != null) {
                // Animation du bouton du joueur
                val matrix = FloatArray(9)
                imgMap.imageMatrix.getValues(matrix)

                val pointCentral = obtenirPointCentral(newScale, newScale, newTransX, newTransY, matrix)

                val btnTrX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, btnPosition.translationX, ((positionJoueur.x * (imgMap.pivotX * 2 - matrix[Matrix.MTRANS_X] * 2) / tailleImage.x) - pointCentral.x) * newScale)
                val btnTrY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, btnPosition.translationY, ((positionJoueur.y * (imgMap.pivotY * 2 - matrix[Matrix.MTRANS_Y] * 2) / tailleImage.y) - pointCentral.y) * newScale)

                btnScaleAnimation = ObjectAnimator.ofPropertyValuesHolder(btnPosition, btnTrX, btnTrY)

                setAnimation = AnimatorSet()
                setAnimation.play(scaleAnimation)
                setAnimation.duration = 1000
                setAnimation.start()

                setAnimation = AnimatorSet()
                setAnimation.play(btnScaleAnimation)
                setAnimation.duration = 1000
                setAnimation.start()
            }
        }

        return true
    }

    override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK) {
            val result  = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if(result != null) {
                if(result.contents == null) {
                    Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    doExploration(result.contents)
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onCapturePositiveClick(dialog: DialogFragment, explorationBase: ExplorationBase?) {

        dialog.dismiss()

        val dialogRunes = RunesFoundDialogFragment.newInstance(explorationBase!!.runes)
        dialogRunes.setTargetFragment(this, 0)
        dialogRunes.isCancelable = false
        dialogRunes.show(fragmentManager, "RunesFound")

        saveExploration(explorationBase, true)
    }

    override fun onCaptureNegativeClick(dialog: DialogFragment, explorationBase: ExplorationBase?) {

        dialog.dismiss()

        val dialogRunes = RunesFoundDialogFragment.newInstance(explorationBase!!.runes)
        dialogRunes.setTargetFragment(this, 0)
        dialogRunes.isCancelable = false
        dialogRunes.show(fragmentManager, "RunesFound")

        saveExploration(explorationBase, false)
    }

    override fun onRunesNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }


    private fun doExploration(uuid: String?) {

        val url = "$PORTALS_URL/$uuid"
        url.httpGet().responseObject<ExplorationBase>(json = JSON(strictMode = false)){ _, response, result ->
            when(response.statusCode) {
                200 -> {
                    val explorationRespose = result.get()

                    if(explorationRespose.unit.name != null) {

                        val dialog = CaptureUnitDialogFragment.newInstance(explorationRespose.unit, explorerObj, result.get())
                        dialog.setTargetFragment(this, 0)
                        dialog.isCancelable = false
                        dialog.show(fragmentManager, "Capture")

                    } else if (checkIfRunesFound(explorationRespose.runes)) {

                        val dialog = RunesFoundDialogFragment.newInstance(explorationRespose.runes)
                        dialog.setTargetFragment(this, 0)
                        dialog.isCancelable = false
                        dialog.show(fragmentManager, "RunesFound")
                    }
                }
                404 -> {
                    val dialog = PortalNotFoundDialogFragment()
                    dialog.show(fragmentManager, "PortalNotFound")
                }
            }
        }
    }

    private fun saveExploration(explorationBase: ExplorationBase?, capture : Boolean) {
        explorationBase!!.capture = capture

        val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE)

        val userToken = preferences.getString("token", "")

        val jsonExploration = JSON.stringify(ExplorationBase.serializer(), explorationBase)
        EXPLORATIONS_URL.httpPost()
                .header(mapOf("Authorization" to "Bearer $userToken"))
                .jsonBody(jsonExploration)
                .responseObject<Explorer>(json = JSON(strictMode = false)){ _, response, result ->
            when(response.statusCode) {
                201 -> {
                    val explorerReceived = result.get()
                    val lastExploration = explorerReceived.explorations.last()

                    // Modifier la position du joueur
                    positionJoueur = PointF(lastExploration.destination.coordonnees.x.toFloat(), lastExploration.destination.coordonnees.y.toFloat())
                    positionnerBouton()

                    listener!!.utilisateurCharge(explorerReceived)
                }
                else -> {
                    Log.d("error", response.toString())
                }
            }
        }
    }

    private fun checkIfRunesFound(runes : Runes) : Boolean {
        return (runes!!.air != 0
                || runes!!.darkness != 0
                || runes!!.earth != 0
                || runes!!.energy != 0
                || runes!!.fire != 0
                || runes!!.life != 0
                || runes!!.light != 0
                || runes!!.logic != 0
                || runes!!.music != 0
                || runes!!.space != 0
                || runes!!.toxic != 0)
    }


    private fun zoomIn(x : Float, y : Float, baseScale : Float) : PointF? {
        val posView = IntArray(2)
        imgMap.getLocationOnScreen(posView)

        val matrix = FloatArray(9)
        imgMap.imageMatrix.getValues(matrix)

        val posX = x - (posView[0] + matrix[Matrix.MTRANS_X])
        val posY = y - (posView[1] + matrix[Matrix.MTRANS_Y])

        val tailleMap = obtenirTailleMap()

        if (posX >= 0
                && posY >= 0
                && posX <= tailleMap.x
                && posY <= tailleMap.y)
            return PointF(((tailleMap.x / 2) * baseScale) - (posX * baseScale)
                    , ((tailleMap.y / 2) * baseScale) - (posY * baseScale))
        else
            return null
    }

    private fun obtenirTailleMap() : PointF {
        val matrix = FloatArray(9)
        imgMap.imageMatrix.getValues(matrix)

        val widthMap = (matrix[Matrix.MSCALE_X] * imgMap.drawable.intrinsicWidth)
        val heightMap = (matrix[Matrix.MSCALE_Y] * imgMap.drawable.intrinsicHeight)

        return PointF(widthMap, heightMap)
    }

    private fun obtenirPointCentral(newScaleX : Float, newScaleY : Float, newTransX : Float, newTransY: Float, matrix : FloatArray) : PointF {
        return PointF((imgMap.pivotX) - matrix[Matrix.MTRANS_X] - (newTransX / newScaleX),
                (imgMap.pivotY) - matrix[Matrix.MTRANS_Y] - (newTransY / newScaleY))
    }

    private fun verifierTranslationValide(trans : Float, transHori : Boolean) : Boolean {
        var curr: Float
        val tailleMap = obtenirTailleMap()

        if (transHori) {
            curr = imgMap.translationX

            return ((tailleMap.x * imgMap.scaleX / 2) < (tailleMap.x * imgMap.scaleX) - (curr + trans)
                    && (tailleMap.x * imgMap.scaleX / 2) < (tailleMap.x * imgMap.scaleX) + (curr + trans))
        } else {
            curr = imgMap.translationY

            return ((tailleMap.y * imgMap.scaleY / 2) < (tailleMap.y * imgMap.scaleY) - (curr + trans)
                    && (tailleMap.y * imgMap.scaleY / 2) < (tailleMap.y * imgMap.scaleY) + (curr + trans))
        }
    }

    private fun positionnerBouton(): Boolean {
        val matrix = FloatArray(9)
        imgMap.imageMatrix.getValues(matrix)

        val pointCentral = obtenirPointCentral(imgMap.scaleX, imgMap.scaleY, imgMap.translationX, imgMap.translationY, matrix)
        btnPosition.translationX = ((positionJoueur.x * (imgMap.pivotX * 2 - matrix[Matrix.MTRANS_X] * 2) / tailleImage.x) - pointCentral.x) * imgMap.scaleX
        btnPosition.translationY = ((positionJoueur.y * (imgMap.pivotY * 2 - matrix[Matrix.MTRANS_Y] * 2) / tailleImage.y) - pointCentral.y) * imgMap.scaleY

        return true
    }


    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun utilisateurCharge(utilisateur: Explorer)
        fun utilisateurExistant() : Boolean
        fun retourLogin()
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = MapFragment()
    }
}

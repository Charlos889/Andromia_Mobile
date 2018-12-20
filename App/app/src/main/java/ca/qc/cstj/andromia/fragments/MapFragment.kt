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
import android.os.Handler
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
import ca.qc.cstj.andromia.models.PositionExploration
import com.bumptech.glide.Glide
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
                    , GestureDetector.OnDoubleTapListener {

    private var positionJoueur = PointF(540f, 361f)
    private var tailleImage = PointF()
    private var oldX : Float = 0f
    private var oldY : Float = 0f
    private var setAnimation = AnimatorSet()
    private var gDetector: GestureDetectorCompat? = null
    private var ScaleDetector : ScaleGestureDetector? = null
    private var listener: OnFragmentInteractionListener? = null
    private var explorerObj: Explorer? = null
    private val handler : Handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Gérer les évènements sur la carte (déplacements, zoom, ...)
        view.setOnTouchListener { _, event ->
            when (event!!.action) {
                // Gérer les déplacements
                MotionEvent.ACTION_MOVE -> {
                    // Pour éviter de déplacer la carte pendant qu'on zoom
                    if (event.pointerCount == 1) {
                        val newX = event.getX(0)
                        val newY = event.getY(0)
                        val scrollX = (newX - oldX)
                        val scrollY = (newY - oldY)

                        // On vérife si les scrolls sont valides séparémment, car même si on est arrivé à une extrémité en X,
                        // on veut pouvoir continuer de se déplacer en Y
                        if (verifierTranslationValide(scrollX, true)) {
                            imgMap.translationX  += scrollX
                            oldX = newX
                        }

                        if (verifierTranslationValide(scrollY, false)) {
                            imgMap.translationY += scrollY
                            oldY = newY
                        }

                        // Repositionne le bouton du joueur
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
        // On obtient les infos de l'explorer
        // On le fait dans le onStart, pour faire en sorte que lorsque l'utilisateur revient en appuyant sur la flèche de retour
        // on aille chercher à nouveau ses informations (utile pour updater ses runes et inox, qui sont ajoutées à chaque heure)
        val preferences = activity!!.getSharedPreferences("Andromia", Context.MODE_PRIVATE)

        var path = "$EXPLORERS_URL/${preferences.getString("username", "")}"

        path.httpGet()
                .header(mapOf("Authorization" to "Bearer ${preferences.getString("token", "")}"))
                .responseJson { request, response, result ->

                    when (response.statusCode) {
                        200 -> {
                            try {
                                val json = result.get()
                                val explorer = json.obj()
                                explorerObj = JSON.nonstrict.parse(Explorer.serializer(), explorer.toString())

                                listener!!.utilisateurCharge(explorerObj!!)
                            } catch (e : Exception) {
                                e.printStackTrace()
                            }
                        }
                        // Le token a expired
                        401 -> {
                            listener!!.retourLogin()
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

        path = "$EXPLORERS_URL/${preferences.getString("username", "")}/location"

        path.httpGet()
                .header(mapOf("Authorization" to "Bearer ${preferences.getString("token", "")}"))
                .responseJson { request, response, result ->

                    when (response.statusCode) {
                        200 -> {
                            try {
                                val json = result.get()
                                val position = json.obj()
                                val positionObj = JSON.nonstrict.parse(PositionExploration.serializer(), position.toString())

                                // On update la position du joueur
                                positionJoueur = PointF(positionObj.coordonnees.x.toFloat(), positionObj.coordonnees.y.toFloat())
                                positionnerBouton()
                            } catch (e : Exception) {
                                e.printStackTrace()
                            }
                        }
                        // Le token a expired
                        401 -> {
                            listener!!.retourLogin()
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
        // Gérer le scaling de l'image
        ScaleDetector = ScaleGestureDetector(this.context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                val scale = imgMap.scaleX * detector!!.scaleFactor

                // Limiter le zoom
                if (scale < 1) {
                    imgMap.scaleX = 1f
                    imgMap.scaleY = 1f
                } else if (scale > 5) {
                    imgMap.scaleX = 5f
                    imgMap.scaleY = 5f
                } else {
                    imgMap.scaleX = scale
                    imgMap.scaleY = scale
                }

                // On doit repositionner, car la zone affichée à l'écran a changée
                positionnerBouton()

                return true
            }
        })

        // Obtenir la taille actuelle de l'image
        // Je suis pas sûr à 100% pourquoi, mais l'image de base fait 1044px de large, et sur l'émulateur que je testais, elle faisait 1800px de large
        // Je crois qu'ils l'allongent pour que son Dpi match celui du cellulaire, mais je suis pas sûr.
        // Mais comme le serveur va nous renvoyer des coordonnées par rapport à la vraie taille de l'image,
        // on a besoin de connaître la vraie taille de l'image pour faire la comparaison
        val optBitmap = BitmapFactory.Options()
        optBitmap.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT
        val bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.andromia, optBitmap)
        tailleImage = PointF(bmp.width.toFloat(), bmp.height.toFloat())

        // Pour positionner le bouton lorsqu'on load le fragment pour la première fois
        // Je le fais ici car j'ai besoin de la matrix de l'image
        imgMap.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                imgMap.viewTreeObserver.removeOnGlobalLayoutListener(this)

                positionnerBouton()
            }
        })

        // OnClick du scan
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
        // On obtient les coordonnées X, Y d'où on a cliqué
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
        // Gérer le double tap

        // On ne veut pas partir une nouvelle animation si on est déjà en train de zoomer
        if (!setAnimation.isRunning) {
            val scaleAnimation : ObjectAnimator?
            val btnScaleAnimation : ObjectAnimator?
            var newScale = 0f
            var newTransX = 0f
            var newTransY = 0f

            // Zoom In
            if (imgMap.scaleX == 1f && imgMap.scaleY == 1f) {
                val BaseScale = TypedValue()

                resources.getValue(R.dimen.BASE_SCALE, BaseScale, true)

                val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, imgMap.scaleX, BaseScale.float)
                val pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, imgMap.scaleY, BaseScale.float)

                // Obtenir la zone où l'utilisateur a cliqué
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
            }
            // Zoom Out
            else {
                val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, imgMap.scaleX, 1f)
                val pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, imgMap.scaleY, 1f)

                // On recentre
                val trX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, imgMap.translationX, 0f)
                val trY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, imgMap.translationY, 0f)

                scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(imgMap, pvhX, pvhY, trX, trY)

                newScale = 1f
                newTransX = 0f
                newTransY = 0f
            }

            if (scaleAnimation != null) {
                // Obtenier le point central de l'écran
                val matrix = FloatArray(9)
                imgMap.imageMatrix.getValues(matrix)
                val pointCentral = obtenirPointCentral(newScale, newScale, newTransX, newTransY, matrix)

                // Animation du bouton du joueur
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

                    handler.postDelayed({listener!!.onPortalScanned(result.contents, explorerObj!!)}, 1500)
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun zoomIn(x : Float, y : Float, baseScale : Float) : PointF? {
        // TODO : Améliorer la précision du zoom in, on ne zoom pas exactement où l'utilisateur a cliqué

        // On obtient la position de la view à l'écran, puisque les positions X,Y du click sont selon l'écran complet
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
        // Retourne la taille de la carte affichée à l'écran
        val matrix = FloatArray(9)
        imgMap.imageMatrix.getValues(matrix)

        val widthMap = (matrix[Matrix.MSCALE_X] * imgMap.drawable.intrinsicWidth)
        val heightMap = (matrix[Matrix.MSCALE_Y] * imgMap.drawable.intrinsicHeight)
        return PointF(widthMap, heightMap)
    }

    private fun obtenirPointCentral(newScaleX : Float, newScaleY : Float, newTransX : Float, newTransY: Float, matrix : FloatArray) : PointF {
        // Obtient le point central affiché à l'écran selon la scale et les translations données
        return PointF((imgMap.pivotX) - matrix[Matrix.MTRANS_X] - (newTransX / newScaleX),
                (imgMap.pivotY) - matrix[Matrix.MTRANS_Y] - (newTransY / newScaleY))
    }

    private fun verifierTranslationValide(trans : Float, transHori : Boolean) : Boolean {
        // Vérifie si la translation demandée est valide
        val curr: Float
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
        // Repositionne le bouton du joueur à l'écran
        val matrix = FloatArray(9)
        imgMap.imageMatrix.getValues(matrix)

        val pointCentral = obtenirPointCentral(imgMap.scaleX, imgMap.scaleY, imgMap.translationX, imgMap.translationY, matrix)
        btnPosition.translationX = ((positionJoueur.x * (imgMap.pivotX * 2 - matrix[Matrix.MTRANS_X] * 2) / tailleImage.x) - pointCentral.x) * imgMap.scaleX
        btnPosition.translationY = ((positionJoueur.y * (imgMap.pivotY * 2 - matrix[Matrix.MTRANS_Y] * 2) / tailleImage.y) - pointCentral.y) * imgMap.scaleY

        return true
    }


    interface OnFragmentInteractionListener {
        fun utilisateurCharge(utilisateur: Explorer)
        fun utilisateurExistant() : Boolean
        fun retourLogin()
        fun onPortalScanned(uuid : String, explorer : Explorer)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MapFragment()
    }
}

package ca.qc.cstj.andromia.Activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Matrix
import android.graphics.PointF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.util.TypedValue
import ca.qc.cstj.andromia.R
import kotlinx.android.synthetic.main.activity_map.*
import android.util.DisplayMetrics
import android.graphics.BitmapFactory
import android.view.*


class MapActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private val positionJoueur = PointF(540f, 361f)
    private var tailleImage = PointF()
    private var x1 : Float = 0f
    private var x2 : Float? = null
    private var y1 : Float = 0f
    private var y2 : Float? = null
    private var setAnimation = AnimatorSet()
    private var gDetector: GestureDetectorCompat? = null
    private var ScaleDetector : ScaleGestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        gDetector = GestureDetectorCompat(this, this)

        gDetector?.setOnDoubleTapListener(this)
        ScaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
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
    }

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        x1 = p0!!.getX(0)
        y1 = p0!!.getY(0)
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event!!.action) {
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1) {
                    val newX = event.getX(0)
                    val newY = event.getY(0)
                    val scrollX = (newX - x1)
                    val scrollY = (newY - y1)

                    if (verifierTranslationValide(scrollX, true)) {
                        imgMap.translationX  += scrollX
                        x1 = newX
                    }

                    if (verifierTranslationValide(scrollY, false)) {
                        imgMap.translationY += scrollY
                        y1 = newY
                    }

                    positionnerBouton()
                }
                else {
                    ScaleDetector!!.onTouchEvent(event)
                }
            }
            else -> {
                gDetector?.onTouchEvent(event)
                ScaleDetector!!.onTouchEvent(event)
            }
        }

        return super.onTouchEvent(event)
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

    private fun positionnerBouton() {
        val matrix = FloatArray(9)
        imgMap.imageMatrix.getValues(matrix)

        val pointCentral = obtenirPointCentral(imgMap.scaleX, imgMap.scaleY, imgMap.translationX, imgMap.translationY, matrix)
        btnPosition.translationX = ((positionJoueur.x * (imgMap.pivotX * 2 - matrix[Matrix.MTRANS_X] * 2) / tailleImage.x) - pointCentral.x) * imgMap.scaleX
        btnPosition.translationY = ((positionJoueur.y * (imgMap.pivotY * 2 - matrix[Matrix.MTRANS_Y] * 2) / tailleImage.y) - pointCentral.y) * imgMap.scaleY
    }
}

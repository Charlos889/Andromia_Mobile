package ca.qc.cstj.andromia.Activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import ca.qc.cstj.andromia.R
import ca.qc.cstj.andromia.R.id.imgMap
import kotlinx.android.synthetic.main.activity_map.*
import kotlin.math.absoluteValue

class MapActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private var x1 : Float = 0f
    private var x2 : Float? = null
    private var y1 : Float = 0f
    private var y2 : Float? = null
    private var setAnimation = AnimatorSet()

    private var gDetector: GestureDetectorCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        gDetector = GestureDetectorCompat(this, this)

        gDetector?.setOnDoubleTapListener(this)
        ScaleDetector = ScaleGestureDetector(this, ScaleListener)
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

            if (imgMap.scaleX == 1f && imgMap.scaleY == 1f) {
                var BaseScale = TypedValue()

                resources.getValue(R.dimen.BASE_SCALE, BaseScale, true)

                val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, imgMap.scaleX, BaseScale.float)
                val pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, imgMap.scaleY, BaseScale.float)

                val point = ZoomIn(p0!!.x, p0.y, BaseScale)
                if (point != null) {
                    val trX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, imgMap.translationX, point.x)
                    val trY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, imgMap.translationY, point.y)

                    scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(imgMap, pvhX, pvhY, trX, trY)
                } else {
                    scaleAnimation = null
                }
            } else {
                val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, imgMap.scaleX, 1f)
                val pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, imgMap.scaleY, 1f)

                val trX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, imgMap.translationX, 0f)
                val trY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, imgMap.translationY, 0f)

                scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(imgMap, pvhX, pvhY, trX, trY)
            }

            setAnimation = AnimatorSet()
            setAnimation.play(scaleAnimation)
            setAnimation.duration = 1000
            setAnimation.start()
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

                    imgMap.translationX  += scrollX
                    imgMap.translationY += scrollY
                    x1 = newX
                    y1 = newY
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

    private fun ZoomIn(x : Float, y : Float, baseScale : TypedValue) : PointF? {
        val posView = IntArray(2)
        imgMap.getLocationOnScreen(posView)

        val matrix = FloatArray(9)
        imgMap.imageMatrix.getValues(matrix)

        val posX = x - (posView[0] + matrix[Matrix.MTRANS_X])
        val posY = y - (posView[1] + matrix[Matrix.MTRANS_Y])

        val widthMap = (matrix[Matrix.MSCALE_X] * imgMap.drawable.intrinsicWidth)
        val heightMap = (matrix[Matrix.MSCALE_Y] * imgMap.drawable.intrinsicHeight)

        if (posX >= 0
            && posY >= 0
            && posX <= widthMap
            && posY <= heightMap)
            return PointF(((widthMap / 2) * baseScale.float) - (posX * baseScale.float)
                            , ((heightMap / 2) * baseScale.float) - (posY * baseScale.float))
        else
            return null
    }

    private val ScaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            val scale = imgMap.scaleX * detector!!.scaleFactor

            if (scale >= 1f) {
                imgMap.scaleX = scale
                imgMap.scaleY = scale
            } else {
                imgMap.scaleX = 1f
                imgMap.scaleY = 1f
            }
            return true
        }
    }

    private var ScaleDetector : ScaleGestureDetector? = null;
}

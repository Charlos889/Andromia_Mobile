package ca.qc.cstj.andromia.Activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import ca.qc.cstj.andromia.R
import kotlinx.android.synthetic.main.activity_map.*
import java.time.Duration
import java.util.*

class MapActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private var x1 : Float = 0f
    private var x2 : Float = 0f
    private var y1 : Float = 0f
    private var y2 : Float = 0f

    private var gDetector: GestureDetectorCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        /*ctlMap.setOnTouchListener(View.OnTouchListener { v, event ->

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    //txtTest.text = imgMap.scrollBy(-100, -100).toString()
                }

            }

            return@OnTouchListener true
        })*/

        gDetector = GestureDetectorCompat(this, this)

        gDetector?.setOnDoubleTapListener(this)
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
        val pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, imgMap.scaleX, 1f)
        val pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, imgMap.scaleY, 1f)
        val scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(imgMap, pvhX, pvhY)

        val setAnimation = AnimatorSet()
        setAnimation.play(scaleAnimation)
        setAnimation.duration = 1000
        setAnimation.start()
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
                    val scrollX = (newX - x1) / 2
                    val scrollY = (newY - y1) / 2

                    imgMap.scrollBy(-scrollX.toInt(), -scrollY.toInt())
                    x1 = newX
                    y1 = newY
                } else if (event.pointerCount == 2) {

                }
            }
            else -> {
                gDetector?.onTouchEvent(event)
            }
        }

        return super.onTouchEvent(event)
    }
}

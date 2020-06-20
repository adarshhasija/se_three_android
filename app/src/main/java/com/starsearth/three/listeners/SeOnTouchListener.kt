package com.starsearth.two.listeners

import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.*

class SeOnTouchListener(val context : OnSeTouchListenerInterface?) : View.OnTouchListener {

    var mListener : OnSeTouchListenerInterface? = null

    init {
        mListener = context
    }

    private var x1: Float = 0.toFloat()
    private var x2:Float = 0.toFloat()
    private var y1:Float = 0.toFloat()
    private var y2:Float = 0.toFloat()
    private var actionDownTimestamp : Long = 0
    internal val MIN_DISTANCE = 150

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.getX()
                y1 = event.getY()
                actionDownTimestamp = Calendar.getInstance().timeInMillis
            }
            MotionEvent.ACTION_UP -> {
                val actionUpTimestamp = Calendar.getInstance().timeInMillis
                x2 = event.getX()
                y2 = event.getY()
                val deltaX = x2 - x1
                val deltaY = y2 - y1
                if (Math.abs(deltaX) < MIN_DISTANCE && -deltaY > MIN_DISTANCE) {
                    mListener?.gestureSwipeUp()
                }
                else if (-deltaX > MIN_DISTANCE && Math.abs(deltaY) < MIN_DISTANCE) {
                    mListener?.gestureSwipeLeft()
                }
                else if (Math.abs(deltaX) > MIN_DISTANCE || Math.abs(deltaY) > MIN_DISTANCE) {
                    //We do not want to recognize other swipes right now
                }
                else if (Math.abs(actionUpTimestamp - actionDownTimestamp) > 500) {
                   // mListener?.gestureLongPress()
                }
                else {
                    mListener?.gestureTap()
                }
            }
        }
        return true
    }

    interface OnSeTouchListenerInterface {
        fun gestureTap()
        fun gestureSwipeUp()
        fun gestureSwipeLeft()
        fun gestureLongPress()
    }

}
package com.example.fairchildfreecell

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

internal class CardGestureListener(
    private val view: View,
    private val card: Card,
    private val location: CardLocation,
    private val onSwipeDown: (Card, CardLocation) -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        view.performClick()
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 != null) {
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            if (abs(diffY) > abs(diffX)) {
                if (diffY > 0) {
                    onSwipeDown(card, location)
                    return true
                }
            }
        }
        return false
    }
}
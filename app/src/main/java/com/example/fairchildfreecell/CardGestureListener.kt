package com.example.fairchildfreecell

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

internal class CardGestureListener(
    private val view: View,
    private val card: Card,
    private val location: CardLocation,
    private val onDoubleClick: (Card, CardLocation) -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        view.performClick()
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        onDoubleClick(card, location)
        return true
    }
}
package com.example.fairchildfreecell

import android.view.GestureDetector
import android.view.MotionEvent

private class CardGestureListener(
    private val card: Card,
    private val section: GameSection,
    private val column: Int,
    private val onSingleClick: (Card, GameSection, Int) -> Unit,
    private val onDoubleClick: (Card, GameSection, Int) -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        onSingleClick(card, section, column)
        return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        onDoubleClick(card, section, column)
        return true
    }
}
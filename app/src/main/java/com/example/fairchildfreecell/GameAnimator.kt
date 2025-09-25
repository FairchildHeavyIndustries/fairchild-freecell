package com.example.fairchildfreecell

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.createBitmap
import androidx.core.view.isNotEmpty
import java.util.LinkedList
import java.util.Queue
import kotlin.math.roundToInt

class GameAnimator(
    private val activity: Activity,
    private val cardViewMap: Map<Card, View>,
    private val cardWidth: Int,
    private val cardHeight: Int
) {

    private val rootLayout = activity.findViewById<ConstraintLayout>(R.id.rootLayout)
    private val animationQueue: Queue<MoveEvent> = LinkedList()
    private val animationHandler = Handler(Looper.getMainLooper())
    private var onMoveCompleteCallback: ((MoveEvent) -> Unit)? = null

    fun animateMoves(
        moves: List<MoveEvent>,
        fastDraw: Boolean = false,
        onMoveComplete: (move: MoveEvent) -> Unit
    ) {
        this.onMoveCompleteCallback = onMoveComplete
        animationQueue.addAll(moves)
        // If the queue was empty, start the animation process immediately.
        if (animationQueue.size == moves.size) {
            animateNextMoveInQueue(fastDraw)
        }
    }

    private fun animateNextMoveInQueue(fastDraw: Boolean = false) {
        if (animationQueue.isNotEmpty()) {
            val moveEvent = animationQueue.poll()
            if (moveEvent != null) {
                // The original animateMove function is now private and renamed.
                performAnimation(moveEvent, fastDraw)
            }
        }
    }

    private fun performAnimation(moveEvent: MoveEvent, fastDraw: Boolean = false) {
        val topCardOfStack = moveEvent.cards.first()
        val originalView = cardViewMap[topCardOfStack] ?: return

        val startCoordinates = getStartCoordinates(originalView)
        prepareLayoutsForAnimation(moveEvent)
        val destinationCoordinates = getDestinationCoordinates(moveEvent)

        val viewToAnimate: View
        val isStack = moveEvent.cards.size > 1

        if (isStack) {
            // Create a container for the stack of cards
            val stackContainer = LinearLayout(activity)
            stackContainer.orientation = LinearLayout.VERTICAL

            // Add all the cards in the stack to the container
            moveEvent.cards.forEachIndexed { index, card ->
                val cardView = cardViewMap[card]!!
                (cardView.parent as? android.view.ViewGroup)?.removeView(cardView) // Detach from old parent
                val layoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight)
                if (index > 0) {
                    layoutParams.topMargin = -(cardHeight * 0.65).roundToInt()
                }
                cardView.layoutParams = layoutParams
                stackContainer.addView(cardView)
            }
            val stackHeight =
                (cardHeight + (moveEvent.cards.size - 1) * (cardHeight * 0.35)).toInt()
            stackContainer.measure(
                View.MeasureSpec.makeMeasureSpec(cardWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(stackHeight, View.MeasureSpec.EXACTLY)
            )
            stackContainer.layout(0, 0, cardWidth, stackHeight)
            viewToAnimate = stackContainer
        } else {
            viewToAnimate = originalView
        }

        // Create a bitmap copy of the view to animate.
        val bitmap = createBitmapFromView(viewToAnimate)
        val ghostView = ImageView(activity)
        ghostView.setImageBitmap(bitmap)
        ghostView.layoutParams = ConstraintLayout.LayoutParams(bitmap.width, bitmap.height)


        // 3. Add the ghost to the root layout at the starting position.
        rootLayout.addView(ghostView)
        ghostView.x = startCoordinates[0].toFloat()
        ghostView.y = startCoordinates[1].toFloat()

        // 4. Make the original view(s) invisible during the animation.
        moveEvent.cards.forEach { card ->
            cardViewMap[card]?.visibility = View.INVISIBLE
        }

        // 5. Animate the ghost view.
        ghostView.animate()
            .x(destinationCoordinates[0].toFloat())
            .y(destinationCoordinates[1].toFloat())
            .setDuration(if (fastDraw) 10 else 250)
            .withEndAction {
                rootLayout.removeView(ghostView)
                finalizeMove(moveEvent)
                moveEvent.cards.forEach { card ->
                    cardViewMap[card]?.visibility = View.VISIBLE
                }
                onMoveCompleteCallback?.invoke(moveEvent)

                // Check if there are more moves to animate.
                if (animationQueue.isNotEmpty()) {
                    // Post the next animation with a 300ms delay.
                    animationHandler.postDelayed({
                        animateNextMoveInQueue(fastDraw)
                    }, if (fastDraw) 20L else 100L)
                }
            }
            .start()
    }

    private fun createBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
        )
        val bitmap = createBitmap(view.measuredWidth, view.measuredHeight)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
    }

    private fun getStartCoordinates(view: View): IntArray {
        val startCoordinates = IntArray(2)
        view.getLocationOnScreen(startCoordinates)
        return startCoordinates
    }

    private fun getDestinationCoordinates(moveEvent: MoveEvent): IntArray {
        val destParent = findParentLayout(activity, moveEvent.destination)
        val destinationCoordinates = IntArray(2)
        val targetView: View

        when (moveEvent.destination.section) {
            GameSection.BOARD -> {
                targetView = if (destParent.isNotEmpty()) {
                    destParent.getChildAt(destParent.childCount - 1)
                } else {
                    destParent
                }
                targetView.getLocationOnScreen(destinationCoordinates)
                if (destParent.isNotEmpty()) {
                    // Offset to simulate stacking on top.
                    destinationCoordinates[1] += (targetView.height * 0.35).roundToInt()
                }
            }

            GameSection.FREECELL, GameSection.FOUNDATION -> {
                targetView = destParent.getChildAt(moveEvent.destination.columnIndex)
                targetView.getLocationOnScreen(destinationCoordinates)
            }
        }
        return destinationCoordinates
    }

    private fun prepareLayoutsForAnimation(moveEvent: MoveEvent) {
        // Remove all moved cards from their source parent to make way for the animation.
        moveEvent.cards.forEach { card ->
            val cardView = cardViewMap[card]
            (cardView?.parent as? LinearLayout)?.removeView(cardView)
        }

        // Repair the source pile if it was a Free Cell or Foundation to show what's underneath.
        when (moveEvent.source.section) {
            GameSection.FREECELL -> {
                val sourceParent = findParentLayout(activity, moveEvent.source)
                sourceParent.addView(
                    createPlaceholderView(activity, cardWidth, cardHeight),
                    moveEvent.source.columnIndex
                )
            }

            GameSection.FOUNDATION -> {
                val sourceParent = findParentLayout(activity, moveEvent.source)
                val movedCard = moveEvent.cards.first()
                if (movedCard.value == Value.ACE) {
                    sourceParent.addView(
                        createPlaceholderView(activity, cardWidth, cardHeight),
                        moveEvent.source.columnIndex
                    )
                } else {
                    val valueUnderneath = Value.entries[movedCard.value.ordinal - 1]
                    val cardUnderneath = Card(valueUnderneath, movedCard.suit)
                    val viewUnderneath = cardViewMap[cardUnderneath]!!
                    (viewUnderneath.parent as? android.view.ViewGroup)?.removeView(viewUnderneath)
                    sourceParent.addView(viewUnderneath, moveEvent.source.columnIndex)
                }
            }

            else -> {}
        }
    }

    private fun finalizeMove(moveEvent: MoveEvent) {
        val destParent = findParentLayout(activity, moveEvent.destination)
        // Add all moved cards (including the one that was animated) to the final destination layout.
        moveEvent.cards.forEach { card ->
            val cardView = cardViewMap[card]!!
            // Reset view properties before re-parenting.
            cardView.x = 0f
            cardView.y = 0f
            cardView.translationX = 0f
            cardView.translationY = 0f

            (cardView.parent as? android.view.ViewGroup)?.removeView(cardView)

            val newLayoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight)
            if (moveEvent.destination.section == GameSection.BOARD && destParent.isNotEmpty()) {
                newLayoutParams.topMargin = -(cardHeight * 0.65).roundToInt()
            }
            cardView.layoutParams = newLayoutParams

            when (moveEvent.destination.section) {
                GameSection.BOARD -> destParent.addView(cardView)
                GameSection.FREECELL, GameSection.FOUNDATION -> {
                    val destIndex = moveEvent.destination.columnIndex
                    destParent.removeViewAt(destIndex)
                    destParent.addView(cardView, destIndex)
                }
            }
        }
    }
}
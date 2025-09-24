package com.example.fairchildfreecell

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isNotEmpty
import java.util.LinkedList
import java.util.Queue
import kotlin.math.roundToInt

class GameView(private val activity: Activity, private val gameActions: GameActions) {

    private val cardViewMap = mutableMapOf<Card, View>()
    private val rootLayout = activity.findViewById<ConstraintLayout>(R.id.rootLayout)
    private val freeCellLayout = activity.findViewById<LinearLayout>(R.id.freeCellLayout)
    private val foundationLayout = activity.findViewById<LinearLayout>(R.id.foundationLayout)
    private var restartButton: ImageButton = activity.findViewById(R.id.restartButton)
    private var undoButton: ImageButton = activity.findViewById(R.id.undoButton)
    private var newGameButton: ImageButton = activity.findViewById(R.id.newGameButton)
    private var cardWidth = 0
    private var cardHeight = 0
    private val boardColumnIds = listOf(
        R.id.boardColumn1, R.id.boardColumn2, R.id.boardColumn3, R.id.boardColumn4,
        R.id.boardColumn5, R.id.boardColumn6, R.id.boardColumn7, R.id.boardColumn8
    )

    private val animationQueue: Queue<MoveEvent> = LinkedList()
    private val animationHandler = Handler(Looper.getMainLooper())
    private var onCardTapCallback: ((Card, GameSection, Int) -> Unit)? = null

    init {
        hideSystemUI()
        restartButton.setOnClickListener {
            gameActions.onRestartClicked()
        }
        undoButton.setOnClickListener {
            gameActions.onUndoClicked()
        }
        newGameButton.setOnClickListener {
            gameActions.onNewGameClicked()
        }
        calculateCardDimensions()
    }

    fun drawNewGame(
        gameState: GameState,
        onCardTap: (Card, GameSection, Int) -> Unit
    ) {
        cardViewMap.clear()
        drawTopLayouts()
        drawBoard(gameState, cardWidth, cardHeight, onCardTap)
        drawGameNumber(gameState.gameNumber)
    }

    fun animateMoves(moves: List<MoveEvent>, fastDraw: Boolean = false, onCardTap: (Card, GameSection, Int) -> Unit) {
        onCardTapCallback = onCardTap
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
    fun performAnimation(moveEvent: MoveEvent, fastDraw: Boolean = false) {
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
            val stackHeight = (cardHeight + (moveEvent.cards.size - 1) * (cardHeight * 0.35)).toInt()
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
            .setDuration(if (fastDraw)  10 else 250)
            .withEndAction {
                rootLayout.removeView(ghostView)
                finalizeMove(moveEvent)
                moveEvent.cards.forEach { card ->
                    cardViewMap[card]?.visibility = View.VISIBLE
                }
                // Use the stored callback for updating click listeners.
                updateClickListeners(moveEvent, onCardTapCallback!!)

                // Check if there are more moves to animate.
                if (animationQueue.isNotEmpty()) {
                    // Post the next animation with a 300ms delay.
                    animationHandler.postDelayed({
                        animateNextMoveInQueue()
                    }, if (fastDraw)  20L else 100L)
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
        val destParent = findParentLayout(moveEvent.destination)
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
                val sourceParent = findParentLayout(moveEvent.source)
                sourceParent.addView(createPlaceholderView(), moveEvent.source.columnIndex)
            }
            GameSection.FOUNDATION -> {
                val sourceParent = findParentLayout(moveEvent.source)
                val movedCard = moveEvent.cards.first()
                if (movedCard.value == Value.ACE) {
                    sourceParent.addView(createPlaceholderView(), moveEvent.source.columnIndex)
                } else {
                    val valueUnderneath = Value.entries[movedCard.value.ordinal - 1]
                    val cardUnderneath = Card(valueUnderneath, movedCard.suit)
                    val viewUnderneath = cardViewMap[cardUnderneath]!!
                    sourceParent.addView(viewUnderneath, moveEvent.source.columnIndex)
                }
            }
            else -> {}
        }
    }

    private fun finalizeMove(moveEvent: MoveEvent) {
        val destParent = findParentLayout(moveEvent.destination)
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

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun createPlaceholderView(): ImageView {
        val placeholder = ImageView(activity)
        placeholder.setBackgroundResource(R.drawable.placeholder_background)
        placeholder.layoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight)
        return placeholder
    }

    private fun findParentLayout(location: CardLocation): LinearLayout {
        return when (location.section) {
            GameSection.BOARD -> activity.findViewById(boardColumnIds[location.columnIndex - 1])
            GameSection.FREECELL -> freeCellLayout
            GameSection.FOUNDATION -> foundationLayout
        }
    }

    private fun calculateCardDimensions() {
        // Get the width of the screen in pixels
        val screenWidth = activity.resources.displayMetrics.widthPixels

        // We have 8 columns. Let's assume a small 4dp margin on each side of the board
        val boardPadding = (8 * activity.resources.displayMetrics.density).roundToInt()
        val availableWidth = screenWidth - boardPadding

        // Calculate the width for a single card to fit 8 across
        cardWidth = availableWidth / 8

        // Calculate height based on a standard card aspect ratio (e.g., 1.4)
        cardHeight = (cardWidth * 1.4).roundToInt()
    }

    private fun drawGameNumber(gameNumber: Int) {
        val gameNumberTextView = activity.findViewById<TextView>(R.id.gameNumberTextView)
        gameNumberTextView.text = "$gameNumber"
    }

    // In GameView.kt

    private fun drawTopLayouts() {
        freeCellLayout.removeAllViews()
        foundationLayout.removeAllViews()

        (0..3).forEach { i ->
            freeCellLayout.addView(createPlaceholderView())
            foundationLayout.addView(createPlaceholderView())
        }
    }


    private fun populateCardView(cardView: View, card: Card) {
        val valueTextView = cardView.findViewById<TextView>(R.id.valueTextView)
        val suitTextView = cardView.findViewById<TextView>(R.id.suitTextView)
        val suitTopRightTextView = cardView.findViewById<TextView>(R.id.suitSmallTextView)

        valueTextView.text = getValueString(card.value)
        suitTextView.text = getSuitString(card.suit)
        suitTopRightTextView.text = getSuitString(card.suit)

        val color = if (card.suit == Suit.DIAMONDS || card.suit == Suit.HEARTS) {
            ContextCompat.getColor(activity, R.color.red_card)
        } else {
            ContextCompat.getColor(activity, R.color.black_card)
        }
        valueTextView.setTextColor(color)
        suitTextView.setTextColor(color)
        suitTopRightTextView.setTextColor(color)
    }

    private fun updateClickListeners(
        moveEvent: MoveEvent,
        onCardTap: (Card, GameSection, Int) -> Unit
    ) {
        moveEvent.cards.forEach { card ->
            val cardView = cardViewMap[card] ?: return@forEach
            if (moveEvent.destination.section != GameSection.FOUNDATION) {
                cardView.setOnClickListener {
                    onCardTap(
                        card,
                        moveEvent.destination.section,
                        moveEvent.destination.columnIndex
                    )
                }
            } else {
                cardView.setOnClickListener(null) // Foundation cards are not clickable.
            }
        }

        // Set listener on the newly exposed card in the source pile.
        if (moveEvent.source.section == GameSection.BOARD) {
            val sourceParent = findParentLayout(moveEvent.source)
            if (sourceParent.isNotEmpty()) {
                val exposedView = sourceParent.getChildAt(sourceParent.childCount - 1)
                cardViewMap.entries.find { it.value == exposedView }?.key?.let { exposedCard ->
                    exposedView.setOnClickListener {
                        onCardTap(
                            exposedCard,
                            GameSection.BOARD,
                            moveEvent.source.columnIndex
                        )
                    }
                }
            }
        }
    }

    private fun drawBoard(
        gameState: GameState,
        cardWidth: Int,
        cardHeight: Int,
        onCardTap: (Card, GameSection, Int) -> Unit
    ) {
        boardColumnIds.forEachIndexed { index, columnId ->
            val pileNum = index + 1
            val boardColumn = activity.findViewById<LinearLayout>(columnId)
            boardColumn.removeAllViews()

            val pile = gameState.boardPiles[pileNum]
            pile?.forEach { card ->
                val cardView =
                    LayoutInflater.from(activity).inflate(R.layout.card_layout, boardColumn, false)
                val layoutParams = cardView.layoutParams as LinearLayout.LayoutParams
                layoutParams.width = cardWidth
                layoutParams.height = cardHeight
                if (boardColumn.isNotEmpty()) {
                    layoutParams.topMargin = -(cardHeight * 0.65).roundToInt()
                }
                cardView.layoutParams = layoutParams
                populateCardView(cardView, card)
                cardView.tag = card

                // Every card gets a click listener.
                cardView.setOnClickListener { onCardTap(card, GameSection.BOARD, pileNum) }
                cardViewMap[card] = cardView
                boardColumn.addView(cardView)
            }
        }
    }

    private fun getValueString(value: Value): String {
        return when (value) {
            Value.ACE -> "A"
            Value.JACK -> "J"
            Value.QUEEN -> "Q"
            Value.KING -> "K"
            else -> (value.ordinal + 1).toString()
        }
    }

    private fun getSuitString(suit: Suit): String {
        return when (suit) {
            Suit.CLUBS -> "♣"
            Suit.DIAMONDS -> "♦"
            Suit.HEARTS -> "♥"
            Suit.SPADES -> "♠"
        }
    }
}
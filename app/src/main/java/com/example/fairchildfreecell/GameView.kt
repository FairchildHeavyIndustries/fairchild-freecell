package com.example.fairchildfreecell

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty


import kotlin.math.roundToInt

class GameView(private val activity: Activity, private val gameActions: GameActions) {

    private val cardViewMap = mutableMapOf<Card, View>()
    private val freeCellLayout = activity.findViewById<LinearLayout>(R.id.freeCellLayout)
    private val foundationLayout = activity.findViewById<LinearLayout>(R.id.foundationLayout)
    private var restartButton: ImageButton = activity.findViewById(R.id.restartButton)
    private var undoButton: ImageButton = activity.findViewById(R.id.undoButton)

    private var cardWidth = 0
    private var cardHeight = 0

    // A list of the actual resource IDs for our tableau columns
    private val boardColumnIds = listOf(
        R.id.tableauColumn1, R.id.tableauColumn2, R.id.tableauColumn3, R.id.tableauColumn4,
        R.id.tableauColumn5, R.id.tableauColumn6, R.id.tableauColumn7, R.id.tableauColumn8
    )
    init {
        restartButton.setOnClickListener {
            gameActions.onRestartClicked() // Call the interface method
        }
        undoButton.setOnClickListener {
            gameActions.onUndoClicked()
        }
        calculateCardDimensions()
    }
    fun drawNewGame(
        gameState: GameState,
        onCardTap: (Card, GameSection, Int) -> Unit
    ) {
        cardViewMap.clear()
        drawTopLayouts()
        drawBoard(gameState, cardWidth, cardHeight,  onCardTap)
        drawGameNumber(gameState.gameNumber)
    }
    fun updateViewForMove(moveEvent: MoveEvent, onCardTap: (Card, GameSection, Int) -> Unit) {
        val sourceParent = findParentLayout(moveEvent.source)
        val destParent = findParentLayout(moveEvent.destination)

        // Restore placeholder if the source was a free cell.
        if (moveEvent.source.section == GameSection.FREECELL) {
            sourceParent.removeViewAt(moveEvent.source.columnIndex)
            sourceParent.addView(createPlaceholderView(), moveEvent.source.columnIndex)
        }

        // Move each card's view.
        moveEvent.cards.forEach { card ->
            val cardView = cardViewMap[card] ?: return@forEach
            val parent = cardView.parent as? LinearLayout
            parent?.removeView(cardView)

            // Reset margins before adding to a new parent.
            val layoutParams = cardView.layoutParams as LinearLayout.LayoutParams
            layoutParams.topMargin = 0

            // Apply new margins if moving to a board pile.
            if (moveEvent.destination.section == GameSection.BOARD && destParent.isNotEmpty()) {
                layoutParams.topMargin = -(cardHeight * 0.65).roundToInt()
            }
            cardView.layoutParams = layoutParams

            when (moveEvent.destination.section) {
                GameSection.BOARD -> {
                    destParent.addView(cardView)
                }
                GameSection.FREECELL, GameSection.FOUNDATION -> {
                    // Replace the placeholder at the destination index.
                    val destIndex = moveEvent.destination.columnIndex
                    destParent.removeViewAt(destIndex)
                    destParent.addView(cardView, destIndex)
                }
            }
        }
        updateClickListeners(moveEvent, onCardTap)
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

        // We have 8 columns. Let's assume a small 4dp margin on each side of the tableau
        val tableauPadding = (8 * activity.resources.displayMetrics.density).roundToInt()
        val availableWidth = screenWidth - tableauPadding

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

        valueTextView.text = getRankString(card.rank)
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

    private fun updateClickListeners(moveEvent: MoveEvent, onCardTap: (Card, GameSection, Int) -> Unit) {
        // Set listener on the top card of the destination pile.
        val destParent = findParentLayout(moveEvent.destination)
        val destIndex = moveEvent.destination.columnIndex
        if (destParent.isNotEmpty()) {
            // Determine which view to update based on the layout type.
            val topView = if (moveEvent.destination.section == GameSection.BOARD) {
                // For multi-card tableau stacks, the top card is always the last one.
                destParent.getChildAt(destParent.childCount - 1)
            } else {
                // For single-slot layouts (Freecell/Foundation), use the specific index.
                destParent.getChildAt(destIndex)
            }

            val topCard = cardViewMap.entries.find { it.value == topView }?.key
            if (topCard != null) {
                // Assign the listener with the correct source section and index.
                if (moveEvent.destination.section != GameSection.FOUNDATION) {
                    topView.setOnClickListener { onCardTap(topCard, moveEvent.destination.section, destIndex) }
                } else {
                    topView.setOnClickListener(null) // Foundation cards are not clickable.
                }
            }
        }
        // Set listener on the newly exposed card in the source pile.
        if (moveEvent.source.section == GameSection.BOARD) {
            val sourceParent = findParentLayout(moveEvent.source)
            if (sourceParent.isNotEmpty()) {
                val exposedView = sourceParent.getChildAt(sourceParent.childCount - 1)
                cardViewMap.entries.find { it.value == exposedView }?.key?.let { exposedCard ->
                    exposedView.setOnClickListener { onCardTap(exposedCard, GameSection.BOARD, moveEvent.source.columnIndex) }
                }
            }
        }
    }
    private fun drawBoard(
        gameState: GameState, cardWidth: Int, cardHeight: Int, onCardTap: (Card, GameSection, Int) -> Unit
    ) {
        boardColumnIds.forEachIndexed { index, columnId ->
            val pileNum = index + 1
            val boardColumn = activity.findViewById<LinearLayout>(columnId)
            boardColumn.removeAllViews()

            val pile = gameState.boardPiles[pileNum]
            pile?.forEach { card ->
                val cardView = LayoutInflater.from(activity).inflate(R.layout.card_layout, boardColumn, false)
                val layoutParams = cardView.layoutParams as LinearLayout.LayoutParams
                layoutParams.width = cardWidth
                layoutParams.height = cardHeight
                if (boardColumn.isNotEmpty()) {
                    layoutParams.topMargin = -(cardHeight * 0.65).roundToInt()
                }
                cardView.layoutParams = layoutParams
                populateCardView(cardView, card)

                // Every card gets a click listener.
                cardView.setOnClickListener { onCardTap(card, GameSection.BOARD, pileNum) }
                cardViewMap[card] = cardView
                boardColumn.addView(cardView)
            }
        }
    }

    private fun getRankString(rank: Rank): String {
        return when (rank) {
            Rank.ACE -> "A"
            Rank.JACK -> "J"
            Rank.QUEEN -> "Q"
            Rank.KING -> "K"
            else -> (rank.ordinal + 1).toString()
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
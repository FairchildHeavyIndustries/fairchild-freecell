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
        calculateCardDimensions()
    }
    fun drawNewGame(
        gameState: GameState,
        onCardTap: (Card, GameSection, Int) -> Unit
    ) {
        cardViewMap.clear()
        drawTopLayouts(cardWidth, cardHeight)
        drawBoard(gameState, cardWidth, cardHeight,  onCardTap)
        drawGameNumber(gameState.gameNumber)
    }
    fun updateViewForMove( moveEvent: MoveEvent) {
        val cardView = cardViewMap[moveEvent.card] ?: return // Find the view for the moved card

        // 1. Remove the view from its old parent
        val sourceParent = findParentLayout(moveEvent.source)
        sourceParent.removeView(cardView)

        // 2. Add the view to its new parent
        val destParent = findParentLayout(moveEvent.destination)
        when (moveEvent.destination.section) {
            GameSection.BOARD -> {
                // For board piles, adding to the end is correct.
                destParent.addView(cardView)
            }
            GameSection.FREECELL, GameSection.FOUNDATION -> {
                // For free cells and foundations, we replace the view at the destination index.
                val destinationIndex = moveEvent.destination.columnIndex

                // Remove the placeholder (or existing card) at the target spot.
                destParent.removeViewAt(destinationIndex)

                // Add the new card view at that same spot.
                destParent.addView(cardView, destinationIndex)
            }
        }

        // 3. Update placeholders and click listeners (simplified)
        // - If sourceParent is now empty, add a placeholder.
        // - If destParent previously had a placeholder, remove it.
        // - Update the click listener on the new top card of the source pile.
        // ... this logic needs to be built out ...
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

    private fun drawTopLayouts(cardWidth: Int, cardHeight: Int) {
        freeCellLayout.removeAllViews()
        foundationLayout.removeAllViews()

        // A helper function to avoid repeating the placeholder creation code
        fun createPlaceholder(): ImageView {
            val placeholder = ImageView(activity)
            placeholder.setBackgroundResource(R.drawable.placeholder_background)
            val layoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight)
            placeholder.layoutParams = layoutParams
            return placeholder
        }

        // Draw 4 placeholders for each top layout
        (0..3).forEach { i ->
            freeCellLayout.addView(createPlaceholder())
            foundationLayout.addView(createPlaceholder())
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
            if (pile != null && pile.isNotEmpty()) {
                // Find the last card in the pile before the loop starts.
                val lastCardInPile = pile.last()

                for (card in pile) {
                    val cardView = LayoutInflater.from(activity).inflate(R.layout.card_layout, boardColumn, false)

                    val layoutParams = cardView.layoutParams as LinearLayout.LayoutParams
                    layoutParams.width = cardWidth
                    layoutParams.height = cardHeight

                    if (boardColumn.isNotEmpty()) {
                        val overlap = (cardHeight * 0.65).roundToInt()
                        layoutParams.topMargin = -overlap
                    }
                    cardView.layoutParams = layoutParams

                    populateCardView(cardView, card)
                    cardViewMap[card] = cardView

                    // Only the top card in the stack (the last one) gets a click listener.
                    if (card == lastCardInPile) {
                        cardView.setOnClickListener {
                            onCardTap(card, GameSection.BOARD, pileNum)
                        }
                    }

                    boardColumn.addView(cardView)
                }
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
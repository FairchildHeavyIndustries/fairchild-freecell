package com.example.fairchildfreecell

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat


import kotlin.math.roundToInt

class GameView(private val activity: Activity, private val gameActions: GameActions) {

    // Get references to the top layout containers
    private val freeCellLayout = activity.findViewById<LinearLayout>(R.id.freeCellLayout)
    private val foundationLayout = activity.findViewById<LinearLayout>(R.id.foundationLayout)
    // We can call this automatically when the view is created
    private var restartButton: ImageButton = activity.findViewById(R.id.restartButton)
    private var cardWidth = 0
    private var cardHeight = 0

    // A list of the actual resource IDs for our tableau columns
    private val tableauColumnIds = listOf(
        R.id.tableauColumn1, R.id.tableauColumn2, R.id.tableauColumn3, R.id.tableauColumn4,
        R.id.tableauColumn5, R.id.tableauColumn6, R.id.tableauColumn7, R.id.tableauColumn8
    )
    init {
        restartButton.setOnClickListener {
            gameActions.onRestartClicked() // Call the interface method
        }
        calculateCardDimensions()
    }
    fun drawGameState(
        gameState: GameState,
        onCardClick: (Card, String, Int) -> Unit
    ) {
        drawTopLayouts(gameState, cardWidth, cardHeight,  onCardClick)
        drawTableau(gameState, cardWidth, cardHeight,  onCardClick)
        drawGameNumber(gameState.gameNumber)
    }

    fun calculateCardDimensions() {
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

    private fun drawTopLayouts(
        gameState: GameState,
        cardWidth: Int,
        cardHeight: Int,
        onFreeCellCardClick: (Card, String, Int) -> Unit
    ) {
        freeCellLayout.removeAllViews()
        foundationLayout.removeAllViews()

        // Draw the Free Cell piles
        for (i in 0..3) {
            val card = gameState.freeCellPiles[i]
            if (card != null) {
                // If a card exists, create and add a card view
                val cardView = LayoutInflater.from(activity).inflate(R.layout.card_layout, freeCellLayout, false)
                val layoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight)
                cardView.layoutParams = layoutParams
                cardView.setOnClickListener {
                    onFreeCellCardClick(card, "freecell", i)
                }
                populateCardView(cardView, card)
                freeCellLayout.addView(cardView)
            } else {
                // If the cell is empty, draw a placeholder
                val placeholder = ImageView(activity)
                placeholder.setBackgroundResource(R.drawable.placeholder_background)
                val layoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight)
                placeholder.layoutParams = layoutParams
                freeCellLayout.addView(placeholder)
            }
        }

        // Draw the Foundation piles
        for (i in 0..3) {
            val foundationPile = gameState.foundationPiles[i]
            val topCard = foundationPile?.lastOrNull()
            if (topCard != null) {
                // If the pile is not empty, draw the top card
                val cardView = LayoutInflater.from(activity).inflate(R.layout.card_layout, foundationLayout, false)
                val layoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight)
                cardView.layoutParams = layoutParams
                populateCardView(cardView, topCard)
                foundationLayout.addView(cardView)
            } else {
                // If the pile is empty, draw a placeholder
                val placeholder = ImageView(activity)
                placeholder.setBackgroundResource(R.drawable.placeholder_background)
                val layoutParams = LinearLayout.LayoutParams(cardWidth, cardHeight)
                placeholder.layoutParams = layoutParams
                foundationLayout.addView(placeholder)
            }
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


    private fun drawTableau(
        gameState: GameState,
        cardWidth: Int,
        cardHeight: Int,
        onCardClick: (Card, String, Int) -> Unit
    ) {
        tableauColumnIds.forEachIndexed { index, columnId ->
            val pileNum = index + 1
            val tableauColumn = activity.findViewById<LinearLayout>(columnId)
            tableauColumn.removeAllViews()

            val pile = gameState.tableauPiles[pileNum]
            if (pile != null && pile.isNotEmpty()) {
                // Find the last card in the pile before the loop starts.
                val lastCardInPile = pile.last()

                for (card in pile) {
                    val cardView = LayoutInflater.from(activity).inflate(R.layout.card_layout, tableauColumn, false)

                    val layoutParams = cardView.layoutParams as LinearLayout.LayoutParams
                    layoutParams.width = cardWidth
                    layoutParams.height = cardHeight

                    if (tableauColumn.childCount > 0) {
                        val overlap = (cardHeight * 0.65).roundToInt()
                        layoutParams.topMargin = -overlap
                    }
                    cardView.layoutParams = layoutParams

                    populateCardView(cardView, card)
                    // Only the top card in the stack (the last one) gets a click listener.
                    if (card == lastCardInPile) {
                        cardView.setOnClickListener {
                            onCardClick(card, "tableau", pileNum)
                        }
                    }

                    tableauColumn.addView(cardView)
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
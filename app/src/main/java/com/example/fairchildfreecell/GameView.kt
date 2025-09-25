package com.example.fairchildfreecell

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isNotEmpty
import kotlin.math.roundToInt

class GameView(private val activity: Activity, private val gameActions: GameActions) {

    private val cardViewMap = mutableMapOf<Card, View>()
    private val freeCellLayout = activity.findViewById<LinearLayout>(R.id.freeCellLayout)
    private val foundationLayout = activity.findViewById<LinearLayout>(R.id.foundationLayout)
    private var restartButton: ImageButton = activity.findViewById(R.id.restartButton)
    private var undoButton: ImageButton = activity.findViewById(R.id.undoButton)
    private var newGameButton: ImageButton = activity.findViewById(R.id.newGameButton)
    private var cardWidth = 0
    private var cardHeight = 0

    private val gameAnimator: GameAnimator

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
        gameAnimator = GameAnimator(activity, cardViewMap, cardWidth, cardHeight)
    }

    fun drawNewGame(
        gameState: GameState,
        onCardTap: (Card, CardLocation) -> Unit
    ) {
        cardViewMap.clear()
        drawTopLayouts()
        drawBoard(gameState, cardWidth, cardHeight, onCardTap)
        drawGameNumber(gameState.gameNumber)
    }

    fun animateMoves(
        moves: List<MoveEvent>,
        fastDraw: Boolean = false,
        onCardTap: (Card, CardLocation) -> Unit,
        onCardDoubleTapped: (Card, CardLocation) -> Unit
    ) {
        gameAnimator.animateMoves(moves, fastDraw) { moveEvent ->
            updateClickListeners(moveEvent, onCardTap)
        }
    }


    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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

    private fun drawTopLayouts() {
        freeCellLayout.removeAllViews()
        foundationLayout.removeAllViews()

        (0..3).forEach { _ ->
            freeCellLayout.addView(createPlaceholderView(activity, cardWidth, cardHeight))
            foundationLayout.addView(createPlaceholderView(activity, cardWidth, cardHeight))
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
        onCardTap: (Card, CardLocation) -> Unit
    ) {
        moveEvent.cards.forEach { card ->
            val cardView = cardViewMap[card] ?: return@forEach
            if (moveEvent.destination.section != GameSection.FOUNDATION) {
                cardView.setOnClickListener {
                    onCardTap(card, moveEvent.destination)
                }
            } else {
                cardView.setOnClickListener(null) // Foundation cards are not clickable.
            }
        }

        // Set listener on the newly exposed card in the source pile.
        if (moveEvent.source.section == GameSection.BOARD) {
            val sourceParent = findParentLayout(activity, moveEvent.source)
            if (sourceParent.isNotEmpty()) {
                val exposedView = sourceParent.getChildAt(sourceParent.childCount - 1)
                cardViewMap.entries.find { it.value == exposedView }?.key?.let { exposedCard ->
                    exposedView.setOnClickListener {
                        onCardTap(exposedCard, moveEvent.source)
                    }
                }
            }
        }
    }

    private fun drawBoard(
        gameState: GameState,
        cardWidth: Int,
        cardHeight: Int,
        onCardTap: (Card, CardLocation) -> Unit
    ) {
        BOARD_COLUMN_IDS.forEachIndexed { index, columnId ->
            val pileNum = index + 1
            val boardColumn = activity.findViewById<LinearLayout>(columnId)
            boardColumn.removeAllViews()

            val pile = gameState.boardPiles[pileNum]
            pile?.forEach { card ->
                val cardView =
                    LayoutInflater.from(activity)
                        .inflate(R.layout.card_layout, boardColumn, false)
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
                cardView.setOnClickListener {
                    onCardTap(
                        card,
                        CardLocation(GameSection.BOARD, pileNum)
                    )
                }
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
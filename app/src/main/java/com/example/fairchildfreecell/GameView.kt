package com.example.fairchildfreecell

import android.annotation.SuppressLint
import android.app.Activity
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isNotEmpty
import kotlin.math.roundToInt
import androidx.core.view.isVisible

class GameView(private val activity: Activity, private val gameActions: GameActions) {

    private val cardViewMap = mutableMapOf<Card, View>()
    private val freeCellLayout = activity.findViewById<LinearLayout>(R.id.freeCellLayout)
    private val foundationLayout = activity.findViewById<LinearLayout>(R.id.foundationLayout)
    private var restartButton: ImageButton
    private var undoButton: ImageButton
    private var newGameButton: ImageButton
    private var moreOptionsButton: ImageButton
    private var settingsButton: ImageButton
    private var hintButton: ImageButton
    private var saveGameButton: ImageButton
    private var moreOptionsMenu: LinearLayout
    private var cardWidth = 0
    private var cardHeight = 0

    private val gameAnimator: GameAnimator

    init {
        hideSystemUI()
        restartButton = activity.findViewById(R.id.restartButton)
        undoButton = activity.findViewById(R.id.undoButton)
        newGameButton = activity.findViewById(R.id.newGameButton)
        moreOptionsButton = activity.findViewById(R.id.moreOptionsButton)
        settingsButton = activity.findViewById(R.id.settingsButton)
        hintButton = activity.findViewById(R.id.helpButton)
        saveGameButton = activity.findViewById(R.id.saveGameButton)
        moreOptionsMenu = activity.findViewById(R.id.moreOptionsMenu)

        restartButton.setOnClickListener { gameActions.onRestartClicked() }
        undoButton.setOnClickListener { gameActions.onUndoClicked() }
        newGameButton.setOnClickListener { gameActions.onNewGameClicked() }
        moreOptionsButton.setOnClickListener { gameActions.onMoreOptionsTapped() }
        settingsButton.setOnClickListener { gameActions.onSettingsTapped() }
        hintButton.setOnClickListener { gameActions.onHelpTapped() }
        saveGameButton.setOnClickListener { gameActions.onSaveGameTapped() }

        calculateCardDimensions()
        gameAnimator = GameAnimator(activity, cardViewMap, cardWidth, cardHeight)
    }

    fun toggleMoreOptions() {
        val animationDuration = 500L
        if (moreOptionsMenu.isVisible) {
            val slideDown = TranslateAnimation(0f, 0f, 0f, moreOptionsMenu.height.toFloat())
            val fadeOut = AlphaAnimation(1f, 0f)
            fadeOut.interpolator = DecelerateInterpolator(2f) // Starts fast, ends slow

            val animationSet = AnimationSet(true)
            animationSet.addAnimation(slideDown)
            animationSet.addAnimation(fadeOut)
            animationSet.duration = animationDuration
            animationSet.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    moreOptionsMenu.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            moreOptionsMenu.startAnimation(animationSet)
        } else {
            moreOptionsMenu.visibility = View.VISIBLE
            val slideUp = TranslateAnimation(0f, 0f, moreOptionsMenu.height.toFloat(), 0f)
            val fadeIn = AlphaAnimation(0f, 1f)
            fadeIn.interpolator = AccelerateInterpolator(1f) // Starts slow, ends fast

            val animationSet = AnimationSet(true)
            animationSet.addAnimation(slideUp)
            animationSet.addAnimation(fadeIn)
            animationSet.duration = animationDuration
            moreOptionsMenu.startAnimation(animationSet)
        }
    }


    fun setBottomButtonsEnabled(isEnabled: Boolean) {
        newGameButton.isEnabled = isEnabled
        restartButton.isEnabled = isEnabled
        undoButton.isEnabled = isEnabled
    }

    fun drawNewGame(
        gameState: GameState,
        onCardTap: (Card, CardLocation) -> Unit,
        onCardSwipedDown: (Card, CardLocation) -> Unit
    ) {
        cardViewMap.clear()
        drawTopLayouts()
        drawBoard(gameState, cardWidth, cardHeight, onCardTap, onCardSwipedDown)
        drawGameNumber(gameState.gameNumber)
    }

    fun animateMoves(
        moves: List<MoveEvent>,
        fastDraw: Boolean = false,
        onCardTap: (Card, CardLocation) -> Unit,
        onCardSwipedDown: (Card, CardLocation) -> Unit,
        onAllMovesComplete: () -> Unit
    ) {
        gameAnimator.animateMoves(
            moves,
            fastDraw,
            { moveEvent -> updateClickListeners(moveEvent, onCardTap, onCardSwipedDown) },
            onAllMovesComplete
        )
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

    @SuppressLint("ClickableViewAccessibility")
    private fun updateClickListeners(
        moveEvent: MoveEvent,
        onCardTap: (Card, CardLocation) -> Unit,
        onCardSwipedDown: (Card, CardLocation) -> Unit
    ) {
        moveEvent.cards.forEach { card ->
            val cardView = cardViewMap[card] ?: return@forEach
            if (moveEvent.destination.section != GameSection.FOUNDATION) {
                val location = moveEvent.destination
                cardView.setOnClickListener { onCardTap(card, location) }
                val gestureDetector = GestureDetector(activity, CardGestureListener(
                    view = cardView,
                    card = card,
                    location = location,
                    onSwipeDown = onCardSwipedDown
                ))
                cardView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
            } else {
                cardView.setOnClickListener(null)
                cardView.setOnTouchListener(null)
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun drawBoard(
        gameState: GameState,
        cardWidth: Int,
        cardHeight: Int,
        onCardTap: (Card, CardLocation) -> Unit,
        onCardSwipedDown: (Card, CardLocation) -> Unit
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
                    layoutParams.topMargin = getBoardCardTopMargin(cardHeight)
                }
                cardView.layoutParams = layoutParams
                populateCardView(cardView, card)
                cardView.tag = card

                val location = CardLocation(GameSection.BOARD, pileNum)

                // Set standard click listener for single taps
                cardView.setOnClickListener { onCardTap(card, location) }

                // Set up GestureDetector for double taps and to trigger performClick
                val gestureDetector = GestureDetector(activity, CardGestureListener(
                    view = cardView,
                    card = card,
                    location = location,
                    onSwipeDown = onCardSwipedDown
                ))
                cardView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

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
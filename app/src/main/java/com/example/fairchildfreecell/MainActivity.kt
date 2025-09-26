package com.example.fairchildfreecell

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), GameActions {
    private var currentGameNumber = 0
    private lateinit var gameState: GameState
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gameView = GameView(this, this)
        startNewGame()
    }

    override fun onRestartClicked() {
        restartCurrentGame()
    }


    override fun onNewGameClicked() {
        startNewGame()
    }

    override fun onCardTapped(card: Card, sourceLocation: CardLocation) {
        handleMove(
            gameState.moveCard(
                clickedCard = card,
                sourceLocation = sourceLocation,
                quality = MoveQuality.BEST
            )
        )
    }

    override fun onCardSwipedDown(card: Card, sourceLocation: CardLocation) {
        handleMove(
            gameState.moveCard(
                clickedCard = card,
                sourceLocation = sourceLocation,
                quality = MoveQuality.SECOND_BEST
            )
        )
    }

    override fun onUndoClicked() {
        val undoMoveEvent = gameState.undoLastMove()

        if (undoMoveEvent != null) {
            gameView.animateMoves(
                listOf(undoMoveEvent),
                fastDraw = false,
                this::onCardTapped,
                onCardSwipedDown = this::onCardSwipedDown
            ) {}
        }
    }

    private fun handleMove(allMoveEvents: List<MoveEvent>) {
        if (allMoveEvents.isNotEmpty()) {
            if (gameState.isGameWon) {
                gameView.setBottomButtonsEnabled(false)
            }
            gameView.animateMoves(
                allMoveEvents,
                gameState.isGameWon,
                this::onCardTapped,
                onCardSwipedDown = this::onCardSwipedDown
            ) {
                if (gameState.isGameWon) {
                    gameView.setBottomButtonsEnabled(true)
                }
            }
        }
    }

    private fun refreshGameView() {
        gameView.drawNewGame(gameState, this::onCardTapped, this::onCardSwipedDown)
    }

    private fun startNewGame() {
        currentGameNumber = (1..32000).random()
//        currentGameNumber = 77777
        restartCurrentGame()
    }

    private fun restartCurrentGame() {
        this.gameState = if (currentGameNumber == 77777) {
            GameState(testState = TestGameStates.kingsDownToAcesState)
        } else {
            GameState(currentGameNumber)
        }
        refreshGameView()
        gameView.setBottomButtonsEnabled(true)
    }
}
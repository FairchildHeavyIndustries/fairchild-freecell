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

    override fun onUndoClicked() {
        val undoMoveEvent = gameState.undoLastMove()

        if (undoMoveEvent != null) {
            gameView.animateMoves(listOf(undoMoveEvent), this::onCardTapped)
        }
    }

    private fun onCardTapped(card: Card, sourceSection: GameSection, column: Int) {
        val allMoveEvents = gameState.moveCard(card, sourceSection, column)
        if (allMoveEvents.isNotEmpty()) {
            gameView.animateMoves(allMoveEvents, this::onCardTapped)
           }
    }

    private fun refreshGameView() {
        gameView.drawNewGame(gameState, this::onCardTapped)
    }

    private fun startNewGame() {
        currentGameNumber = (1..32000).random()
        restartCurrentGame()
    }

    private fun restartCurrentGame() {
//                gameState = GameState(2)
        if (currentGameNumber == 77777) {
            gameState = GameState(TestGameStates.exampleState)
        } else {
            gameState = GameState(currentGameNumber)
        }
        refreshGameView()
    }


}
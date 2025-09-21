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
            gameView.updateViewForMove(undoMoveEvent, this::onCardTapped)
        }
    }

    private fun onCardTapped(card: Card, sourceSection: GameSection, column: Int) {
        val moveEvent = gameState.moveCard(card, sourceSection, column)
        if (moveEvent != null) {
            gameView.updateViewForMove(moveEvent, this::onCardTapped)
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
        //        gameState = GameState(23092)
        gameState = GameState(currentGameNumber)
        refreshGameView()
    }






}
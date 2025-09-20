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
        TODO("Not yet implemented")
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

    private fun onCardTapped(card: Card, sourceSection: GameSection, column: Int) {
        val bestMove = gameState.findBestMove(card, sourceSection)

        if (bestMove != null) {
            gameState.moveCard(card, sourceSection, column, bestMove)
            refreshGameView()
        }
    }


    private fun refreshGameView() {
        gameView.drawGameState(gameState, this::onCardTapped)
    }




}
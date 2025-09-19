package com.example.fairchildfreecell

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var currentGameNumber = 0
    private lateinit var gameState: GameState
    private lateinit var gameView: GameView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startNewGame()
    }

    private fun startNewGame() {
        currentGameNumber = (1..32000).random()
//        gameState = GameState(currentGameNumber)
        gameState = GameState(23092)
        gameView = GameView(this)
        refreshGameView()
    }

    private fun onTableauPileTapped(card: Card, pileNum: Int) {
        gameState.performAutoMove(card, "tableau", pileNum)
        refreshGameView()

    }

    private fun onFreeCellCardTapped(card: Card, freeCellNum: Int) {
        gameState.performAutoMove(card, "freecell", freeCellNum)
        refreshGameView()
    }


    private fun refreshGameView() {
        gameView.drawGameState(
            gameState,
            this::onTableauPileTapped,
            this::onFreeCellCardTapped
        )
    }


}
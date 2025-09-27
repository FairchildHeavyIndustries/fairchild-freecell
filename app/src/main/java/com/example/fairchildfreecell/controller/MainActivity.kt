package com.example.fairchildfreecell.controller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fairchildfreecell.R
import com.example.fairchildfreecell.views.GameView
import com.example.fairchildfreecell.model.settings.SavedGameManager
import com.example.fairchildfreecell.model.Card
import com.example.fairchildfreecell.model.CardLocation
import com.example.fairchildfreecell.model.GameState
import com.example.fairchildfreecell.model.MoveCommand
import com.example.fairchildfreecell.model.MoveEvent
import com.example.fairchildfreecell.model.TestGameStates

class MainActivity : AppCompatActivity(), GameActions {
    private var currentGameNumber = 0
    private lateinit var gameState: GameState
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gameView = GameView(activity = this, gameActions = this)
        SavedGameManager.init(context = this) //
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
                moveCommand = MoveCommand.BEST
            )
        )
    }

    override fun onCardSwipedDown(card: Card, sourceLocation: CardLocation) {
        handleMove(
            gameState.moveCard(
                clickedCard = card,
                sourceLocation = sourceLocation,
                moveCommand = MoveCommand.BOARD
            )
        )
    }

    override fun onCardSwipedUp(card: Card, sourceLocation: CardLocation) {
        handleMove(
            gameState.moveCard(
                clickedCard = card,
                sourceLocation = sourceLocation,
                moveCommand = MoveCommand.FREECELL
            )
        )
    }

    override fun onUndoClicked() {
        val undoMoveEvent = gameState.undoLastMove()

        if (undoMoveEvent != null) {
            gameView.animateMoves(
                moves = listOf(undoMoveEvent),
                fastDraw = false,
                onCardTap = this::onCardTapped,
                onCardSwipedDown = this::onCardSwipedDown,
                onCardSwipedUp = this::onCardSwipedUp
            ) {}
        }
    }

    override fun onMoreOptionsTapped() {
        gameView.toggleMoreOptions()
    }

    override fun onSettingsTapped() {
        // TODO: Implement settings screen
    }

    override fun onHelpTapped() {
        // TODO: Implement help screen
    }

    override fun onSaveGameTapped() {
        SavedGameManager.toggleSaveGame(currentGameNumber)
        gameView.updateSaveGameButton(isSaved = SavedGameManager.isGameSaved(currentGameNumber))
    }

    private fun handleMove(allMoveEvents: List<MoveEvent>) {
        if (allMoveEvents.isNotEmpty()) {
            if (gameState.isGameWon) {
                gameView.setBottomButtonsEnabled(false)
            }
            gameView.animateMoves(
                moves = allMoveEvents,
                fastDraw = gameState.isGameWon,
                onCardTap = this::onCardTapped,
                onCardSwipedDown = this::onCardSwipedDown,
                onCardSwipedUp = this::onCardSwipedUp
            ) {
                if (gameState.isGameWon) {
                    gameView.setBottomButtonsEnabled(true)
                }
            }
        }
    }

    private fun refreshGameView() {
        gameView.drawNewGame(
            gameState = gameState,
            onCardTap = this::onCardTapped,
            onCardSwipedDown = this::onCardSwipedDown,
            onCardSwipedUp = this::onCardSwipedUp
        )
    }

    private fun startNewGame() {
        currentGameNumber = (1..32000).random()
//        currentGameNumber = 1115
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
        gameView.updateSaveGameButton(isSaved = SavedGameManager.isGameSaved(currentGameNumber))
    }
}
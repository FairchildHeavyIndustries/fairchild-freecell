package com.example.fairchildfreecell

interface GameActions {
    fun onCardTapped(card: Card, sourceLocation: CardLocation)
    fun onCardDoubleTapped(card: Card, sourceLocation: CardLocation)
    fun onRestartClicked()

    fun onUndoClicked()
    fun onNewGameClicked()
}
package com.example.fairchildfreecell

interface GameActions {
    fun onCardTapped(card: Card, sourceLocation: CardLocation)
    fun onCardSwipedDown(card: Card, sourceLocation: CardLocation)
    fun onRestartClicked()
    fun onUndoClicked()
    fun onNewGameClicked()
    fun onMoreOptionsTapped()
    fun onSettingsTapped()
    fun onHelpTapped()
    fun onSaveGameTapped()
}
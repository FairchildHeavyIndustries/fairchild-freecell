package com.example.fairchildfreecell.controller

import com.example.fairchildfreecell.model.Card
import com.example.fairchildfreecell.model.CardLocation

interface GameActions {
    fun onCardTapped(card: Card, sourceLocation: CardLocation)
    fun onCardSwipedDown(card: Card, sourceLocation: CardLocation)

    fun onCardSwipedUp(card: Card, sourceLocation: CardLocation)
    fun onRestartClicked()
    fun onUndoClicked()
    fun onNewGameClicked()
    fun onMoreOptionsTapped()
    fun onSettingsTapped()
    fun onHelpTapped()
    fun onSaveGameTapped()
}
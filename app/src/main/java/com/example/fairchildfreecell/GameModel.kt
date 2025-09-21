package com.example.fairchildfreecell

enum class GameSection {
    BOARD, FOUNDATION, FREECELL
}
data class CardLocation(
    val section: GameSection,
    val columnIndex: Int
)
enum class Suit {
    CLUBS, DIAMONDS, HEARTS, SPADES
}

enum class Rank {
    ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
}

data class MoveEvent(
    val cards: List<Card>,
    val source: CardLocation,
    val destination: CardLocation
)


data class Card(val suit: Suit, val rank: Rank)

object Deck {
    fun createDeck(): MutableList<Card> {
        val cards = mutableListOf<Card>()
        // Loop through each suit and each rank to create all 52 cards
        for (rank in Rank.entries) {
            for (suit in Suit.entries) {
                cards.add(Card(suit, rank))
            }
        }
        return cards
    }
}
package com.example.fairchildfreecell

// An enum is a perfect way to represent a fixed set of values like suits.
enum class Suit {
    CLUBS, DIAMONDS, HEARTS, SPADES
}

// We can do the same for card ranks.
enum class Rank {
    ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
}

// A data class holds the properties of a card. Each card has a suit and a rank.
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
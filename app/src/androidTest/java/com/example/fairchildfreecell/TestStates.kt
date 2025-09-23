package com.example.fairchildfreecell

object TestGameStates {

    val screenshotState = PresetGameState(
        freeCellPiles = mapOf(
            0 to null,
            1 to Card(Rank.NINE, Suit.DIAMONDS),
            2 to Card(Rank.FOUR, Suit.DIAMONDS),
            3 to null
        ),
        foundationPiles = mapOf(
            0 to mutableListOf(
                Card(Rank.ACE, Suit.SPADES),
                Card(Rank.TWO, Suit.SPADES),
                Card(Rank.THREE, Suit.SPADES),
                Card(Rank.FOUR, Suit.SPADES)),
            1 to mutableListOf(
                Card(Rank.ACE, Suit.HEARTS),
                Card(Rank.TWO, Suit.HEARTS),
                Card(Rank.THREE, Suit.HEARTS)),
            2 to mutableListOf(
                Card(Rank.ACE, Suit.DIAMONDS),
                Card(Rank.TWO, Suit.DIAMONDS)),
            3 to mutableListOf(Card(Rank.ACE, Suit.CLUBS),
                Card(Rank.TWO, Suit.CLUBS))
        ),
        boardPiles = mapOf(
            1 to mutableListOf(
                Card(Rank.QUEEN, Suit.DIAMONDS)
            ),
            2 to mutableListOf(

                Card(Rank.JACK, Suit.CLUBS),
                Card(Rank.TEN, Suit.DIAMONDS)
            ),
            3 to mutableListOf(
                Card(Rank.KING, Suit.CLUBS),
                Card(Rank.JACK, Suit.SPADES),
                Card(Rank.EIGHT, Suit.CLUBS),
                Card(Rank.KING, Suit.SPADES),
                Card(Rank.TEN, Suit.CLUBS),
                Card(Rank.SEVEN, Suit.HEARTS),
                Card(Rank.TEN, Suit.HEARTS),
                Card(Rank.NINE, Suit.SPADES),
                Card(Rank.EIGHT, Suit.DIAMONDS),
                Card(Rank.SEVEN, Suit.SPADES),
                Card(Rank.SIX, Suit.DIAMONDS),
                Card(Rank.FIVE, Suit.SPADES),
                Card(Rank.FOUR, Suit.HEARTS)

            ),
            4 to mutableListOf(
                Card(Rank.THREE, Suit.CLUBS),
                Card(Rank.SIX, Suit.HEARTS),
                Card(Rank.SIX, Suit.CLUBS),
                Card(Rank.FIVE, Suit.DIAMONDS)
            ),
            5 to mutableListOf(),
            6 to mutableListOf(
                Card(Rank.KING, Suit.HEARTS),
                Card(Rank.QUEEN, Suit.SPADES),
                Card(Rank.JACK, Suit.HEARTS),
                Card(Rank.TEN, Suit.SPADES),
                Card(Rank.NINE, Suit.HEARTS),
                Card(Rank.EIGHT, Suit.SPADES),
                Card(Rank.SEVEN, Suit.DIAMONDS),
                Card(Rank.SIX, Suit.SPADES),
                Card(Rank.FIVE, Suit.HEARTS),
                Card(Rank.FOUR, Suit.SPADES),
                Card(Rank.THREE, Suit.DIAMONDS)
            ),
            7 to mutableListOf(
                Card(Rank.KING, Suit.DIAMONDS),
                Card(Rank.QUEEN, Suit.CLUBS),
                Card(Rank.JACK, Suit.DIAMONDS)

            ),
            8 to mutableListOf(
                Card(Rank.FIVE, Suit.CLUBS),
                Card(Rank.NINE, Suit.CLUBS),
                Card(Rank.QUEEN, Suit.HEARTS),
                Card(Rank.EIGHT, Suit.HEARTS),
                Card(Rank.SEVEN, Suit.SPADES),

            )
        )
    )
}

// A simple data class to hold the pile definitions for a test.

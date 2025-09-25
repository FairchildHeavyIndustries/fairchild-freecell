package com.example.fairchildfreecell

enum class GameSection {
    BOARD, FOUNDATION, FREECELL
}
data class Card(
    val value: Value,
    val suit: Suit) {
    val color: CardColor
        get() = suit.color
}
data class CardLocation(
    val section: GameSection,
    val columnIndex: Int
)
enum class Suit {
    CLUBS, DIAMONDS, HEARTS, SPADES;
    val color: CardColor
        get() = when (this) {
            HEARTS, DIAMONDS -> CardColor.RED
            else -> CardColor.BLACK
        }

}

enum class Value {
    ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
}

data class MoveEvent(
    val cards: List<Card>,
    val source: CardLocation,
    val destination: CardLocation
)

enum class MoveQuality {
    BEST, SECOND_BEST
}
enum class CardColor {
    RED, BLACK
}





data class PresetGameState(
    val freeCellPiles: Map<Int, Card?>,
    val foundationPiles: Map<Int, MutableList<Card>>,
    val boardPiles: Map<Int, MutableList<Card>>
)


object Deck {
    fun createDeck(): MutableList<Card> {
        val cards = mutableListOf<Card>()
        // Loop through each suit and each value to create all 52 cards
        for (value in Value.entries) {
            for (suit in Suit.entries) {
                cards.add(Card(value, suit))
            }
        }
        return cards
    }
}
object TestGameStates {

    val exampleState = PresetGameState(
        freeCellPiles = mapOf(
            0 to null,
            1 to Card(Value.NINE, Suit.DIAMONDS),
            2 to Card(Value.FOUR, Suit.DIAMONDS),
            3 to null
        ),
        foundationPiles = mapOf(
            0 to mutableListOf(
                Card(Value.ACE, Suit.SPADES),
                Card(Value.TWO, Suit.SPADES),
                Card(Value.THREE, Suit.SPADES),
                Card(Value.FOUR, Suit.SPADES)),
            1 to mutableListOf(
                Card(Value.ACE, Suit.HEARTS),
                Card(Value.TWO, Suit.HEARTS),
                Card(Value.THREE, Suit.HEARTS)),
            2 to mutableListOf(
                Card(Value.ACE, Suit.DIAMONDS),
                Card(Value.TWO, Suit.DIAMONDS)),
            3 to mutableListOf(Card(Value.ACE, Suit.CLUBS),
                Card(Value.TWO, Suit.CLUBS))
        ),
        boardPiles = mapOf(
            1 to mutableListOf(
                Card(Value.QUEEN, Suit.DIAMONDS)
            ),
            2 to mutableListOf(

                Card(Value.JACK, Suit.CLUBS),
                Card(Value.TEN, Suit.DIAMONDS)
            ),
            3 to mutableListOf(
                Card(Value.KING, Suit.CLUBS),
                Card(Value.JACK, Suit.SPADES),
                Card(Value.EIGHT, Suit.CLUBS),
                Card(Value.KING, Suit.SPADES),
                Card(Value.TEN, Suit.CLUBS),
                Card(Value.SEVEN, Suit.HEARTS),
                Card(Value.TEN, Suit.HEARTS),
                Card(Value.NINE, Suit.SPADES),
                Card(Value.EIGHT, Suit.DIAMONDS),
                Card(Value.SEVEN, Suit.SPADES),
                Card(Value.SIX, Suit.DIAMONDS),
                Card(Value.FIVE, Suit.SPADES),
                Card(Value.FOUR, Suit.HEARTS)

            ),
            4 to mutableListOf(
                Card(Value.THREE, Suit.CLUBS),
                Card(Value.SIX, Suit.HEARTS),
                Card(Value.SIX, Suit.CLUBS),
                Card(Value.FIVE, Suit.DIAMONDS)
            ),
            5 to mutableListOf(),
            6 to mutableListOf(
                Card(Value.KING, Suit.HEARTS),
                Card(Value.QUEEN, Suit.SPADES),
                Card(Value.JACK, Suit.HEARTS),
                Card(Value.TEN, Suit.SPADES),
                Card(Value.NINE, Suit.HEARTS),
                Card(Value.EIGHT, Suit.SPADES),
                Card(Value.SEVEN, Suit.DIAMONDS),
                Card(Value.SIX, Suit.SPADES),
                Card(Value.FIVE, Suit.HEARTS),
                Card(Value.FOUR, Suit.SPADES),
                Card(Value.THREE, Suit.DIAMONDS)
            ),
            7 to mutableListOf(
                Card(Value.KING, Suit.DIAMONDS),
                Card(Value.QUEEN, Suit.CLUBS),
                Card(Value.JACK, Suit.DIAMONDS)

            ),
            8 to mutableListOf(
                Card(Value.FIVE, Suit.CLUBS),
                Card(Value.NINE, Suit.CLUBS),
                Card(Value.QUEEN, Suit.HEARTS),
                Card(Value.EIGHT, Suit.HEARTS),
                Card(Value.SEVEN, Suit.SPADES),

                )
        )
    )
}


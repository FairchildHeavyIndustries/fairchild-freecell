package com.example.fairchildfreecell

import org.junit.Assert.*
import org.junit.Test

class MSDealGeneratorTest {



    /**
     * Test 2: Validates that the entire shuffle process produces the
     * correct final sequence of card integers for a known game number.
     * This tests the RNG, the initial deck order, and the shuffle loop together.
     */
    @Test
    fun getShuffledDeckFirstCardTest() {
        val shuffledDeck = MSDealGenerator.getShuffledDeck(1)
        val firstCard = Card(Value.JACK, Suit.DIAMONDS)


        // ASSERT
        // Check if the resulting shuffled integers match the expected sequence.
        assertEquals("The deck is incorrect.", firstCard, shuffledDeck[0] )
    }

    @Test
    fun getShuffledDeckLastCardTest() {
        val shuffledDeck = MSDealGenerator.getShuffledDeck(1)
        val lastCard = Card(Value.SIX, Suit.HEARTS)

        // ASSERT
        // Check if the resulting shuffled integers match the expected sequence.
        assertEquals("The deck is incorrect.",      lastCard, shuffledDeck[51] )
    }

    @Test
    fun moveCardTest() {
        val gameState = GameState(TestGameStates.screenshotState)
        val card = Card(Value.SIX, Suit.DIAMONDS)
        val sourceSection = GameSection.BOARD
        val sourceColumn = 3
        val moveEvent = gameState.moveCard(card, sourceSection, sourceColumn)
        assertNotNull(moveEvent)

    }
}
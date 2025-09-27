package com.example.fairchildfreecell

import com.example.fairchildfreecell.model.Card
import com.example.fairchildfreecell.model.MSDealGenerator
import com.example.fairchildfreecell.model.Suit
import com.example.fairchildfreecell.model.Value
import org.junit.Assert.assertEquals
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


}
package com.example.fairchildfreecell.model

object MSDealGenerator {

    internal class MSRandomNumGenerator(private var seed: Int) {
        fun rand(): Int {
            seed = seed * 214013 + 2531011

            return (seed ushr 16) and 0x7FFF
        }
    }

    fun getShuffledDeck(gameNumber: Int): MutableList<Card> {
        val originalDeck = Deck.createDeck()
        val finalDeck = mutableListOf<Card>()
        val rng = MSRandomNumGenerator(gameNumber)

        while (originalDeck.isNotEmpty()) {
            val randomIndex = rng.rand() % originalDeck.size
            finalDeck.add(originalDeck[randomIndex])
            originalDeck[randomIndex] = originalDeck[originalDeck.size - 1]
            originalDeck.removeAt(originalDeck.size - 1)
        }
        return finalDeck
    }
}
package com.example.fairchildfreecell
// In GameState.kt, at the top of the file before the class definition

sealed class MoveDestination {
    data class Tableau(val pileNum: Int) : MoveDestination()
    data class Foundation(val pileNum: Int) : MoveDestination()
    data class FreeCell(val cellNum: Int) : MoveDestination()
}
class GameState(val gameNumber: Int) {
    val tableauPiles = mutableMapOf<Int, MutableList<Card>>()
    val foundationPiles = mutableMapOf<Int, MutableList<Card>>()
    val freeCellPiles = mutableMapOf<Int, Card?>()


    init {
        // Get a shuffled deck using the classic Microsoft algorithm
        val deck = MSDealGenerator.getShuffledDeck(gameNumber)

        // Initialize and deal the cards
        initializePiles()
        dealCards(deck)
    }

    private fun initializePiles() {
        for (i in 0..3) {
            foundationPiles[i] = mutableListOf()
            freeCellPiles[i] = null
        }
        for (i in 1..8) {
            tableauPiles[i] = mutableListOf()
        }
    }

    private fun dealCards(deck: MutableList<Card>) {
        // Correct round-robin dealing for Freecell
        deck.forEachIndexed { i, card ->
            val pileNum = (i % 8) + 1
            tableauPiles[pileNum]?.add(card)
        }
    }

    fun performAutoMove(card: Card, sourcePile: String, sourceNum: Int): Boolean {
        // If the card is from a free cell, the logic is different
        if (sourcePile == "freecell") {
            // 1. TRY FOUNDATION (always highest priority)
            findBestFoundationMove(card)?.let { targetFoundation ->
                moveFreeCellToFoundation(sourceNum)
                return true
            }

            // 2. TRY TABLEAU (as per your new rule for free cell cards)
            findBestTableauMove(card, sourcePile)?.let { targetTableau ->
                moveFreeCellToTableau(sourceNum, targetTableau)
                return true
            }

            // 3. TRY A DIFFERENT FREECELL (if tableau fails)
            findBestFreeCellMove(sourcePile)?.let { targetFreeCell ->
                // This would move from one free cell to another, which isn't a useful auto-move.
                // We can leave this unimplemented as it doesn't progress the game.
            }

        } else if (sourcePile == "tableau") {
            // This is the original logic for cards starting in the tableau
            // 1. TRY FOUNDATION
            findBestFoundationMove(card)?.let { targetFoundation ->
                moveTableauToFoundation(card, sourceNum, targetFoundation)
                return true
            }

            // 2. TRY TABLEAU
            findBestTableauMove(card, sourcePile)?.let { targetTableau ->
                moveTableauToTableau(card, sourceNum, targetTableau)
                return true
            }

            // 3. TRY FREECELL
            findBestFreeCellMove(sourcePile)?.let { targetFreeCell ->
                moveTableauToFreeCell(card, sourceNum, targetFreeCell)
                return true
            }
        }

        return false // No valid move was found
    }

    private fun findBestFoundationMove(card: Card): Int? {
        // Special case for Aces
        if (card.rank == Rank.ACE) {
            // Find the first empty foundation pile
            return foundationPiles.entries.find { it.value.isEmpty() }?.key
        }

        // Find a foundation pile with a matching suit and rank one lower
        return foundationPiles.entries.find {
            val topCard = it.value.lastOrNull()
            topCard != null && topCard.suit == card.suit && topCard.rank.ordinal == card.rank.ordinal - 1
        }?.key
    }

    private fun findBestTableauMove(card: Card, sourcePile: String): Int? {
        // Find all valid destination piles, including empty ones.
        val validDestinations = tableauPiles.filter { entry ->
            val targetPile = entry.value
            if (targetPile.isEmpty() && sourcePile == "tableau" && card.rank == Rank.KING) {
                true
            } else if (targetPile.isEmpty() && sourcePile == "tableau") {
                false
            } else if (targetPile.isEmpty()) {
                true
            } else {
                // Rules for moving to a non-empty pile.
                val topCard = targetPile.last()
                val cardIsRed = card.suit == Suit.DIAMONDS || card.suit == Suit.HEARTS
                val destIsRed = topCard.suit == Suit.DIAMONDS || topCard.suit == Suit.HEARTS
                (cardIsRed != destIsRed) && (topCard.rank.ordinal == card.rank.ordinal + 1)
            }

        }



        // Apply the tie-breaker rule. We give empty piles a low score (-1)
        // so they are considered, but a stack with a high-value low card is preferred.
        return validDestinations.maxByOrNull {
            if (it.value.isEmpty()) {
                -1
            } else {
                it.value.minOf { c -> c.rank.ordinal }
            }
        }?.key
    }

    private fun findBestFreeCellMove(sourcePile: String): Int? {
        // Only move to a free cell if the source was the tableau
        if (sourcePile == "tableau") {
            return freeCellPiles.entries.find { it.value == null }?.key
        }
        return null
    }

    fun moveTableauToFreeCell(card: Card, fromTableauNum: Int, toFreeCellNum: Int): Boolean {
        // Rule: Is the target free cell empty?
        if (freeCellPiles[toFreeCellNum] == null) {
            tableauPiles[fromTableauNum]?.remove(card)
            freeCellPiles[toFreeCellNum] = card
            return true
        }
        return false // Target cell is not empty
    }

    fun moveFreeCellToFoundation(fromFreeCellNum: Int): Boolean {
        val card = freeCellPiles[fromFreeCellNum] ?: return false // Exit if no card in the cell

        // 1. Find the correct target foundation pile.
        // It's either a pile already started with the correct suit, or an empty pile if the card is an Ace.
        val targetPile = foundationPiles.values.find { pile ->
            pile.isNotEmpty() && pile.last().suit == card.suit
        } ?: if (card.rank == Rank.ACE) foundationPiles.values.find { it.isEmpty() } else null


        // 2. If a valid pile was found, check the rules and make the move.
        if (targetPile != null) {
            val topCard = targetPile.lastOrNull()

            // Rule: Can you place this card on the foundation pile?
            if ((topCard == null && card.rank == Rank.ACE) ||
                (topCard != null && card.rank.ordinal == topCard.rank.ordinal + 1)
            ) {

                freeCellPiles[fromFreeCellNum] = null // Empty the free cell
                targetPile.add(card)
                return true
            }
        }

        // 3. If no valid pile was found or the move was illegal, return false.
        return false
    }

    fun moveTableauToTableau(card: Card, fromPileNum: Int, toPileNum: Int): Boolean {
        val toPile = tableauPiles[toPileNum]


        if (toPile.isNullOrEmpty()) {
            return true
        }

        val topCardOfToPile = toPile.last()

        // Rule: Is the card being moved one rank lower than the card it's being placed on?
        val isRankLower = card.rank.ordinal == topCardOfToPile.rank.ordinal - 1

        // Rule: Are the cards different colors?
        val cardIsRed = card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS
        val destinationIsRed =
            topCardOfToPile.suit == Suit.HEARTS || topCardOfToPile.suit == Suit.DIAMONDS
        val isOppositeColor = cardIsRed != destinationIsRed

        // If both rules pass, make the move.
        if (isRankLower && isOppositeColor) {
            tableauPiles[fromPileNum]?.remove(card)
            tableauPiles[toPileNum]?.add(card)
            return true
        }

        return false // Move is invalid
    }

    fun moveTableauToFoundation(card: Card, fromTableauNum: Int, toFoundationNum: Int): Boolean {
        val foundationPile = foundationPiles[toFoundationNum]

        // Rule: Does the card's suit match the foundation pile we're trying to move to?
        // An empty foundation pile can accept any suit (the first card defines its suit).
        val topCard = foundationPile?.lastOrNull()
        if (topCard != null && topCard.suit != card.suit) {
            return false // Suit does not match
        }

        // Rule: Can you place this card on the foundation pile?
        if ((topCard == null && card.rank == Rank.ACE) ||
            (topCard != null && card.rank.ordinal == topCard.rank.ordinal + 1)
        ) {

            tableauPiles[fromTableauNum]?.remove(card)
            foundationPile?.add(card)
            return true
        }
        return false // Move is out of sequence
    }

    fun moveFreeCellToTableau(fromFreeCellNum: Int, toTableauNum: Int): Boolean {
        val card = freeCellPiles[fromFreeCellNum]
        val toPile = tableauPiles[toTableauNum]

        if (card != null && toPile != null) {

            // Apply the same rules as a tableau-to-tableau move
            val topCardOfToPile = toPile.lastOrNull()
            if (topCardOfToPile == null) {
                if (toPile.isEmpty()) {
                    freeCellPiles[fromFreeCellNum] = null
                    toPile.add(card)
                    return true
                }
                return false
            }

            val isRankLower = card.rank.ordinal == topCardOfToPile.rank.ordinal - 1
            val cardIsRed = card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS
            val destinationIsRed =
                topCardOfToPile.suit == Suit.HEARTS || topCardOfToPile.suit == Suit.DIAMONDS
            val isOppositeColor = cardIsRed != destinationIsRed

            if (isRankLower && isOppositeColor) {
                freeCellPiles[fromFreeCellNum] = null // Empty the free cell
                toPile.add(card)
                return true
            }
        }
        return false
    }


}
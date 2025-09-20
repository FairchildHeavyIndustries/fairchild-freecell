package com.example.fairchildfreecell
// In GameState.kt, at the top of the file before the class definition

enum class DestinationType {
    TABLEAU, FOUNDATION, FREECELL
}

data class MoveDestination(
    val type: DestinationType,
    val index: Int,
    val isEmpty: Boolean
)


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

    fun findBestMove(card: Card): MoveDestination? {
        // 1. Always prioritize moving to the foundation.
        findBestFoundationMove(card)?.let { return it }

        // 2. If no foundation move is found, find the best possible tableau and free cell moves.
        val bestTableauMove = findBestTableauMove(card)
        val bestFreeCellMove = findFirstEmptyFreecell()

        // 3. Apply the priority rules to decide which move to return.
        return when {
            // If only one type of move is available, return it.
            bestTableauMove != null && bestFreeCellMove == null -> bestTableauMove
            bestTableauMove == null && bestFreeCellMove != null -> bestFreeCellMove

            // If both are available, apply the special rules.
            bestTableauMove != null && bestFreeCellMove != null -> {
                if (card.rank == Rank.KING) {
                    bestTableauMove // Kings prioritize moving to the tableau.
                } else if (bestTableauMove.isEmpty) {
                    bestFreeCellMove // Other cards prioritize moving to a free cell.
                } else {
                    bestTableauMove
                }
            }

            // If no moves are available, return null.
            else -> null
        }
    }

    fun moveCard(card: Card, sourcePile: String, sourceNum: Int, destination: MoveDestination) {

        when (sourcePile) {
            "tableau" -> tableauPiles[sourceNum]?.remove(card)
            "freecell" -> freeCellPiles[sourceNum] = null
        }


        when (destination.type) {
            DestinationType.TABLEAU  -> tableauPiles[destination.index]?.add(card)
            DestinationType.FOUNDATION -> foundationPiles[destination.index]?.add(card)
            DestinationType.FREECELL -> freeCellPiles[destination.index] = card
        }
    }





    private fun findBestFoundationMove(card: Card): MoveDestination? {
        val targetPileNum = if (card.rank == Rank.ACE) {
            foundationPiles.entries.find { it.value.isEmpty() }?.key
        } else {
            foundationPiles.entries.find {
                val topCard = it.value.lastOrNull()
                topCard != null && topCard.suit == card.suit && topCard.rank.ordinal == card.rank.ordinal - 1
            }?.key
        }

        return targetPileNum?.let {
            val isEmpty = foundationPiles[it]?.isEmpty() ?: true
            MoveDestination(DestinationType.FOUNDATION, it, isEmpty)
        }
    }

    private fun findBestTableauMove(card: Card): MoveDestination? {
        val validDestinations = tableauPiles.filter { entry ->
            val targetPile = entry.value
            if (targetPile.isEmpty()) {
                true
            } else {
                val topCard = targetPile.last()
                val cardIsRed = card.suit == Suit.DIAMONDS || card.suit == Suit.HEARTS
                val destIsRed = topCard.suit == Suit.DIAMONDS || topCard.suit == Suit.HEARTS
                (cardIsRed != destIsRed) && (topCard.rank.ordinal == card.rank.ordinal + 1)
            }
        }

        val bestDestinationEntry = validDestinations.maxByOrNull {
            if (it.value.isEmpty()) -1 else it.value.minOf { c -> c.rank.ordinal }
        }

        return bestDestinationEntry?.let {
            MoveDestination(DestinationType.TABLEAU, it.key, it.value.isEmpty())
        }
    }



    private fun findFirstEmptyFreecell(): MoveDestination? {
            val targetCellNum = freeCellPiles.entries.find { it.value == null }?.key
            return targetCellNum?.let {
                MoveDestination(DestinationType.FREECELL, it, true)
            }


    }


}
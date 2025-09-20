package com.example.fairchildfreecell
// In GameState.kt, at the top of the file before the class definition

enum class GameSection {
    BOARD, FOUNDATION, FREECELL
}

data class MoveDestination(
    val type: GameSection,
    val index: Int,
    val isEmpty: Boolean
)


class GameState(val gameNumber: Int) {
    val boardPiles = mutableMapOf<Int, MutableList<Card>>()
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
            boardPiles[i] = mutableListOf()
        }
    }

    private fun dealCards(deck: MutableList<Card>) {
        // Correct round-robin dealing for Freecell
        deck.forEachIndexed { i, card ->
            val pileNum = (i % 8) + 1
            boardPiles[pileNum]?.add(card)
        }
    }

    fun findBestMove(card: Card, sourceSection: GameSection): MoveDestination? {
        // 1. Always prioritize moving to the foundation.
        findBestFoundationMove(card)?.let { return it }

        // 2. If no foundation move is found, find the best possible tableau and free cell moves.
        val bestBoardMove = findBestBoardMove(card)
        val firstFreecell = findFirstEmptyFreecell()

        // 3. Apply the priority rules to decide which move to return.
        return when {
            // If only one type of move is available, return it.
            bestBoardMove != null && firstFreecell == null -> bestBoardMove
            bestBoardMove == null && firstFreecell != null -> firstFreecell

            // If both are available, apply the special rules.
            bestBoardMove != null && firstFreecell != null -> {
                if ((card.rank == Rank.KING) || (bestBoardMove.isEmpty && sourceSection == GameSection.FREECELL) || !bestBoardMove.isEmpty) {
                    bestBoardMove
                } else firstFreecell
            }
            else -> null
        }
    }

    fun moveCard(card: Card, sourceSection: GameSection, sourceNum: Int, destination: MoveDestination) {

        when (sourceSection) {
            GameSection.BOARD -> boardPiles[sourceNum]?.remove(card)
            GameSection.FREECELL -> freeCellPiles[sourceNum] = null
            else -> {}
        }

        when (destination.type) {
            GameSection.BOARD  -> boardPiles[destination.index]?.add(card)
            GameSection.FOUNDATION -> foundationPiles[destination.index]?.add(card)
            GameSection.FREECELL -> freeCellPiles[destination.index] = card
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
            MoveDestination(GameSection.FOUNDATION, it, isEmpty)
        }
    }

    private fun findBestBoardMove(card: Card): MoveDestination? {
        val validDestinations = boardPiles.filter { entry ->
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
            MoveDestination(GameSection.BOARD, it.key, it.value.isEmpty())
        }
    }



    private fun findFirstEmptyFreecell(): MoveDestination? {
            val targetCellNum = freeCellPiles.entries.find { it.value == null }?.key
            return targetCellNum?.let {
                MoveDestination(GameSection.FREECELL, it, true)
            }


    }


}
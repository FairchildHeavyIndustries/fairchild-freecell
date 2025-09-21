package com.example.fairchildfreecell

class GameState(val gameNumber: Int) {
    val boardPiles = mutableMapOf<Int, MutableList<Card>>()
    val foundationPiles = mutableMapOf<Int, MutableList<Card>>()
    val freeCellPiles = mutableMapOf<Int, Card?>()

    private val moveHistory = mutableListOf<MoveEvent>()

    init {
        val deck = MSDealGenerator.getShuffledDeck(gameNumber)
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

    fun findBestMove(card: Card, sourceSection: GameSection): CardLocation? {
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

    fun moveCard(card: Card, sourceSection: GameSection, sourceNum: Int, destination: CardLocation) : MoveEvent {
        val moveEvent = doCardMove(card, sourceSection, sourceNum, destination)
        moveHistory.add(moveEvent)
        return moveEvent
    }

    private fun doCardMove(card: Card, sourceSection: GameSection, sourceNum: Int, destination: CardLocation) : MoveEvent {
        val sourceWasEmpty = when (sourceSection) {
            GameSection.BOARD -> boardPiles[sourceNum]?.isEmpty() ?: true
            GameSection.FREECELL -> freeCellPiles[sourceNum] == null
            GameSection.FOUNDATION -> foundationPiles[sourceNum]?.isEmpty() ?: true
        }
        val source = CardLocation(sourceSection, sourceNum, sourceWasEmpty)

        when (sourceSection) {
            GameSection.BOARD -> boardPiles[sourceNum]?.remove(card)
            GameSection.FREECELL -> freeCellPiles[sourceNum] = null
            GameSection.FOUNDATION -> foundationPiles[sourceNum]?.remove(card)
        }

        when (destination.section) {
            GameSection.BOARD -> boardPiles[destination.columnIndex]?.add(card)
            GameSection.FOUNDATION -> foundationPiles[destination.columnIndex]?.add(card)
            GameSection.FREECELL -> freeCellPiles[destination.columnIndex] = card
        }

        val moveEvent = MoveEvent(card, source, destination)

        return moveEvent
    }

    fun undoLastMove(): MoveEvent? {
        if (moveHistory.isEmpty()) {
            return null
        }

        val lastMove = moveHistory.removeAt(moveHistory.lastIndex)

        return doCardMove(
            card = lastMove.card,
            sourceSection = lastMove.destination.section,
            sourceNum = lastMove.destination.columnIndex,
            destination = lastMove.source
        )
    }
    private fun findBestFoundationMove(card: Card): CardLocation? {
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
            CardLocation(GameSection.FOUNDATION, it, isEmpty)
        }
    }

    private fun findBestBoardMove(card: Card): CardLocation? {
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
            CardLocation(GameSection.BOARD, it.key, it.value.isEmpty())
        }
    }



    private fun findFirstEmptyFreecell(): CardLocation? {
            val targetCellNum = freeCellPiles.entries.find { it.value == null }?.key
            return targetCellNum?.let {
                CardLocation(GameSection.FREECELL, it, true)
            }


    }


}
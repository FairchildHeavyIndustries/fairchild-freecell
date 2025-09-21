package com.example.fairchildfreecell

import kotlin.math.pow

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

//    fun findBestMove(card: Card, sourceSection: GameSection): CardLocation? {
//        // 1. Always prioritize moving to the foundation.
//        findBestFoundationMove(card)?.let { return it }
//
//        // 2. If no foundation move is found, find the best possible tableau and free cell moves.
//        val bestBoardMove = findBestBoardMove(card)
//        val firstFreecell = findFirstEmptyFreecell()
//
//        // 3. Apply the priority rules to decide which move to return.
//        return when {
//            // If only one type of move is available, return it.
//            bestBoardMove != null && firstFreecell == null -> bestBoardMove
//            bestBoardMove == null && firstFreecell != null -> firstFreecell
//
//            // If both are available, apply the special rules.
//            bestBoardMove != null && firstFreecell != null -> {
//                if ((card.rank == Rank.KING) || (bestBoardMove.isEmpty && sourceSection == GameSection.FREECELL) || !bestBoardMove.isEmpty) {
//                    bestBoardMove
//                } else firstFreecell
//            }
//            else -> null
//        }
//    }

    fun moveCard(clickedCard: Card, sourceSection: GameSection, sourceColumn: Int): MoveEvent? {
        val stackToMove = getStackToMove(clickedCard, sourceSection, sourceColumn)
        if (stackToMove.isEmpty() || !isStackValid(stackToMove)) {
            return null
        }

        val sourceLocation = CardLocation(sourceSection, sourceColumn)
        val destination = findBestDestinationFor(stackToMove, sourceLocation) ?: return null

        val moveEvent = MoveEvent(stackToMove, sourceLocation, destination)
        performMove(moveEvent)
        moveHistory.add(moveEvent)
        return moveEvent
    }

    fun undoLastMove(): MoveEvent? {
        if (moveHistory.isEmpty()) return null
        val lastMove = moveHistory.removeAt(moveHistory.lastIndex)
        val undoEvent = MoveEvent(lastMove.cards.reversed(), lastMove.destination, lastMove.source)
        performMove(undoEvent)
        return undoEvent
    }

    private fun performMove(moveEvent: MoveEvent) {
        // Remove cards from the source pile(s).
        when (moveEvent.source.section) {
            GameSection.BOARD -> boardPiles[moveEvent.source.columnIndex]?.removeAll(moveEvent.cards.toSet())
            GameSection.FREECELL -> freeCellPiles[moveEvent.source.columnIndex] = null
            GameSection.FOUNDATION -> foundationPiles[moveEvent.source.columnIndex]?.removeAll(moveEvent.cards.toSet())
        }

        // Add cards to the destination pile(s).
        when (moveEvent.destination.section) {
            GameSection.BOARD -> boardPiles[moveEvent.destination.columnIndex]?.addAll(moveEvent.cards)
            GameSection.FREECELL -> freeCellPiles[moveEvent.destination.columnIndex] = moveEvent.cards.first()
            GameSection.FOUNDATION -> foundationPiles[moveEvent.destination.columnIndex]?.addAll(moveEvent.cards)
        }
    }

    private fun getStackToMove(card: Card, section: GameSection, num: Int): List<Card> {
        return when (section) {
            GameSection.BOARD -> {
                val pile = boardPiles[num] ?: return emptyList()
                val cardIndex = pile.indexOf(card)
                if (cardIndex != -1) pile.subList(cardIndex, pile.size).toList() else emptyList()
            }
            GameSection.FREECELL -> listOfNotNull(freeCellPiles[num])
            else -> emptyList()
        }
    }

    private fun findBestDestinationFor(stackToMove: List<Card>, source: CardLocation): CardLocation? {
        // Priority 1: Foundation (only for single cards)
        if (stackToMove.size == 1) {
            findBestFoundationMove(stackToMove.first())?.let { return it }
        }

        // Priority 2 & 3: Board and Free Cells
        val bestBoardMove = findBestBoardMoveForStack(stackToMove, source.columnIndex)
        val bestFreeCellMove = if (stackToMove.size == 1) findFirstEmptyFreecell() else null

        return when {
            bestBoardMove != null && bestFreeCellMove == null -> bestBoardMove
            bestBoardMove == null && bestFreeCellMove != null -> bestFreeCellMove
            bestBoardMove != null && bestFreeCellMove != null -> {
                if (stackToMove.first().rank == Rank.KING || source.section == GameSection.FREECELL) {
                    bestBoardMove
                } else {
                    bestFreeCellMove
                }
            }
            else -> null
        }
    }
    private fun findBestBoardMoveForStack(stackToMove: List<Card>, sourcePileNum: Int): CardLocation? {
        val emptyFreeCells = freeCellPiles.values.count { it == null }
        val emptyBoardPiles = boardPiles.values.count { it.isEmpty() }
        val maxMoveSize = (1 + emptyFreeCells) * (2.0.pow(emptyBoardPiles)).toInt()

        if (stackToMove.size > maxMoveSize) return null

        val validDestination = boardPiles.entries.find { (pileNum, pile) ->
            if (pileNum == sourcePileNum) return@find false
            if (pile.isEmpty()) return@find true

            val topCard = pile.last()
            val bottomCardOfStack = stackToMove.first()
            val destIsRed = topCard.suit in listOf(Suit.DIAMONDS, Suit.HEARTS)
            val stackIsRed = bottomCardOfStack.suit in listOf(Suit.DIAMONDS, Suit.HEARTS)
            destIsRed != stackIsRed && topCard.rank.ordinal == bottomCardOfStack.rank.ordinal + 1
        }
        return validDestination?.let { CardLocation(GameSection.BOARD, it.key) }
    }

    private fun isStackValid(stack: List<Card>): Boolean {
        if (stack.size <= 1) return true
        for (i in 0 until stack.size - 1) {
            val bottomCard = stack[i]
            val topCard = stack[i + 1]
            val bottomIsRed = bottomCard.suit in listOf(Suit.DIAMONDS, Suit.HEARTS)
            val topIsRed = topCard.suit in listOf(Suit.DIAMONDS, Suit.HEARTS)
            if (bottomIsRed == topIsRed || bottomCard.rank.ordinal != topCard.rank.ordinal + 1) return false
        }
        return true
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
        return targetPileNum?.let { CardLocation(GameSection.FOUNDATION, it) }
    }


    private fun findFirstEmptyFreecell(): CardLocation? {
        return freeCellPiles.entries.find { it.value == null }?.key?.let {
            CardLocation(GameSection.FREECELL, it)
        }
    }
}
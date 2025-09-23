package com.example.fairchildfreecell

import kotlin.math.pow

class GameState {
    val gameNumber: Int
    val foundation = Foundation()
    private val suitToIndexMap = Suit.entries.associateWith { it.ordinal }

    val boardPiles = mutableMapOf<Int, MutableList<Card>>()
    val freeCellPiles = mutableMapOf<Int, Card?>()

    val moveHistory = mutableListOf<MoveEvent>()

    constructor(gameNumber: Int) {
        this.gameNumber = gameNumber
        val deck = MSDealGenerator.getShuffledDeck(gameNumber)
        initializePiles()
        dealCards(deck)
    }

    // Secondary constructor for loading a predefined state for testing
    constructor(testState: PresetGameState, gameNumber: Int = 0) {
        this.gameNumber = gameNumber
        // Create deep copies to prevent tests from modifying the base test state object
        boardPiles.putAll(testState.boardPiles.mapValues { it.value.toMutableList() })
        freeCellPiles.putAll(testState.freeCellPiles)
        testState.foundationPiles.values.flatten().forEach { foundation.add(it) }
    }

    fun moveCard(clickedCard: Card, sourceSection: GameSection, sourceColumn: Int): List<MoveEvent> {
        val allMoveEvents = mutableListOf<MoveEvent>()
        val stackToMove = getStackToMove(clickedCard, sourceSection, sourceColumn)
        if (stackToMove.isNotEmpty() && isStackValid(stackToMove)) {
            val sourceLocation = CardLocation(sourceSection, sourceColumn)
            val destination = findBestMove(stackToMove, sourceLocation)
            if (destination != null) {
                val moveEvent = MoveEvent(stackToMove, sourceLocation, destination)
                performMove(moveEvent)
                allMoveEvents.add(moveEvent)
            }
        }
        if (allMoveEvents.isNotEmpty()){
            allMoveEvents.addAll(autoMoveCardsToFoundation())
            moveHistory.addAll(allMoveEvents)
        }
        return allMoveEvents
    }

    fun undoLastMove(): MoveEvent? {
        if (moveHistory.isEmpty()) return null
        val lastMove = moveHistory.removeAt(moveHistory.lastIndex)
        val undoEvent = MoveEvent(lastMove.cards, lastMove.destination, lastMove.source)
        performMove(undoEvent)
        return undoEvent
    }

    private fun autoMoveCardsToFoundation(): List<MoveEvent> {
        val autoMoves = mutableListOf<MoveEvent>()
        while (true) {
            val eligibleCards = foundation.getEligibleAutoMoveCards()
            if (eligibleCards.isEmpty()) break

            val eligibleTopCards = findEligibleTopCards(eligibleCards)
            if (eligibleTopCards.isEmpty()) break


            for ((card, source) in eligibleTopCards) {
                    val destination = CardLocation(GameSection.FOUNDATION, suitToIndexMap[card.suit]!!)
                    val moveEvent = MoveEvent(listOf(card), source, destination)
                    performMove(moveEvent)
                    moveHistory.add(moveEvent)
                    autoMoves.add(moveEvent)

            }

        }
        return autoMoves
    }

    private fun findEligibleTopCards(candidates: List<Card>): Map<Card, CardLocation> {
        val eligibleTopCards = mutableMapOf<Card, CardLocation>()
        val candidateSet = candidates.toSet() // Use a Set for efficient lookups.

        freeCellPiles.forEach { (index, card) ->
            if (card != null && card in candidateSet) {
                eligibleTopCards[card] = CardLocation(GameSection.FREECELL, index)
            }
        }
        boardPiles.forEach { (index, pile) ->
            pile.lastOrNull()?.let { card ->
                if (card in candidateSet) {
                    eligibleTopCards[card] = CardLocation(GameSection.BOARD, index)
                }
            }
        }
        return eligibleTopCards
    }

    private fun findBestMove(stackToMove: List<Card>, source: CardLocation): CardLocation? {
        // Priority 1: Foundation (only for single cards)
        if (stackToMove.size == 1) {
            findBestFoundationMove(stackToMove.first())?.let { return it }
        }

        // Priority 2 & 3: Board and Free Cells
        val bestBoardMove = findBestBoardMoveForStack(stackToMove, source)
        val bestFreeCellMove = if (stackToMove.size == 1) findFirstEmptyFreecell() else null

        return when {
            bestBoardMove != null && bestFreeCellMove == null -> bestBoardMove
            bestBoardMove == null && bestFreeCellMove != null -> bestFreeCellMove
            bestBoardMove != null && bestFreeCellMove != null -> {
                val isBoardMoveToEmptyPile =
                    boardPiles[bestBoardMove.columnIndex]?.isEmpty() ?: false
                if ((stackToMove.first().value == Value.KING) || (isBoardMoveToEmptyPile && source.section == GameSection.FREECELL) || !isBoardMoveToEmptyPile) {
                    //if ((card.value == Value.KING) || (bestBoardMove.isEmpty && sourceSection == GameSection.FREECELL) || !bestBoardMove.isEmpty) {
                    //if (!isBoardMoveToEmptyPile || cardToMove.value == Value.KING || source.section == GameSection.FREECELL)
                    bestBoardMove
                } else {
                    bestFreeCellMove
                }
            }

            else -> null
        }
    }

    private fun findBestBoardMoveForStack(
        stackToMove: List<Card>,
        source: CardLocation
    ): CardLocation? {
        val emptyFreeCells = freeCellPiles.values.count { it == null }
        val emptyBoardPiles = boardPiles.values.count { it.isEmpty() }

        val validDestinations = boardPiles.entries.filter { (pileNum, pile) ->
            // A move is invalid if it's from a board to the same pile
            if (source.section == GameSection.BOARD && pileNum == source.columnIndex) return@filter false

            // Determine the maximum number of cards that can be moved.
            val maxMoveSize = (1 + emptyFreeCells) * (2.0.pow(emptyBoardPiles)).toInt()

            // The move is invalid if the stack is too large.
            if (stackToMove.size > maxMoveSize) return@filter false

            // Check for valid placement (empty pile, or alternating color and sequential value).
            if (pile.isEmpty()) {
                true
            } else {
                canStackOn(cardBelow = pile.last(), cardAbove = stackToMove.first())
            }
        }

        // 2. From the list of valid moves, select the BEST one
        val bestDestinationEntry = validDestinations.maxByOrNull {
            // A lower score is better. Prioritize moves that uncover low-value cards.
            // Empty piles get a score of -1.
            if (it.value.isEmpty()) -1 else it.value.minOf { c -> c.value.ordinal }
        }

        // 3. Return the best destination as a CardLocation.
        return bestDestinationEntry?.let {
            CardLocation(GameSection.BOARD, it.key)
        }

    }

    private fun performMove(moveEvent: MoveEvent) {
        // Remove cards from the source pile(s).
        when (moveEvent.source.section) {
            GameSection.BOARD -> boardPiles[moveEvent.source.columnIndex]?.removeAll(moveEvent.cards.toSet())
            GameSection.FREECELL -> freeCellPiles[moveEvent.source.columnIndex] = null
            GameSection.FOUNDATION -> moveEvent.cards.forEach { foundation.remove(it) }
        }

        // Add cards to the destination pile(s).
        when (moveEvent.destination.section) {
            GameSection.BOARD -> boardPiles[moveEvent.destination.columnIndex]?.addAll(moveEvent.cards)
            GameSection.FREECELL -> freeCellPiles[moveEvent.destination.columnIndex] =
                moveEvent.cards.first()

            GameSection.FOUNDATION -> moveEvent.cards.forEach { foundation.add(it) }
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


    private fun isStackValid(stack: List<Card>): Boolean {
        if (stack.size <= 1) return true
        for (i in 0 until stack.size - 1) {
            if (!canStackOn(cardBelow = stack[i], cardAbove = stack[i + 1])) return false
        }
        return true
    }

    private fun canStackOn(cardBelow: Card, cardAbove: Card): Boolean {
        return cardBelow.color != cardAbove.color && cardBelow.value.ordinal == cardAbove.value.ordinal + 1
    }

    private fun findBestFoundationMove(card: Card): CardLocation? {
        val topCard = foundation.getTopCardFor(card.suit)
        val isValidMove =
            if (topCard == null) card.value == Value.ACE else card.value.ordinal == topCard.value.ordinal + 1

        return if (isValidMove) {
            CardLocation(GameSection.FOUNDATION, suitToIndexMap[card.suit]!!)
        } else {
            null
        }
    }


    private fun findFirstEmptyFreecell(): CardLocation? {
        return freeCellPiles.entries.find { it.value == null }?.key?.let {
            CardLocation(GameSection.FREECELL, it)
        }
    }


    private fun initializePiles() {
        for (i in 0..3) {
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
}
package com.example.fairchildfreecell


class Foundation {
    private val pilesBySuit = Suit.entries.associateWith { FoundationPile(it) }

    fun add(card: Card) {
        pilesBySuit[card.suit]?.cards?.add(card)
    }

    fun remove(card: Card) {
        pilesBySuit[card.suit]?.cards?.remove(card)
    }

    fun getTopCardFor(suit: Suit): Card? = pilesBySuit[suit]?.topCard

    private fun getMinValueOfColor(color: CardColor): Value {
        return Suit.entries
            .filter { it.color == color }
            .map { pilesBySuit[it]?.topCard?.value }
            .minByOrNull { it?.ordinal ?: -1 } ?: Value.ACE
    }

    fun getEligibleAutoMoveCards(): List<Card> {
        val eligibleCards = mutableListOf<Card>()
        val minRedValue = getMinValueOfColor(CardColor.RED)
        val minBlackValue = getMinValueOfColor(CardColor.BLACK)

        pilesBySuit.values.forEach { pile ->
            if (pile.topCard?.value == Value.KING) return@forEach
            val nextCard = Card(pile.nextValue, pile.suit)
            // A card is eligible if its value is not more than 1 above the lowest-valued card of the opposite color.
            val requirementMet = when (nextCard.color) {
                CardColor.RED -> nextCard.value.ordinal <= minBlackValue.ordinal + 1
                CardColor.BLACK -> nextCard.value.ordinal <= minRedValue.ordinal + 1
            }
            if (requirementMet) {
                eligibleCards.add(nextCard)
            }
        }
        return eligibleCards
    }
    fun isFoundationComplete(): Boolean {
        return pilesBySuit.values.sumOf { it.cards.size } == 52
    }
}
class FoundationPile(val suit: Suit) {
    val cards = mutableListOf<Card>()
    val topCard: Card? get() = cards.lastOrNull()
    val nextValue: Value get() = topCard?.let { Value.entries[it.value.ordinal + 1] } ?: Value.ACE
}

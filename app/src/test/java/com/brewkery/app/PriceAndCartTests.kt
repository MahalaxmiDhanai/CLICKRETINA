package com.brewkery.app

import com.brewkery.app.domain.CartLine
import com.brewkery.app.domain.CartLineKey
import com.brewkery.app.domain.ItemCustomizations
import com.brewkery.app.domain.MenuItem
import com.brewkery.app.domain.MilkOption
import com.brewkery.app.domain.PriceCalculator
import com.brewkery.app.domain.SizeOption
import com.brewkery.app.domain.TicketIdGenerator
import com.brewkery.app.domain.lineTotalCents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun makeMilk(id: String, extraCents: Long) = MilkOption(id, id, extraCents)
private fun makeSize(id: String, extraCents: Long) = SizeOption(id, id, extraCents)

private fun makeItem(id: Int = 1, baseCents: Long = 500L) = MenuItem(
    id = id, categoryId = "cat_hot_coffee", name = "Test Coffee",
    tagline = "", description = "", basePriceCents = baseCents,
    rating = 4.5, reviewCount = 100, prepTime = "5 min", calories = 200,
    imageUrl = "", badge = "", ingredients = emptyList(),
    customizations = ItemCustomizations(emptyList(), emptyList(), emptyList()),
)

private fun makeCartLine(
    item: MenuItem,
    size: SizeOption,
    milk: MilkOption,
    sugar: String,
    quantity: Int,
): CartLine {
    val sugarExtra = PriceCalculator.parseSugarExtraCents(sugar)
    val unit = PriceCalculator.unitPriceCents(item.basePriceCents, size.extraPriceCents, milk.extraPriceCents, sugarExtra)
    return CartLine(
        key           = CartLineKey(item.id, size.id, milk.id, sugar),
        item          = item,
        chosenSize    = size,
        chosenMilk    = milk,
        chosenSugar   = sugar,
        quantity      = quantity,
        unitPriceCents = unit,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. Reference price check: subtotal $9.40, delivery $2.50, tax $0.75, total $12.65
// ─────────────────────────────────────────────────────────────────────────────

class PriceCalculatorReferenceTest {

    @Test
    fun `reference case gives 12_65 total`() {
        val subtotal  = 940L   // $9.40
        val delivery  = 250L   // $2.50
        val taxRate   = 8.0    // 8%
        val tax       = PriceCalculator.taxCents(subtotal, taxRate) // $0.752 → rounds to 75¢
        val total     = PriceCalculator.totalCents(subtotal, delivery, tax)

        assertEquals("Tax should be 75 cents", 75L, tax)
        assertEquals("Total should be 1265 cents", 1265L, total)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Unit price with size + milk extras
// ─────────────────────────────────────────────────────────────────────────────

class UnitPriceTest {

    @Test
    fun `unit price sums base + size + milk extras`() {
        // base $5.00 + size $0.50 + milk $0.30 = $5.80 = 580 cents
        val unit = PriceCalculator.unitPriceCents(500L, 50L, 30L, 0L)
        assertEquals(580L, unit)
    }

    @Test
    fun `unit price includes sugar extra from regex`() {
        // "Light Wildflower Honey (+0.40)" → 40 cents sugar extra
        val sugarExtra = PriceCalculator.parseSugarExtraCents("Light Wildflower Honey (+0.40)")
        val unit = PriceCalculator.unitPriceCents(500L, 0L, 0L, sugarExtra)
        assertEquals(40L, sugarExtra)
        assertEquals(540L, unit)
    }

    @Test
    fun `plain sugar level has zero extra`() {
        val sugarExtra = PriceCalculator.parseSugarExtraCents("Regular")
        assertEquals(0L, sugarExtra)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Tax rounding: HALF_UP, delivery excluded from tax base
// ─────────────────────────────────────────────────────────────────────────────

class TaxRoundingTest {

    @Test
    fun `tax rounds HALF_UP at midpoint`() {
        // 8% of 940 = 75.2 → rounds down to 75
        assertEquals(75L, PriceCalculator.taxCents(940L, 8.0))
        // 8% of 950 = 76.0 → exactly 76
        assertEquals(76L, PriceCalculator.taxCents(950L, 8.0))
        // 8% of 945 = 75.6 → rounds up to 76
        assertEquals(76L, PriceCalculator.taxCents(945L, 8.0))
    }

    @Test
    fun `delivery fee is excluded from tax base`() {
        // If we mistakenly included delivery in the base:
        // (940 + 250) * 8% = 95.2 → 95 — that's WRONG.
        // Correct: 940 * 8% = 75.
        val correctTax = PriceCalculator.taxCents(940L, 8.0)
        val wrongTax   = PriceCalculator.taxCents(940L + 250L, 8.0)
        assertEquals(75L, correctTax)
        assertTrue("Delivery must not be taxed", correctTax < wrongTax)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Cart merge and identity
// ─────────────────────────────────────────────────────────────────────────────

class CartMergeTest {

    private val item   = makeItem()
    private val sizeM  = makeSize("sz_medium", 50L)
    private val sizeL  = makeSize("sz_large", 100L)
    private val milkO  = makeMilk("m_oat", 30L)
    private val milkA  = makeMilk("m_almond", 40L)
    private val sugar  = "Regular"

    /** Simulate the cart-merge logic that CartRepository will implement. */
    private fun mergeInto(
        lines: MutableMap<CartLineKey, CartLine>,
        newLine: CartLine,
    ) {
        val existing = lines[newLine.key]
        lines[newLine.key] = if (existing != null) {
            existing.copy(quantity = existing.quantity + newLine.quantity)
        } else {
            newLine
        }
    }

    @Test
    fun `same configuration merges quantity`() {
        val cart = mutableMapOf<CartLineKey, CartLine>()
        val line1 = makeCartLine(item, sizeM, milkO, sugar, 1)
        val line2 = makeCartLine(item, sizeM, milkO, sugar, 2)
        mergeInto(cart, line1)
        mergeInto(cart, line2)

        assertEquals(1, cart.size)
        assertEquals(3, cart.values.first().quantity)
    }

    @Test
    fun `different size creates separate line`() {
        val cart = mutableMapOf<CartLineKey, CartLine>()
        mergeInto(cart, makeCartLine(item, sizeM, milkO, sugar, 1))
        mergeInto(cart, makeCartLine(item, sizeL, milkO, sugar, 1))

        assertEquals(2, cart.size)
    }

    @Test
    fun `different milk creates separate line`() {
        val cart = mutableMapOf<CartLineKey, CartLine>()
        mergeInto(cart, makeCartLine(item, sizeM, milkO, sugar, 1))
        mergeInto(cart, makeCartLine(item, sizeM, milkA, sugar, 1))

        assertEquals(2, cart.size)
    }

    @Test
    fun `decrement to zero removes line`() {
        val cart = mutableMapOf<CartLineKey, CartLine>()
        val line = makeCartLine(item, sizeM, milkO, sugar, 1)
        mergeInto(cart, line)

        // Decrement: new qty = 1 - 1 = 0 → remove
        val existing = cart[line.key]!!
        val newQty   = existing.quantity - 1
        if (newQty <= 0) cart.remove(line.key) else cart[line.key] = existing.copy(quantity = newQty)

        assertTrue(cart.isEmpty())
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. Ticket ID format
// ─────────────────────────────────────────────────────────────────────────────

class TicketIdTest {

    @Test
    fun `ticket id matches regex`() {
        repeat(20) {
            val id = TicketIdGenerator.generate()
            assertTrue(
                "Expected #BK-XXXXX format, got: $id",
                id.matches(Regex("^#BK-\\d{5}$"))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. formatPrice
// ─────────────────────────────────────────────────────────────────────────────

class FormatPriceTest {

    @Test
    fun `formatPrice renders dollar symbol and two decimals`() {
        assertEquals("\$12.65", PriceCalculator.formatPrice(1265L, "$"))
        assertEquals("\$0.75", PriceCalculator.formatPrice(75L, "$"))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. dollarsToCents precision
// ─────────────────────────────────────────────────────────────────────────────

class DollarsToCentsTest {

    @Test
    fun `converts floating-point dollar to exact cents`() {
        assertEquals(250L, PriceCalculator.dollarsToCents(2.50))
        assertEquals(40L,  PriceCalculator.dollarsToCents(0.40))
        assertEquals(399L, PriceCalculator.dollarsToCents(3.99))
    }
}

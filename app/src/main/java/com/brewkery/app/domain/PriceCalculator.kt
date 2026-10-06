package com.brewkery.app.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure Kotlin object — zero Android dependencies.
 * All inputs are in cents (Long); outputs are in cents.
 *
 * Pricing rules:
 *  unitPrice  = base + size.extra + milk.extra + sugarExtra
 *  lineTotal  = unitPrice × quantity
 *  subtotal   = Σ lineTotals
 *  tax        = subtotal × taxRate / 100, HALF_UP to nearest cent (delivery excluded from tax base)
 *  total      = subtotal + deliveryFee + tax
 *
 * Reference: subtotal 940¢, delivery 250¢, tax 75¢ (8 % of 940 = 75.2 → rounds to 75¢), total 1265¢.
 */
object PriceCalculator {

    /** Parse a trailing "(+X.XX)" sugar-level quirk, e.g. "Light Wildflower Honey (+0.40)".
     *  The API returns sugar levels as plain strings, but one item embeds a hidden price.
     *  We extract it here so it factors into the unit price. Returns 0 if no pattern found.
     */
    private val SUGAR_PRICE_REGEX = Regex("""\(\+(\d+\.\d+)\)""")

    fun parseSugarExtraCents(sugarLevel: String): Long {
        val match = SUGAR_PRICE_REGEX.find(sugarLevel) ?: return 0L
        val dollars = match.groupValues[1].toDoubleOrNull() ?: return 0L
        return dollarsToCents(dollars)
    }

    /** Converts a Double dollar amount to Long cents using BigDecimal to avoid floating-point loss. */
    fun dollarsToCents(dollars: Double): Long =
        BigDecimal.valueOf(dollars)
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()

    fun unitPriceCents(
        basePriceCents:      Long,
        sizeExtraCents:      Long,
        milkExtraCents:      Long,
        sugarExtraCents:     Long,
    ): Long = basePriceCents + sizeExtraCents + milkExtraCents + sugarExtraCents

    fun lineTotalCents(unitPriceCents: Long, quantity: Int): Long =
        unitPriceCents * quantity

    fun subtotalCents(lines: List<CartLine>): Long =
        lines.sumOf { it.lineTotalCents }

    /**
     * Tax is calculated on subtotal ONLY (delivery is excluded).
     * Rounded HALF_UP to the nearest cent.
     */
    fun taxCents(subtotalCents: Long, taxRatePercent: Double): Long =
        BigDecimal.valueOf(subtotalCents)
            .multiply(BigDecimal.valueOf(taxRatePercent))
            .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
            .toLong()

    fun totalCents(
        subtotalCents:   Long,
        deliveryFeeCents: Long,
        taxCents:        Long,
    ): Long = subtotalCents + deliveryFeeCents + taxCents

    /** Formats a cent value as a display string, e.g. 1265 + "$" → "$12.65". */
    fun formatPrice(cents: Long, currencySymbol: String): String {
        val dollars = BigDecimal.valueOf(cents).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        return "$currencySymbol$dollars"
    }
}

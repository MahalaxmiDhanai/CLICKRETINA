@file:OptIn(ExperimentalMaterial3Api::class)

package com.brewkery.app.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewkery.app.R
import com.brewkery.app.domain.CartLine
import com.brewkery.app.domain.PriceCalculator
import com.brewkery.app.ui.menu.MenuUiState
import com.brewkery.app.ui.menu.MenuViewModel
import com.brewkery.app.ui.theme.Border
import com.brewkery.app.ui.theme.DarkText
import com.brewkery.app.ui.theme.Espresso
import com.brewkery.app.ui.theme.MutedBrown
import com.brewkery.app.ui.theme.Terracotta

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    menuViewModel: MenuViewModel,
    onBack:        () -> Unit,
    onOrderPlaced: () -> Unit,
) {
    val cartLines    by cartViewModel.cartLines.collectAsState()
    val subtotalCents by cartViewModel.subtotalCents.collectAsState()
    val menuState    by menuViewModel.uiState.collectAsState()
    val meta         = (menuState as? MenuUiState.Success)?.meta

    val deliveryFeeCents = meta?.deliveryFeeCents ?: 250L
    val taxCents         = meta?.let { PriceCalculator.taxCents(subtotalCents, it.taxRatePercent) } ?: 0L
    val totalCents       = subtotalCents + deliveryFeeCents + taxCents

    Scaffold(
        containerColor = Color(0xFFFDFAF7),
        topBar = {
            // ── Cart header — back | "YOUR CART" center | Clear Cart right ────
            Surface(
                modifier        = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color           = Color.White,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button),
                            tint               = Espresso,
                        )
                    }
                    Text(
                        text          = "YOUR CART",
                        modifier      = Modifier.weight(1f),
                        style         = MaterialTheme.typography.titleMedium,
                        fontWeight    = FontWeight.ExtraBold,
                        color         = Espresso,
                        textAlign     = TextAlign.Center,
                        letterSpacing = 1.sp,
                    )
                    TextButton(onClick = { cartViewModel.clearCart() }) {
                        Text(
                            text       = "Clear Cart",
                            color      = Terracotta,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        },
        bottomBar = {
            // "Place Order Now" only shown when cart has items
            if (cartLines.isNotEmpty() && meta != null) {
                PlaceOrderBar(
                    totalCents     = totalCents,
                    currencySymbol = meta.currencySymbol,
                    onPlaceOrder   = {
                        cartViewModel.placeOrder(meta)
                        onOrderPlaced()
                    },
                )
            }
        },
    ) { innerPadding ->
        if (cartLines.isEmpty()) {
            EmptyCartState(
                modifier = Modifier.padding(innerPadding),
                onBack   = onBack,
            )
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
            ) {
                items(
                    items = cartLines,
                    key   = { line ->
                        "${line.key.itemId}|${line.key.sizeId}|${line.key.milkOptionId}|${line.key.sugarLevel}"
                    },
                ) { line ->
                    CartLineCard(
                        line           = line,
                        currencySymbol = meta?.currencySymbol ?: "$",
                        onIncrease     = { cartViewModel.updateQuantity(line.key, line.quantity + 1) },
                        onDecrease     = { cartViewModel.updateQuantity(line.key, line.quantity - 1) },
                    )
                }
                item(key = "summary") {
                    Spacer(Modifier.height(4.dp))
                    OrderSummaryCard(
                        subtotalCents    = subtotalCents,
                        deliveryFeeCents = deliveryFeeCents,
                        taxCents         = taxCents,
                        totalCents       = totalCents,
                        taxRatePercent   = meta?.taxRatePercent ?: 8.0,
                        currencySymbol   = meta?.currencySymbol ?: "$",
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ── Cart line card — NO thumbnail, matching prototype exactly ─────────────────

@Composable
private fun CartLineCard(
    line:           CartLine,
    currencySymbol: String,
    onIncrease:     () -> Unit,
    onDecrease:     () -> Unit,
) {
    // Options summary: Tall (8 oz) • Oat Milk (Barista Blend)
    val sizePart  = line.chosenSize.label.takeIf { it.isNotBlank() } ?: ""
    val milkPart  = line.chosenMilk.name.takeIf  { it.isNotBlank() } ?: ""
    val sugarPart = line.chosenSugar.takeIf       { it.isNotBlank() } ?: ""
    val summary   = listOf(sizePart, milkPart, sugarPart).filter { it.isNotBlank() }.joinToString(" • ")

    val lineTotalCents = line.unitPriceCents * line.quantity

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        border    = BorderStroke(1.dp, Border),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: name, summary, price
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = line.item.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = DarkText,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                if (summary.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = summary,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MutedBrown,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = PriceCalculator.formatPrice(lineTotalCents, currencySymbol),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Terracotta,
                )
            }

            Spacer(Modifier.width(12.dp))

            // Right: +/- stepper in cream rounded container
            Surface(
                shape  = RoundedCornerShape(10.dp),
                color  = Color(0xFFF7EBE1),
                border = BorderStroke(1.dp, Border),
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text       = "−",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        color      = DarkText,
                        modifier   = Modifier.clickable { onDecrease() },
                    )
                    Text(
                        text       = line.quantity.toString(),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = DarkText,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier.widthIn(min = 20.dp),
                    )
                    Text(
                        text       = "+",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        color      = DarkText,
                        modifier   = Modifier.clickable { onIncrease() },
                    )
                }
            }
        }
    }
}

// ── Order summary card ────────────────────────────────────────────────────────

@Composable
private fun OrderSummaryCard(
    subtotalCents:    Long,
    deliveryFeeCents: Long,
    taxCents:         Long,
    totalCents:       Long,
    taxRatePercent:   Double,
    currencySymbol:   String,
) {
    val taxRateDisplay = taxRatePercent.toBigDecimal().stripTrailingZeros().toPlainString()

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        border    = BorderStroke(1.dp, Border),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SummaryRow("Subtotal", PriceCalculator.formatPrice(subtotalCents, currencySymbol))
            SummaryRow("Delivery Fee", PriceCalculator.formatPrice(deliveryFeeCents, currencySymbol))
            SummaryRow("Est. Tax ($taxRateDisplay%)", PriceCalculator.formatPrice(taxCents, currencySymbol))
            // Dashed divider before total
            HorizontalDivider(
                modifier  = Modifier.padding(vertical = 10.dp),
                color     = Border,
                thickness = 1.dp,
            )
            SummaryRow(
                label  = "Total Payable",
                amount = PriceCalculator.formatPrice(totalCents, currencySymbol),
                isBold = true,
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: String, isBold: Boolean = false) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = label,
            fontSize   = if (isBold) 13.sp else 11.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Normal,
            color      = if (isBold) Espresso else MutedBrown,
        )
        Text(
            text       = amount,
            fontSize   = if (isBold) 14.sp else 11.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color      = if (isBold) Terracotta else DarkText,
        )
    }
}

// ── Place Order bar ───────────────────────────────────────────────────────────

@Composable
private fun PlaceOrderBar(
    totalCents:     Long,
    currencySymbol: String,
    onPlaceOrder:   () -> Unit,
) {
    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shadowElevation = 8.dp,
        color           = Color.White,
    ) {
        Button(
            onClick  = onPlaceOrder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp),
            shape          = RoundedCornerShape(12.dp),
            colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            // Gradient matches prototype: gradient-terracotta class
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFD9532F),
                                Color(0xFFB84121),
                                Color(0xFF8C2B12),
                            ),
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = "🔒 ${stringResource(R.string.label_place_order)} • ${PriceCalculator.formatPrice(totalCents, currencySymbol)}",
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White,
                    fontSize   = 12.sp,
                )
            }
        }
    }
}

// ── Empty cart state ──────────────────────────────────────────────────────────

@Composable
private fun EmptyCartState(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("🛒", style = MaterialTheme.typography.displaySmall)
            Text(
                text      = stringResource(R.string.empty_cart_message),
                style     = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color     = MutedBrown,
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = onBack,
                shape   = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text  = stringResource(R.string.label_back_to_menu),
                    color = Terracotta,
                )
            }
        }
    }
}

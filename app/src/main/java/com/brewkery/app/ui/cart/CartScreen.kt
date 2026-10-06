@file:OptIn(ExperimentalMaterial3Api::class)

package com.brewkery.app.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brewkery.app.R
import com.brewkery.app.domain.CartLine
import com.brewkery.app.domain.PriceCalculator
import com.brewkery.app.domain.StoreMeta
import com.brewkery.app.ui.menu.MenuUiState
import com.brewkery.app.ui.menu.MenuViewModel
import com.brewkery.app.ui.theme.Border
import com.brewkery.app.ui.theme.DarkText
import com.brewkery.app.ui.theme.Espresso
import com.brewkery.app.ui.theme.MutedBrown
import com.brewkery.app.ui.theme.Terracotta

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun CartScreen(
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    onBack:        () -> Unit,
    onOrderPlaced: () -> Unit,
) {
    val menuState    by menuViewModel.uiState.collectAsState()
    val meta          = (menuState as? MenuUiState.Success)?.meta
    val cartLines    by cartViewModel.cartLines.collectAsState()
    val subtotalCents by cartViewModel.subtotalCents.collectAsState()

    if (meta == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Terracotta)
        }
    } else {
        CartContent(
            meta          = meta,
            cartLines     = cartLines,
            subtotalCents = subtotalCents,
            cartViewModel = cartViewModel,
            onBack        = onBack,
            onOrderPlaced = onOrderPlaced,
        )
    }
}

// ── Full cart content ─────────────────────────────────────────────────────────

@Composable
private fun CartContent(
    meta:          StoreMeta,
    cartLines:     List<CartLine>,
    subtotalCents: Long,
    cartViewModel: CartViewModel,
    onBack:        () -> Unit,
    onOrderPlaced: () -> Unit,
) {
    val taxCents   = PriceCalculator.taxCents(subtotalCents, meta.taxRatePercent)
    val totalCents = PriceCalculator.totalCents(subtotalCents, meta.deliveryFeeCents, taxCents)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title          = {
                    Text(
                        text       = "My Cart",
                        fontWeight = FontWeight.Bold,
                        color      = Espresso,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button),
                            tint               = Espresso,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },
        bottomBar = {
            if (cartLines.isNotEmpty()) {
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
                        currencySymbol = meta.currencySymbol,
                        onIncrease     = { cartViewModel.updateQuantity(line.key, line.quantity + 1) },
                        onDecrease     = { cartViewModel.updateQuantity(line.key, line.quantity - 1) },
                    )
                }
                item(key = "summary") {
                    OrderSummaryCard(
                        subtotalCents    = subtotalCents,
                        deliveryFeeCents = meta.deliveryFeeCents,
                        taxCents         = taxCents,
                        totalCents       = totalCents,
                        taxRatePercent   = meta.taxRatePercent,
                        currencySymbol   = meta.currencySymbol,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ── Cart line card ────────────────────────────────────────────────────────────

@Composable
private fun CartLineCard(
    line:           CartLine,
    currencySymbol: String,
    onIncrease:     () -> Unit,
    onDecrease:     () -> Unit,
) {
    val summary = buildList<String> {
        if (line.chosenSize.label.isNotBlank()) add(line.chosenSize.label)
        if (line.chosenMilk.name.isNotBlank())  add(line.chosenMilk.name)
        if (line.chosenSugar.isNotBlank())       add(line.chosenSugar)
    }.joinToString(" · ")

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
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Thumbnail
            AsyncImage(
                model              = line.item.imageUrl,
                contentDescription = stringResource(R.string.cd_item_image),
                modifier           = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale       = ContentScale.Crop,
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = line.item.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = DarkText,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                if (summary.isNotBlank()) {
                    Text(
                        text     = summary,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MutedBrown,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text  = "${PriceCalculator.formatPrice(line.unitPriceCents, currencySymbol)} × ${line.quantity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedBrown,
                        )
                        Text(
                            text       = PriceCalculator.formatPrice(lineTotalCents, currencySymbol),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Terracotta,
                        )
                    }
                    // +/- stepper
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalIconButton(
                            onClick  = onDecrease,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Text("−", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text       = line.quantity.toString(),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center,
                            color      = DarkText,
                            modifier   = Modifier.widthIn(min = 24.dp),
                        )
                        FilledTonalIconButton(
                            onClick  = onIncrease,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
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
            Text(
                text       = "Order Summary",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = Espresso,
            )
            Spacer(Modifier.height(12.dp))
            SummaryRow("Subtotal",                    PriceCalculator.formatPrice(subtotalCents,    currencySymbol))
            SummaryRow("Delivery Fee",                PriceCalculator.formatPrice(deliveryFeeCents, currencySymbol))
            SummaryRow("Est. Tax ($taxRateDisplay%)", PriceCalculator.formatPrice(taxCents,         currencySymbol))
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Border)
            SummaryRow(
                label   = "Total Payable",
                amount  = PriceCalculator.formatPrice(totalCents, currencySymbol),
                isBold  = true,
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
            style      = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color      = if (isBold) Espresso else MutedBrown,
        )
        Text(
            text       = amount,
            style      = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
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
        modifier        = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color           = Color.White,
    ) {
        Button(
            onClick  = onPlaceOrder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Terracotta),
        ) {
            Text(
                text       = "${stringResource(R.string.label_place_order)} • ${PriceCalculator.formatPrice(totalCents, currencySymbol)}",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
            )
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

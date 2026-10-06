@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.brewkery.app.ui.detail

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brewkery.app.R
import com.brewkery.app.domain.CartLine
import com.brewkery.app.domain.CartLineKey
import com.brewkery.app.domain.MenuItem
import com.brewkery.app.domain.MilkOption
import com.brewkery.app.domain.PriceCalculator
import com.brewkery.app.domain.SizeOption
import com.brewkery.app.domain.StoreMeta
import com.brewkery.app.ui.cart.CartViewModel
import com.brewkery.app.ui.menu.MenuUiState
import com.brewkery.app.ui.menu.MenuViewModel
import com.brewkery.app.ui.theme.Amber
import com.brewkery.app.ui.theme.Border
import com.brewkery.app.ui.theme.DarkText
import com.brewkery.app.ui.theme.Espresso
import com.brewkery.app.ui.theme.MutedBrown
import com.brewkery.app.ui.theme.Terracotta

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun ItemDetailScreen(
    itemId:        Int,
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    onBack:        () -> Unit,
) {
    val uiState      by menuViewModel.uiState.collectAsState()
    val successState  = uiState as? MenuUiState.Success
    val item          = successState?.items?.find { it.id == itemId }

    if (successState != null && item != null) {
        ItemDetailContent(
            item          = item,
            meta          = successState.meta,
            cartViewModel = cartViewModel,
            onBack        = onBack,
        )
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Terracotta)
        }
    }
}

// ── Full detail content ───────────────────────────────────────────────────────

@Composable
private fun ItemDetailContent(
    item:          MenuItem,
    meta:          StoreMeta,
    cartViewModel: CartViewModel,
    onBack:        () -> Unit,
) {
    val context        = LocalContext.current
    val currencySymbol = meta.currencySymbol

    // Default selections — first option in each list, keyed by item.id
    var chosenSize by remember(item.id) {
        mutableStateOf(item.customizations.sizes.firstOrNull() ?: SizeOption("", "", 0L))
    }
    var chosenMilk by remember(item.id) {
        mutableStateOf(item.customizations.milkOptions.firstOrNull() ?: MilkOption("", "", 0L))
    }
    var chosenSugar by remember(item.id) {
        mutableStateOf(item.customizations.sugarLevels.firstOrNull() ?: "")
    }
    var quantity by remember(item.id) { mutableIntStateOf(1) }

    // Live price recomputation
    val sugarExtraCents = PriceCalculator.parseSugarExtraCents(chosenSugar)
    val unitPriceCents  = PriceCalculator.unitPriceCents(
        basePriceCents  = item.basePriceCents,
        sizeExtraCents  = chosenSize.extraPriceCents,
        milkExtraCents  = chosenMilk.extraPriceCents,
        sugarExtraCents = sugarExtraCents,
    )
    val lineTotalCents = PriceCalculator.lineTotalCents(unitPriceCents, quantity)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // "Add to Cart • $lineTotal" bar — terracotta button
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color           = Color.White,
            ) {
                Button(
                    onClick = {
                        val key = CartLineKey(
                            itemId       = item.id,
                            sizeId       = chosenSize.id,
                            milkOptionId = chosenMilk.id,
                            sugarLevel   = chosenSugar,
                        )
                        cartViewModel.addToCart(
                            CartLine(
                                key            = key,
                                item           = item,
                                chosenSize     = chosenSize,
                                chosenMilk     = chosenMilk,
                                chosenSugar    = chosenSugar,
                                quantity       = quantity,
                                unitPriceCents = unitPriceCents,
                            )
                        )
                        Toast.makeText(context, "${item.name} added to cart!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                ) {
                    Text(
                        text       = "${stringResource(R.string.label_add_to_cart)} • ${PriceCalculator.formatPrice(lineTotalCents, currencySymbol)}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
        ) {

            // ── 1. Header row — back arrow | "Item Customizer" | ❤ ───────────
            item(key = "header_nav") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Back button — white circle with border
                    Surface(
                        shape           = CircleShape,
                        color           = Color.White,
                        border          = BorderStroke(1.dp, Border),
                        shadowElevation = 2.dp,
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back_button),
                                tint               = Espresso,
                                modifier           = Modifier.size(18.dp),
                            )
                        }
                    }

                    // Center title
                    Text(
                        text       = "Item Customizer",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Espresso,
                        letterSpacing = 0.5.sp,
                    )

                    // Heart icon — white circle with border
                    Surface(
                        shape           = CircleShape,
                        color           = Color.White,
                        border          = BorderStroke(1.dp, Border),
                        shadowElevation = 2.dp,
                    ) {
                        Box(
                            modifier         = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "🤍", fontSize = 14.sp)
                        }
                    }
                }
            }

            // ── 2. Hero image — h-160dp, badge top-left, dark pill amber text ─
            item(key = "hero") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    AsyncImage(
                        model              = item.imageUrl,
                        contentDescription = stringResource(R.string.cd_hero_image),
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop,
                    )
                    // Badge — top-left, espresso dark pill with amber text
                    if (item.badge.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp),
                            shape = RoundedCornerShape(50),
                            color = Espresso.copy(alpha = 0.80f),
                        ) {
                            Text(
                                text          = item.badge,
                                modifier      = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style         = MaterialTheme.typography.labelSmall,
                                fontWeight    = FontWeight.ExtraBold,
                                color         = Amber,
                                fontSize      = 9.sp,
                                letterSpacing = 0.8.sp,
                            )
                        }
                    }
                }
            }

            // ── 3. Title, price, tagline ──────────────────────────────────────
            item(key = "title_block") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Top,
                    ) {
                        Text(
                            text       = item.name,
                            modifier   = Modifier.weight(1f),
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = DarkText,
                        )
                        Text(
                            text       = PriceCalculator.formatPrice(item.basePriceCents, currencySymbol),
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Terracotta,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = item.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedBrown,
                    )
                }
            }

            // ── 4. Rating + review count + prep time + calories ───────────────
            item(key = "meta_pills") {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfoPill("⭐ ${"%.1f".format(item.rating)} (${item.reviewCount})")
                    InfoPill("⏱ ${item.prepTime}")
                    InfoPill("🔥 ${item.calories} cal")
                }
            }

            // ── 5. Description ────────────────────────────────────────────────
            item(key = "description") {
                Column(modifier = Modifier.padding(16.dp)) {
                    HorizontalDivider(color = Border)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text  = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedBrown,
                        lineHeight = 18.sp,
                    )
                }
            }

            // ── 6. Key Ingredients ────────────────────────────────────────────
            if (item.ingredients.isNotEmpty()) {
                item(key = "ingredients") {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text(
                            text       = "Key Ingredients",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color      = DarkText,
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement   = Arrangement.spacedBy(6.dp),
                        ) {
                            item.ingredients.forEach { ingredient ->
                                Surface(
                                    shape  = RoundedCornerShape(50),
                                    color  = Color.White,
                                    border = BorderStroke(1.dp, Border),
                                ) {
                                    Text(
                                        text     = ingredient,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MutedBrown,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 7. Size Selection ─────────────────────────────────────────────
            if (item.customizations.sizes.isNotEmpty()) {
                item(key = "sizes") {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Border)
                        CustomizationSection(
                            title   = "Size Selection",
                            options = item.customizations.sizes.map { size ->
                                val extra = if (size.extraPriceCents > 0) {
                                    " (+${PriceCalculator.formatPrice(size.extraPriceCents, currencySymbol)})"
                                } else ""
                                Pair(size.id, "${size.label}$extra")
                            },
                            selectedId = chosenSize.id,
                            onSelect   = { id ->
                                chosenSize = item.customizations.sizes.first { it.id == id }
                            },
                        )
                    }
                }
            }

            // ── 8. Milk Options / Spreads ─────────────────────────────────────
            if (item.customizations.milkOptions.isNotEmpty()) {
                item(key = "milk") {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Border)
                        CustomizationSection(
                            title   = "Milk Options / Spreads",
                            options = item.customizations.milkOptions.map { milk ->
                                val extra = if (milk.extraPriceCents > 0) {
                                    " (+${PriceCalculator.formatPrice(milk.extraPriceCents, currencySymbol)})"
                                } else ""
                                Pair(milk.id, "${milk.name}$extra")
                            },
                            selectedId = chosenMilk.id,
                            onSelect   = { id ->
                                chosenMilk = item.customizations.milkOptions.first { it.id == id }
                            },
                        )
                    }
                }
            }

            // ── 9. Sugar Levels / Serving ─────────────────────────────────────
            // Sugar levels are plain strings — the string IS the key (see CartLineKey).
            if (item.customizations.sugarLevels.isNotEmpty()) {
                item(key = "sugar") {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Border)
                        CustomizationSection(
                            title      = "Sugar Levels / Serving",
                            options    = item.customizations.sugarLevels.map { Pair(it, it) },
                            selectedId = chosenSugar,
                            onSelect   = { chosenSugar = it },
                        )
                    }
                }
            }

            // ── 10. Quantity stepper ──────────────────────────────────────────
            item(key = "quantity") {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Border)
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text       = "Quantity",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color      = DarkText,
                        )
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            FilledTonalIconButton(
                                onClick  = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(36.dp),
                                enabled  = quantity > 1,
                            ) {
                                Text("−", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Text(
                                text       = quantity.toString(),
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign  = TextAlign.Center,
                                color      = DarkText,
                                modifier   = Modifier.widthIn(min = 32.dp),
                            )
                            FilledTonalIconButton(
                                onClick  = { quantity++ },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Shared sub-composables ────────────────────────────────────────────────────

/**
 * Generic single-choice chip selection section.
 * Options are (key, display-label) pairs.
 * Selected chip: terracotta fill, white text.
 * Unselected: white, dark border.
 */
@Composable
private fun CustomizationSection(
    title:      String,
    options:    List<Pair<String, String>>,
    selectedId: String,
    onSelect:   (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = DarkText,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { (id, label) ->
                val isSelected = selectedId == id
                // Surface(onClick = ...) is the clickable variant from Material3.
                // No extra modifier needed — shape, color and border handle all styling.
                Surface(
                    onClick = { onSelect(id) },
                    shape   = RoundedCornerShape(50),
                    color   = if (isSelected) Terracotta else Color.White,
                    border  = if (isSelected) null else BorderStroke(1.dp, Border),
                ) {
                    Text(
                        text       = label,
                        modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isSelected) Color.White else MutedBrown,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoPill(text: String) {
    Surface(
        shape  = RoundedCornerShape(50),
        color  = Color.White,
        border = BorderStroke(1.dp, Border),
    ) {
        Text(
            text     = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = MutedBrown,
        )
    }
}

@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.brewkery.app.ui.detail

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
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

// ─────────────────────────────────────────────────────────────────────────────
// Entry point — resolves item from menu state by itemId
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ItemDetailScreen(
    itemId:        Int,
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    onBack:        () -> Unit,
) {
    val uiState by menuViewModel.uiState.collectAsState()
    val meta    = (uiState as? MenuUiState.Success)?.meta

    when {
        uiState is MenuUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Terracotta)
        }
        meta == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Terracotta)
        }
        else -> {
            val item = (uiState as MenuUiState.Success).items.find { it.id == itemId }
            if (item == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Item not found", color = MutedBrown)
                }
            } else {
                ItemDetailContent(
                    item          = item,
                    meta          = meta,
                    cartViewModel = cartViewModel,
                    onBack        = onBack,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Full detail content exactly matching the prototype screenshots
// ─────────────────────────────────────────────────────────────────────────────

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
    var isFavorite by remember(item.id) { mutableStateOf(false) }

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
        containerColor = Color.White,
        bottomBar = {
            // ── Bottom action row — "- qty +" stepper | "Add to Cart • $X" button ──
            // Both are side-by-side on one row, matching the prototype exactly.
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color           = Color.White,
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Quantity stepper — white bordered rounded box
                    Surface(
                        shape  = RoundedCornerShape(12.dp),
                        color  = Color.White,
                        border = BorderStroke(1.dp, Border),
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Text(
                                text       = "−",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp,
                                color      = DarkText,
                                modifier   = Modifier.clickable { if (quantity > 1) quantity-- },
                            )
                            Text(
                                text       = quantity.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp,
                                color      = DarkText,
                                textAlign  = TextAlign.Center,
                            )
                            Text(
                                text       = "+",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp,
                                color      = DarkText,
                                modifier   = Modifier.clickable { quantity++ },
                            )
                        }
                    }

                    // Add to Cart button — terracotta, fills remaining width
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
                            .weight(1f)
                            .height(48.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        // Gradient background matching prototype: gradient-terracotta
                        Box(
                            modifier         = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFD9532F), // #d9532f
                                            Color(0xFFB84121), // #b84121
                                            Color(0xFF8C2B12), // #8c2b12
                                        ),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end   = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                                    ),
                                    RoundedCornerShape(12.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = "${stringResource(R.string.label_add_to_cart)} • ${PriceCalculator.formatPrice(lineTotalCents, currencySymbol)}",
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color.White,
                                fontSize   = 12.sp,
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top    = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
        ) {

            // ── 1. Header row — back arrow circle | "ITEM CUSTOMIZER" | ❤ ────
            item(key = "header_nav") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Back button — circular white bordered button
                    Surface(
                        shape           = CircleShape,
                        color           = Color.White,
                        border          = BorderStroke(1.dp, Border),
                        shadowElevation = 2.dp,
                        onClick         = onBack,
                    ) {
                        Box(
                            modifier         = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back_button),
                                tint               = Espresso,
                                modifier           = Modifier.size(18.dp),
                            )
                        }
                    }

                    // Center title — "ITEM CUSTOMIZER" all caps
                    Text(
                        text          = "ITEM CUSTOMIZER",
                        style         = MaterialTheme.typography.labelLarge,
                        fontWeight    = FontWeight.ExtraBold,
                        color         = Espresso,
                        letterSpacing = 0.8.sp,
                    )

                    // Heart icon — toggleable favorite
                    Surface(
                        shape           = CircleShape,
                        color           = Color.White,
                        border          = BorderStroke(1.dp, Border),
                        shadowElevation = 2.dp,
                        onClick         = {
                            isFavorite = !isFavorite
                            Toast.makeText(
                                context,
                                if (isFavorite) "Item saved to favorites" else "Removed from favorites",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    ) {
                        Box(
                            modifier         = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text     = if (isFavorite) "❤️" else "🤍",
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }

            // ── 2. Hero image — h-160dp (prototype: h-40), full width, badge top-left
            item(key = "hero") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                ) {
                    AsyncImage(
                        model              = item.imageUrl,
                        contentDescription = stringResource(R.string.cd_hero_image),
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop,
                    )
                    // Badge — top-left, espresso dark semi-transparent pill with amber text
                    if (item.badge.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp),
                            shape = RoundedCornerShape(50),
                            color = Espresso.copy(alpha = 0.85f),
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

            // ── 3. Title + price on same row, description below ───────────────
            item(key = "title_block") {
                Column(modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 6.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text(
                            text       = item.name,
                            modifier   = Modifier.weight(1f),
                            fontWeight = FontWeight.ExtraBold,
                            color      = DarkText,
                            fontSize   = 15.sp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = PriceCalculator.formatPrice(item.basePriceCents, currencySymbol),
                            fontWeight = FontWeight.ExtraBold,
                            color      = Terracotta,
                            fontSize   = 15.sp,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    // Description — MutedBrown per prototype: text-[#786457]
                    Text(
                        text       = item.description,
                        fontSize   = 11.sp,
                        color      = MutedBrown,
                        lineHeight = 16.sp,
                    )
                }
            }

            // ── 4. Key Ingredients ────────────────────────────────────────────
            if (item.ingredients.isNotEmpty()) {
                item(key = "ingredients") {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text(
                            text          = "KEY INGREDIENTS",
                            style         = MaterialTheme.typography.labelSmall,
                            fontWeight    = FontWeight.ExtraBold,
                            color         = MutedBrown,
                            letterSpacing = 0.8.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement   = Arrangement.spacedBy(6.dp),
                        ) {
                            item.ingredients.forEach { ingredient ->
                                Surface(
                                    shape  = RoundedCornerShape(6.dp),
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
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ── 5. Size Selection — rectangular grid buttons ──────────────────
            if (item.customizations.sizes.isNotEmpty()) {
                item(key = "sizes") {
                    CustomizationCard(title = "Size Selection") {
                        // 3-column grid of rectangle buttons matching prototype
                        val sizes = item.customizations.sizes
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            sizes.forEach { size ->
                                val isSelected = chosenSize.id == size.id
                                val extraLabel = if (size.extraPriceCents > 0)
                                    "+${PriceCalculator.formatPrice(size.extraPriceCents, currencySymbol)}"
                                else "+${currencySymbol}0.00"
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { chosenSize = size },
                                    shape  = RoundedCornerShape(10.dp),
                                    color  = if (isSelected) Color(0xFFFAEEE5) else Color.White,
                                    border = if (isSelected)
                                        BorderStroke(1.5.dp, Terracotta)
                                    else
                                        BorderStroke(1.dp, Border),
                                ) {
                                    Column(
                                        modifier            = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text       = size.label,
                                            style      = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color      = if (isSelected) Terracotta else MutedBrown,
                                            textAlign  = TextAlign.Center,
                                            fontSize   = 10.sp,
                                        )
                                        Text(
                                            text      = extraLabel,
                                            style     = MaterialTheme.typography.labelSmall,
                                            color     = if (isSelected) Terracotta.copy(alpha = 0.8f) else MutedBrown.copy(alpha = 0.7f),
                                            fontSize  = 9.sp,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 6. Milk Options / Spreads — full-width rows ───────────────────
            if (item.customizations.milkOptions.isNotEmpty()) {
                item(key = "milk") {
                    CustomizationCard(title = "Milk Options / Spreads") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            item.customizations.milkOptions.forEach { milk ->
                                val isSelected = chosenMilk.id == milk.id
                                val extraLabel = if (milk.extraPriceCents > 0)
                                    "+${PriceCalculator.formatPrice(milk.extraPriceCents, currencySymbol)}"
                                else "+${currencySymbol}0.00"
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { chosenMilk = milk },
                                    shape  = RoundedCornerShape(8.dp),
                                    color  = if (isSelected) Color(0xFFFAEEE5) else Color.White,
                                    border = if (isSelected)
                                        BorderStroke(1.dp, Terracotta)
                                    else
                                        BorderStroke(1.dp, Border),
                                ) {
                                    Row(
                                        modifier              = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment     = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text       = milk.name,
                                            style      = MaterialTheme.typography.bodySmall,
                                            color      = if (isSelected) Terracotta else MutedBrown,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        )
                                        Text(
                                            text      = extraLabel,
                                            style     = MaterialTheme.typography.labelSmall,
                                            color     = if (isSelected) Terracotta else MutedBrown,
                                            fontWeight= FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 7. Sugar Levels / Serving — rounded-lg chips, Espresso for selected ─
            if (item.customizations.sugarLevels.isNotEmpty()) {
                item(key = "sugar") {
                    CustomizationCard(title = "Sugar Levels / Serving") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement   = Arrangement.spacedBy(6.dp),
                        ) {
                            item.customizations.sugarLevels.forEach { sugar ->
                                val isSelected = chosenSugar == sugar
                                // Selected: Espresso dark fill per prototype: bg-[#140b07] text-white border-[#140b07]
                                // Unselected: white bg, muted border, muted text
                                // Shape: rounded-lg = RoundedCornerShape(8.dp) NOT pills
                                Surface(
                                    onClick = { chosenSugar = sugar },
                                    shape   = RoundedCornerShape(8.dp),
                                    color   = if (isSelected) Espresso else Color.White,
                                    border  = BorderStroke(1.dp, if (isSelected) Espresso else Border),
                                ) {
                                    Text(
                                        text       = sugar,
                                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color      = if (isSelected) Color.White else MutedBrown,
                                        fontSize   = 10.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Small bottom spacer so content isn't flush with bottom bar
            item(key = "bottom_space") {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Customization card wrapper — white bordered card matching prototype ────────

@Composable
private fun CustomizationCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape  = RoundedCornerShape(12.dp),
        color  = Color.White,
        border = BorderStroke(1.dp, Border),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color      = DarkText,
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

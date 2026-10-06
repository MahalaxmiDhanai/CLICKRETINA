package com.brewkery.app.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brewkery.app.R
import com.brewkery.app.domain.Category
import com.brewkery.app.domain.MenuItem
import com.brewkery.app.domain.Order
import com.brewkery.app.domain.PriceCalculator
import com.brewkery.app.domain.StoreMeta
import com.brewkery.app.ui.cart.CartViewModel
import com.brewkery.app.ui.theme.Amber
import com.brewkery.app.ui.theme.BannerFrom
import com.brewkery.app.ui.theme.BannerTo
import com.brewkery.app.ui.theme.Border
import com.brewkery.app.ui.theme.DarkText
import com.brewkery.app.ui.theme.Espresso
import com.brewkery.app.ui.theme.MutedBrown
import com.brewkery.app.ui.theme.Terracotta

// ─────────────────────────────────────────────────────────────────────────────
// Screen entry point
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    onItemClick:   (Int) -> Unit,
    onCartClick:   () -> Unit,
    onOrderClick:  () -> Unit,
) {
    val uiState       by menuViewModel.uiState.collectAsState()
    val selectedCatId by menuViewModel.selectedCategoryId.collectAsState()
    val cartCount     by cartViewModel.cartItemCount.collectAsState()
    val subtotalCents by cartViewModel.subtotalCents.collectAsState()
    val activeOrder   by cartViewModel.activeOrder.collectAsState()

    // Local search query state — lives at this scope so it survives recompositions
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        // No TopAppBar Scaffold slot — we render the top bar as the first LazyColumn item
        // so it scrolls with the banner. The BK bar is always pinned via stickyHeader.
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // "View Your Cart" sticky bar — only shown when cart has items
            val successState = uiState as? MenuUiState.Success
            if (cartCount > 0 && successState != null) {
                CartStickyBar(
                    cartCount      = cartCount,
                    subtotalCents  = subtotalCents,
                    currencySymbol = successState.meta.currencySymbol,
                    onViewCart     = onCartClick,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (val state = uiState) {
                is MenuUiState.Loading -> LoadingState()

                is MenuUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = menuViewModel::fetchMenu,
                )

                is MenuUiState.Success -> {
                    // Apply category filter first, then search query
                    val categoryFiltered = if (selectedCatId == null) {
                        state.items
                    } else {
                        state.items.filter { it.categoryId == selectedCatId }
                    }
                    val filteredItems = if (searchQuery.isBlank()) {
                        categoryFiltered
                    } else {
                        val q = searchQuery.trim().lowercase()
                        categoryFiltered.filter { item ->
                            item.name.lowercase().contains(q) ||
                                item.tagline.lowercase().contains(q) ||
                                item.categoryId.lowercase().contains(q)
                        }
                    }

                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {

                        // ── Top app bar (scrolls with content) ────────────────
                        item(key = "appbar") {
                            BrewkeryAppBar(
                                cartCount   = cartCount,
                                onCartClick = onCartClick,
                            )
                        }

                        // ── Store info / active-order banner ──────────────────
                        item(key = "banner") {
                            val currentOrder = activeOrder
                            if (currentOrder != null) {
                                ActiveOrderCard(
                                    order        = currentOrder,
                                    onOrderClick = onOrderClick,
                                )
                            } else {
                                StoreInfoBanner(meta = state.meta)
                            }
                        }

                        // ── Search bar ────────────────────────────────────────
                        item(key = "search") {
                            BrewkerySearchBar(
                                query    = searchQuery,
                                onQuery  = { searchQuery = it },
                            )
                        }

                        // ── Category chip row — sticky below search bar ───────
                        stickyHeader(key = "chips") {
                            CategoryChipRow(
                                categories         = state.categories,
                                selectedCategoryId = selectedCatId,
                                onCategorySelected = menuViewModel::selectCategory,
                            )
                        }

                        // ── Item list or empty state ──────────────────────────
                        if (filteredItems.isEmpty()) {
                            item(key = "empty") {
                                EmptyItemsState()
                            }
                        } else {
                            items(filteredItems, key = { it.id }) { item ->
                                MenuItemCard(
                                    item           = item,
                                    currencySymbol = state.meta.currencySymbol,
                                    onItemClick    = { onItemClick(item.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top app bar — matches prototype exactly:
//   Left:  BK dark circle + tagline + "Brewkery Artisans"
//   Right: shopping-bag icon in white bordered circle with terracotta badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BrewkeryAppBar(cartCount: Int, onCartClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // BK logo + wordmark
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Espresso-filled circle with "BK" in white
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Espresso),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = "BK",
                    color      = Color.White,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text       = "Fresh Roast & Bakes",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = MutedBrown,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text       = "Brewkery Artisans",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Espresso,
                )
            }
        }

        // Cart icon — shopping bag in white circle with border; terracotta badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onCartClick),
            contentAlignment = Alignment.Center,
        ) {
            // Outline border ring
            Surface(
                modifier = Modifier.size(36.dp),
                shape    = CircleShape,
                color    = Color.White,
                border   = androidx.compose.foundation.BorderStroke(1.dp, Border),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Shopping bag emoji icon (matches prototype's fa-bag-shopping)
                    Text(text = "🛍", fontSize = 14.sp)
                }
            }

            // Quantity badge — top-right, hidden when 0
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Terracotta),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = if (cartCount > 9) "9+" else cartCount.toString(),
                        color      = Color.White,
                        fontSize   = 8.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Store info banner — gradient background, scooter in white bordered box,
// STORE INFO label in terracotta, bold delivery text, "Open" pill
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StoreInfoBanner(meta: StoreMeta) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(listOf(BannerFrom, BannerTo))
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // White bordered box with scooter emoji
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape    = RoundedCornerShape(10.dp),
                    color    = Color.White,
                    border   = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🛵", fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text       = "STORE INFO",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = Terracotta,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 9.sp,
                            letterSpacing = 0.8.sp,
                        )
                        Spacer(Modifier.width(4.dp))
                        // Terracotta dot separator
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Terracotta),
                        )
                    }
                    Text(
                        text       = "Delivery in ${meta.estimatedDeliveryTime}",
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = DarkText,
                    )
                    Text(
                        text  = "${PriceCalculator.formatPrice(meta.deliveryFeeCents, meta.currencySymbol)} flat fee",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedBrown,
                    )
                }
            }

            // "Open" pill — white background, dark bordered
            Surface(
                shape  = RoundedCornerShape(8.dp),
                color  = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
            ) {
                Text(
                    text       = "Open",
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = DarkText,
                    fontSize   = 10.sp,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search bar — white background, border, magnifying glass, placeholder
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BrewkerySearchBar(query: String, onQuery: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape  = RoundedCornerShape(12.dp),
        color  = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "🔍", fontSize = 12.sp, color = MutedBrown)
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value         = query,
                onValueChange = onQuery,
                modifier      = Modifier.weight(1f),
                singleLine    = true,
                textStyle     = MaterialTheme.typography.bodySmall.copy(color = DarkText),
                cursorBrush   = SolidColor(Terracotta),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text  = "Search roast, cold brew, pastry…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedBrown,
                        )
                    }
                    inner()
                },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category chip row — sticky header
//   Selected: filled espresso dark (#140B07) text white rounded-full
//   Unselected: white background, border, muted text, rounded-full
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CategoryChipRow(
    categories:         List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.background,
    ) {
        LazyRow(
            modifier              = Modifier.fillMaxWidth(),
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // "All Items" chip — always first
            item(key = "all") {
                CategoryPill(
                    label      = "All Items",
                    isSelected = selectedCategoryId == null,
                    onClick    = { onCategorySelected(null) },
                )
            }
            items(categories, key = { it.id }) { cat ->
                CategoryPill(
                    label      = "${cat.icon} ${cat.name}",
                    isSelected = selectedCategoryId == cat.id,
                    onClick    = { onCategorySelected(cat.id) },
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape    = RoundedCornerShape(50),
        color    = if (isSelected) Espresso else Color.White,
        border   = if (isSelected) null else
            androidx.compose.foundation.BorderStroke(1.dp, Border),
    ) {
        Text(
            text       = label,
            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color      = if (isSelected) Color.White else MutedBrown,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Menu item card — thumbnail left, badge chip, name, ⭐ rating, price
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MenuItemCard(
    item:           MenuItem,
    currencySymbol: String,
    onItemClick:    () -> Unit,
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onItemClick),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        border    = androidx.compose.foundation.BorderStroke(1.dp, Border),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thumbnail
            AsyncImage(
                model              = item.imageUrl,
                contentDescription = stringResource(R.string.cd_item_image),
                modifier           = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale       = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Badge chip (e.g. BESTSELLER, FRESHLY BAKED)
                if (item.badge.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Espresso.copy(alpha = 0.08f),
                    ) {
                        Text(
                            text       = item.badge,
                            modifier   = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Terracotta,
                            fontSize   = 9.sp,
                            letterSpacing = 0.5.sp,
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    text       = item.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = DarkText,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                // Tagline
                Text(
                    text     = item.tagline,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MutedBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⭐", fontSize = 12.sp)
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text  = "%.1f".format(item.rating),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedBrown,
                        )
                    }
                    // Price in terracotta
                    Text(
                        text       = PriceCalculator.formatPrice(item.basePriceCents, currencySymbol),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Terracotta,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sticky cart bottom bar — espresso (#140B07) background
//   Left:  terracotta qty bubble + "View Your Cart" + subtotal
//   Right: "Proceed to Checkout →" in amber bold
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CartStickyBar(
    cartCount:      Int,
    subtotalCents:  Long,
    currencySymbol: String,
    onViewCart:     () -> Unit,
) {
    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewCart),
        color           = Espresso,
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Terracotta quantity bubble
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Terracotta),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = cartCount.toString(),
                        color      = Color.White,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text  = "View Your Cart",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEBD8CB),
                    )
                    Text(
                        text       = PriceCalculator.formatPrice(subtotalCents, currencySymbol),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                    )
                }
            }
            // "Proceed to Checkout →" in amber
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = "Proceed to Checkout",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Amber,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier           = Modifier.size(14.dp),
                    tint               = Amber,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Active order tracking card — shown in the banner slot after an order is placed
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ActiveOrderCard(order: Order, onOrderClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(BannerFrom, BannerTo)))
            .clickable(onClick = onOrderClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ticket icon box
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape    = RoundedCornerShape(10.dp),
                    color    = Color.White,
                    border   = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🎫", fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text       = "ACTIVE ORDER",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = Terracotta,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 9.sp,
                            letterSpacing = 0.8.sp,
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Terracotta),
                        )
                    }
                    Text(
                        text       = order.ticketId,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = DarkText,
                    )
                    Text(
                        text  = "Est. wait: ${order.estimatedWait}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedBrown,
                    )
                }
            }

            // PREPARING pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Terracotta.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Terracotta.copy(alpha = 0.3f)),
            ) {
                Text(
                    text       = "PREPARING",
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Terracotta,
                    fontSize   = 10.sp,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utility states
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Terracotta)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text      = "☕",
            style     = MaterialTheme.typography.displaySmall,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape   = RoundedCornerShape(12.dp),
        ) {
            Text(stringResource(R.string.label_retry))
        }
    }
}

@Composable
fun EmptyItemsState() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "☕", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(12.dp))
            Text(
                text      = "No items in this category",
                style     = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color     = MutedBrown,
            )
        }
    }
}

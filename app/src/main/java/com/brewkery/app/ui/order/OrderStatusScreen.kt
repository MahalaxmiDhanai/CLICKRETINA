@file:OptIn(ExperimentalMaterial3Api::class)

package com.brewkery.app.ui.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewkery.app.R
import com.brewkery.app.domain.Order
import com.brewkery.app.ui.cart.CartViewModel
import com.brewkery.app.ui.theme.Amber
import com.brewkery.app.ui.theme.BannerFrom
import com.brewkery.app.ui.theme.BannerTo
import com.brewkery.app.ui.theme.Border
import com.brewkery.app.ui.theme.DarkText
import com.brewkery.app.ui.theme.Espresso
import com.brewkery.app.ui.theme.MutedBrown
import com.brewkery.app.ui.theme.Terracotta

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun OrderStatusScreen(
    cartViewModel: CartViewModel,
    onBackToMenu:  () -> Unit,
) {
    val activeOrder by cartViewModel.activeOrder.collectAsState()

    if (activeOrder == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Terracotta)
        }
    } else {
        OrderStatusContent(order = activeOrder!!, onBackToMenu = onBackToMenu)
    }
}

// ── Order status content ──────────────────────────────────────────────────────

@Composable
private fun OrderStatusContent(order: Order, onBackToMenu: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text       = stringResource(R.string.label_order_dispatched),
                        fontWeight = FontWeight.Bold,
                        color      = Espresso,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Success badge at top
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(BannerFrom, BannerTo)
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "☕", fontSize = 32.sp)
            }

            // Headings
            Text(
                text       = stringResource(R.string.label_brewing_in_progress),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = Espresso,
                textAlign  = TextAlign.Center,
            )
            Text(
                text      = stringResource(R.string.label_barista_message),
                style     = MaterialTheme.typography.bodySmall,
                color     = MutedBrown,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            // ── Ticket card ───────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                border    = BorderStroke(1.dp, Border),
            ) {
                Column(
                    modifier            = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Ticket ID
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text      = "Order Ticket",
                            style     = MaterialTheme.typography.labelSmall,
                            color     = MutedBrown,
                        )
                        Text(
                            text       = order.ticketId,
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Espresso,
                        )
                    }

                    // PREPARING status chip — amber tint
                    Surface(
                        shape  = RoundedCornerShape(50),
                        color  = Amber.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Amber.copy(alpha = 0.5f)),
                    ) {
                        Text(
                            text       = stringResource(R.string.label_preparing),
                            modifier   = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color(0xFFB45309), // amber-700 for legibility
                            letterSpacing = 1.sp,
                        )
                    }

                    HorizontalDivider(color = Border)

                    // Detail rows
                    OrderDetailRow("Estimated Wait",  order.estimatedWait)
                    OrderDetailRow(
                        label = "Items Ordered",
                        value = "${order.totalItemCount} Item${if (order.totalItemCount != 1) "s" else ""}",
                    )

                    HorizontalDivider(color = Border)

                    // Status message
                    Text(
                        text       = stringResource(R.string.label_barista_accepted),
                        style      = MaterialTheme.typography.bodySmall,
                        color      = Terracotta,
                        fontWeight = FontWeight.SemiBold,
                        textAlign  = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Back to Menu button
            Button(
                onClick  = onBackToMenu,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Espresso),
            ) {
                Text(
                    text       = stringResource(R.string.label_back_to_menu),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                )
            }
        }
    }
}

// ── Detail row ────────────────────────────────────────────────────────────────

@Composable
private fun OrderDetailRow(label: String, value: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MutedBrown,
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = DarkText,
        )
    }
}

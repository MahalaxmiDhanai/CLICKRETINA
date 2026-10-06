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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewkery.app.R
import com.brewkery.app.domain.Order
import com.brewkery.app.ui.cart.CartViewModel
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

// ── Order status content matching prototype exactly ───────────────────────────

@Composable
private fun OrderStatusContent(order: Order, onBackToMenu: () -> Unit) {
    Scaffold(
        containerColor = Color(0xFFFDFAF7),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))

                // Mug Badge: bg-[#f7ebe1] border-2 border-[#d9532f]
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape    = CircleShape,
                    color    = Color(0xFFF7EBE1),
                    border   = BorderStroke(2.dp, Terracotta),
                    shadowElevation = 4.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "☕", fontSize = 34.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // "ORDER DISPATCHED" small uppercase tracking-wider in terracotta
                Text(
                    text          = "ORDER DISPATCHED",
                    style         = MaterialTheme.typography.labelSmall,
                    fontWeight    = FontWeight.Black,
                    color         = Terracotta,
                    letterSpacing = 1.2.sp,
                    fontSize      = 10.sp,
                )

                Spacer(Modifier.height(4.dp))

                // "Brewing in Progress!"
                Text(
                    text       = "Brewing in Progress!",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color      = Espresso,
                    textAlign  = TextAlign.Center,
                    fontSize   = 22.sp,
                )

                Spacer(Modifier.height(4.dp))

                // Subtitle
                Text(
                    text       = "Your ticket was dispatched to our barista.",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = MutedBrown,
                    textAlign  = TextAlign.Center,
                    lineHeight = 16.sp,
                )

                Spacer(Modifier.height(24.dp))

                // ── Ticket Card ───────────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    border    = BorderStroke(1.dp, Border),
                ) {
                    Column(
                        modifier            = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Ticket header row: Order Ticket + ref code | PREPARING chip
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text          = "ORDER TICKET",
                                    fontSize      = 9.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = MutedBrown,
                                    letterSpacing = 0.5.sp,
                                )
                                Text(
                                    text       = order.ticketId,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 15.sp,
                                    color      = Espresso,
                                )
                            }

                            // PREPARING chip: bg-amber-100 text-amber-800
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFFEF3C7),
                            ) {
                                Text(
                                    text          = "PREPARING",
                                    modifier      = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize      = 10.sp,
                                    fontWeight    = FontWeight.Black,
                                    color         = Color(0xFF92400E),
                                    letterSpacing = 0.5.sp,
                                )
                            }
                        }

                        HorizontalDivider(color = Border.copy(alpha = 0.6f))

                        // Estimated Wait row: value in terracotta bold
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text     = "Estimated Wait:",
                                fontSize = 12.sp,
                                color    = MutedBrown,
                            )
                            Text(
                                text       = order.estimatedWait,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Terracotta,
                            )
                        }

                        // Items Ordered row: value in espresso semibold
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text     = "Items Ordered:",
                                fontSize = 12.sp,
                                color    = MutedBrown,
                            )
                            Text(
                                text       = "${order.totalItemCount} Item${if (order.totalItemCount != 1) "s" else ""}",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Espresso,
                            )
                        }

                        HorizontalDivider(color = Border.copy(alpha = 0.6f))

                        // Status block
                        Column {
                            Text(
                                text     = "Status:",
                                fontSize = 10.sp,
                                color    = MutedBrown,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text       = "Barista accepted your order!",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFF047857), // emerald-700
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Return Back to Menu Button: bg-[#140b07] text-white font-extrabold
            Button(
                onClick  = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Espresso),
            ) {
                Text(
                    text       = "Back to Menu",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White,
                )
            }
        }
    }
}

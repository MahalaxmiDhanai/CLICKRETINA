package com.brewkery.app.domain

// ── Store configuration (from meta) ──────────────────────────────────────────

data class StoreMeta(
    val appName:              String,
    val tagline:              String,
    val currencySymbol:       String,
    val deliveryFeeCents:     Long,   // stored in cents to avoid floating-point math
    val taxRatePercent:       Double,
    val estimatedDeliveryTime: String,
)

// ── Category ─────────────────────────────────────────────────────────────────

data class Category(
    val id:   String,
    val name: String,
    val icon: String,
)

// ── Menu item domain model ────────────────────────────────────────────────────

data class MenuItem(
    val id:            Int,
    val categoryId:    String,
    val name:          String,
    val tagline:       String,
    val description:   String,
    val basePriceCents: Long,  // cents
    val rating:        Double,
    val reviewCount:   Int,
    val prepTime:      String,
    val calories:      Int,
    val imageUrl:      String,
    val badge:         String,
    val ingredients:   List<String>,
    val customizations: ItemCustomizations,
)

data class ItemCustomizations(
    val sizes:       List<SizeOption>,
    val sugarLevels: List<String>,
    val milkOptions: List<MilkOption>,
)

data class SizeOption(
    val id:             String,
    val label:          String,
    val extraPriceCents: Long,  // cents
)

data class MilkOption(
    val id:             String,
    val name:           String,
    val extraPriceCents: Long,  // cents
)

// ── Cart ──────────────────────────────────────────────────────────────────────

/**
 * Identity of a cart line: the combination of item + chosen options.
 * Option IDs (e.g. "sz_medium", "m_oat") repeat across items, so itemId
 * is always part of the key to prevent collisions.
 */
data class CartLineKey(
    val itemId:      Int,
    val sizeId:      String,
    val milkOptionId: String,
    val sugarLevel:  String,
)

data class CartLine(
    val key:          CartLineKey,
    val item:         MenuItem,
    val chosenSize:   SizeOption,
    val chosenMilk:   MilkOption,
    val chosenSugar:  String,
    val quantity:     Int,
    // Derived: unitPriceCents = basePriceCents + size.extra + milk.extra + sugar.extra
    val unitPriceCents: Long,
)

val CartLine.lineTotalCents: Long get() = unitPriceCents * quantity

// ── Order ─────────────────────────────────────────────────────────────────────

enum class OrderStatus { PREPARING }

data class Order(
    val ticketId:        String,          // format: #BK-XXXXX
    val status:          OrderStatus,
    val totalItemCount:  Int,
    val estimatedWait:   String,
)

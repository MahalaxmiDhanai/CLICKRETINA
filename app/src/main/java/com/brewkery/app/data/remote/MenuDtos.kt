package com.brewkery.app.data.remote

import com.google.gson.annotations.SerializedName

// ── Root response ─────────────────────────────────────────────────────────────

data class MenuResponse(
    @SerializedName("meta")       val meta:       MetaDto?       = null,
    @SerializedName("categories") val categories: List<CategoryDto> = emptyList(),
    @SerializedName("items")      val items:      List<MenuItemDto> = emptyList(),
)

// ── Meta ──────────────────────────────────────────────────────────────────────

data class MetaDto(
    @SerializedName("app")                      val app:                   String? = null,
    @SerializedName("version")                  val version:               String? = null,
    @SerializedName("tagline")                  val tagline:               String? = null,
    @SerializedName("currency")                 val currency:              String? = null,
    @SerializedName("currency_symbol")          val currencySymbol:        String? = null,
    @SerializedName("delivery_fee")             val deliveryFee:           Double? = null,
    @SerializedName("tax_rate_percent")         val taxRatePercent:        Double? = null,
    @SerializedName("estimated_delivery_time")  val estimatedDeliveryTime: String? = null,
)

// ── Category ─────────────────────────────────────────────────────────────────

data class CategoryDto(
    @SerializedName("id")         val id:        String? = null,
    @SerializedName("name")       val name:      String? = null,
    @SerializedName("icon")       val icon:      String? = null,
    @SerializedName("item_count") val itemCount: Int?    = null,
)

// ── Menu item ─────────────────────────────────────────────────────────────────

data class MenuItemDto(
    @SerializedName("id")             val id:             Int?               = null,
    @SerializedName("category_id")    val categoryId:     String?            = null,
    @SerializedName("name")           val name:           String?            = null,
    @SerializedName("tagline")        val tagline:        String?            = null,
    @SerializedName("description")    val description:    String?            = null,
    @SerializedName("base_price")     val basePrice:      Double?            = null,
    @SerializedName("rating")         val rating:         Double?            = null,
    @SerializedName("review_count")   val reviewCount:    Int?               = null,
    @SerializedName("prep_time")      val prepTime:       String?            = null,
    @SerializedName("calories")       val calories:       Int?               = null,
    @SerializedName("image_url")      val imageUrl:       String?            = null,
    @SerializedName("badge")          val badge:          String?            = null,
    @SerializedName("ingredients")    val ingredients:    List<String>       = emptyList(),
    @SerializedName("customizations") val customizations: CustomizationsDto? = null,
)

// ── Customizations ────────────────────────────────────────────────────────────

data class CustomizationsDto(
    @SerializedName("sizes")        val sizes:       List<SizeOptionDto>  = emptyList(),
    @SerializedName("sugar_levels") val sugarLevels: List<String>         = emptyList(),
    @SerializedName("milk_options") val milkOptions: List<MilkOptionDto>  = emptyList(),
)

data class SizeOptionDto(
    @SerializedName("id")          val id:         String? = null,
    @SerializedName("label")       val label:      String? = null,
    @SerializedName("extra_price") val extraPrice: Double? = null,
)

data class MilkOptionDto(
    @SerializedName("id")          val id:         String? = null,
    @SerializedName("name")        val name:       String? = null,
    @SerializedName("extra_price") val extraPrice: Double? = null,
)

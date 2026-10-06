package com.brewkery.app.data.repository

import com.brewkery.app.domain.CartLine
import com.brewkery.app.domain.CartLineKey
import com.brewkery.app.domain.Order
import com.brewkery.app.domain.OrderStatus
import com.brewkery.app.domain.StoreMeta
import com.brewkery.app.domain.TicketIdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory, app-scoped cart and active order state.
 * Cart resets on process death (no persistence required by spec).
 * Thread-safe via StateFlow's atomic updates.
 */
class CartRepository {

    // ── Cart ─────────────────────────────────────────────────────────────────

    private val _cartLines = MutableStateFlow<Map<CartLineKey, CartLine>>(emptyMap())
    val cartLines: StateFlow<Map<CartLineKey, CartLine>> = _cartLines.asStateFlow()

    /**
     * Adds a line to the cart. If the same key already exists
     * (itemId + sizeId + milkOptionId + sugarLevel), quantities are merged.
     */
    fun addToCart(line: CartLine) {
        _cartLines.update { current ->
            val existing = current[line.key]
            if (existing != null) {
                current + (line.key to existing.copy(quantity = existing.quantity + line.quantity))
            } else {
                current + (line.key to line)
            }
        }
    }

    /**
     * Updates the quantity of a line. Passing newQuantity <= 0 removes the line.
     */
    fun updateQuantity(key: CartLineKey, newQuantity: Int) {
        _cartLines.update { current ->
            when {
                newQuantity <= 0 -> current - key
                else -> {
                    val existing = current[key] ?: return@update current
                    current + (key to existing.copy(quantity = newQuantity))
                }
            }
        }
    }

    fun clearCart() {
        _cartLines.value = emptyMap()
    }

    // ── Active Order ──────────────────────────────────────────────────────────

    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    /**
     * Generates a ticket, clears the cart, and stores the active order.
     * Returns the new Order so callers can navigate to the Order Status screen.
     */
    fun placeOrder(meta: StoreMeta): Order {
        val lines      = _cartLines.value.values.toList()
        val totalQty   = lines.sumOf { it.quantity }
        val ticketId   = TicketIdGenerator.generate()
        val order      = Order(
            ticketId       = ticketId,
            status         = OrderStatus.PREPARING,
            totalItemCount = totalQty,
            estimatedWait  = meta.estimatedDeliveryTime,
        )
        clearCart()
        _activeOrder.value = order
        return order
    }
}

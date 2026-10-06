package com.brewkery.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brewkery.app.AppContainer
import com.brewkery.app.data.repository.CartRepository
import com.brewkery.app.domain.CartLine
import com.brewkery.app.domain.CartLineKey
import com.brewkery.app.domain.Order
import com.brewkery.app.domain.PriceCalculator
import com.brewkery.app.domain.StoreMeta
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Activity-scoped ViewModel — all screens share the same instance via the
 * ViewModelStore of MainActivity, ensuring cart state is consistent across navigation.
 */
class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    /** Ordered list of cart lines for display. */
    val cartLines: StateFlow<List<CartLine>> = cartRepository.cartLines
        .map { it.values.toList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Currently active order (set after placeOrder, cleared on dismissal). */
    val activeOrder: StateFlow<Order?> = cartRepository.activeOrder

    /** Total number of individual items (sum of quantities). */
    val cartItemCount: StateFlow<Int> = cartLines
        .map { lines -> lines.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** Subtotal in cents — used by menu bottom bar and cart summary. */
    val subtotalCents: StateFlow<Long> = cartLines
        .map { lines -> PriceCalculator.subtotalCents(lines) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    fun addToCart(line: CartLine) = cartRepository.addToCart(line)

    fun updateQuantity(key: CartLineKey, newQuantity: Int) =
        cartRepository.updateQuantity(key, newQuantity)

    fun clearCart() = cartRepository.clearCart()

    /**
     * Places the order: clears the cart and stores the active order in the repository.
     * Returns the new Order so the caller can navigate to the Order Status screen.
     */
    fun placeOrder(meta: StoreMeta): Order = cartRepository.placeOrder(meta)

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CartViewModel(container.cartRepository) as T
            }
    }
}

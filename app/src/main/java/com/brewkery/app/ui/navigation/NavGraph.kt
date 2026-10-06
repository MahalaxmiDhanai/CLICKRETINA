package com.brewkery.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.brewkery.app.ui.cart.CartScreen
import com.brewkery.app.ui.cart.CartViewModel
import com.brewkery.app.ui.detail.ItemDetailScreen
import com.brewkery.app.ui.menu.MenuScreen
import com.brewkery.app.ui.menu.MenuViewModel
import com.brewkery.app.ui.order.OrderStatusScreen

// ── Route definitions ─────────────────────────────────────────────────────────

object NavRoutes {
    const val MENU   = "menu"
    const val DETAIL = "detail/{itemId}"
    const val CART   = "cart"
    const val ORDER  = "order"

    fun detail(itemId: Int) = "detail/$itemId"
}

// ── Nav host ─────────────────────────────────────────────────────────────────

@Composable
fun BrewkeryNavGraph(
    navController: NavHostController,
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController    = navController,
        startDestination = NavRoutes.MENU,
        modifier         = modifier,
    ) {

        // ── Menu ─────────────────────────────────────────────────────────────
        composable(NavRoutes.MENU) {
            MenuScreen(
                menuViewModel = menuViewModel,
                cartViewModel = cartViewModel,
                onItemClick   = { itemId ->
                    navController.navigate(NavRoutes.detail(itemId))
                },
                onCartClick   = { navController.navigate(NavRoutes.CART) },
                onOrderClick  = { navController.navigate(NavRoutes.ORDER) },
            )
        }

        // ── Item Detail ───────────────────────────────────────────────────────
        composable(
            route     = NavRoutes.DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
            ItemDetailScreen(
                itemId        = itemId,
                menuViewModel = menuViewModel,
                cartViewModel = cartViewModel,
                onBack        = { navController.popBackStack() },
            )
        }

        // ── Cart ──────────────────────────────────────────────────────────────
        composable(NavRoutes.CART) {
            CartScreen(
                menuViewModel = menuViewModel,
                cartViewModel = cartViewModel,
                onBack        = { navController.popBackStack() },
                onOrderPlaced = {
                    // Pop CART off the back stack so pressing Back from ORDER
                    // returns to MENU — not to an empty cart.
                    navController.navigate(NavRoutes.ORDER) {
                        popUpTo(NavRoutes.CART) { inclusive = true }
                    }
                },
            )
        }

        // ── Order Status ──────────────────────────────────────────────────────
        composable(NavRoutes.ORDER) {
            OrderStatusScreen(
                cartViewModel = cartViewModel,
                onBackToMenu  = {
                    // Pop back to MENU. After Cart→Order navigation, the back stack
                    // is: MENU → ORDER (CART was already popped). popBackStack() is enough.
                    navController.popBackStack()
                },
            )
        }
    }
}

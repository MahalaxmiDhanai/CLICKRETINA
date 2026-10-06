package com.brewkery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.brewkery.app.ui.cart.CartViewModel
import com.brewkery.app.ui.menu.MenuViewModel
import com.brewkery.app.ui.navigation.BrewkeryNavGraph
import com.brewkery.app.ui.theme.BrewkeryTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Obtain the app-level dependency container
        val container = (applicationContext as BrewkeryApp).container

        setContent {
            BrewkeryTheme {
                val navController = rememberNavController()

                // Activity-scoped ViewModels survive configuration changes and navigation.
                // Both are created once and shared across all screens via the NavGraph.
                val menuViewModel: MenuViewModel = viewModel(
                    factory = MenuViewModel.factory(container),
                )
                val cartViewModel: CartViewModel = viewModel(
                    factory = CartViewModel.factory(container),
                )

                BrewkeryNavGraph(
                    navController = navController,
                    menuViewModel = menuViewModel,
                    cartViewModel = cartViewModel,
                )
            }
        }
    }
}

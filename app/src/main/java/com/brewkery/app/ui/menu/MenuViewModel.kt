package com.brewkery.app.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brewkery.app.AppContainer
import com.brewkery.app.data.repository.MenuRepository
import com.brewkery.app.data.repository.NetworkResult
import com.brewkery.app.domain.Category
import com.brewkery.app.domain.MenuItem
import com.brewkery.app.domain.StoreMeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ─────────────────────────────────────────────────────────────────

sealed interface MenuUiState {
    data object Loading : MenuUiState
    data class Error(val message: String) : MenuUiState
    data class Success(
        val meta:       StoreMeta,
        val categories: List<Category>,
        val items:      List<MenuItem>,
    ) : MenuUiState
}

// ── ViewModel ────────────────────────────────────────────────────────────────

class MenuViewModel(private val menuRepository: MenuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    /** null means "All Items" (no category filter applied). */
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    init { fetchMenu() }

    fun fetchMenu() {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            _uiState.value = when (val result = menuRepository.getMenu()) {
                is NetworkResult.Success -> MenuUiState.Success(
                    meta       = result.data.meta,
                    categories = result.data.categories,
                    items      = result.data.items,
                )
                is NetworkResult.Error -> MenuUiState.Error(result.message)
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    /** Looks up a single item from the already-loaded menu list. No extra network call. */
    fun getItemById(id: Int): MenuItem? =
        (_uiState.value as? MenuUiState.Success)?.items?.find { it.id == id }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MenuViewModel(container.menuRepository) as T
            }
    }
}

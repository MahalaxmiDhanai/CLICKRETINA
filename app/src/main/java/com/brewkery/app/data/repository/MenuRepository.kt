package com.brewkery.app.data.repository

import com.brewkery.app.data.remote.BrewkeryApi
import com.brewkery.app.data.remote.toDomain
import com.brewkery.app.domain.Category
import com.brewkery.app.domain.MenuItem
import com.brewkery.app.domain.StoreMeta
import retrofit2.HttpException
import java.io.IOException

/** Sealed result wrapper — keeps ViewModels decoupled from Retrofit/IO details. */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}

data class MenuData(
    val meta:       StoreMeta,
    val categories: List<Category>,
    val items:      List<MenuItem>,
)

class MenuRepository(private val api: BrewkeryApi) {

    /**
     * Fetches the full menu once per app session.
     * IOException  → network unavailable.
     * HttpException → non-2xx from server.
     * Any other Throwable is also caught to prevent uncaught crashes.
     */
    suspend fun getMenu(): NetworkResult<MenuData> = try {
        val response = api.getMenu()
        val meta       = response.meta?.toDomain()
            ?: return NetworkResult.Error("Missing store configuration.")
        val categories = response.categories.map { it.toDomain() }
        val items      = response.items.map { it.toDomain() }
        NetworkResult.Success(MenuData(meta, categories, items))
    } catch (e: IOException) {
        NetworkResult.Error("Network error: ${e.message}")
    } catch (e: HttpException) {
        NetworkResult.Error("Server error ${e.code()}: ${e.message()}")
    } catch (e: Exception) {
        NetworkResult.Error("Unexpected error: ${e.message}")
    }

    /** Fetches a single item by id. Used for detail-refresh; UI falls back to cached menu data. */
    suspend fun getItem(id: Int): NetworkResult<MenuItem> = try {
        NetworkResult.Success(api.getItem(id).toDomain())
    } catch (e: IOException) {
        NetworkResult.Error("Network error: ${e.message}")
    } catch (e: HttpException) {
        NetworkResult.Error("Server error ${e.code()}: ${e.message()}")
    } catch (e: Exception) {
        NetworkResult.Error("Unexpected error: ${e.message}")
    }
}

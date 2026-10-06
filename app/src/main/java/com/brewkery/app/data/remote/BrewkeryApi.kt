package com.brewkery.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface BrewkeryApi {

    /**
     * Fetches the full menu and store configuration.
     * raw.githubusercontent.com returns text/plain, but the Gson converter
     * parses by body content rather than Content-Type, so no special adapter needed.
     */
    @GET("data.json")
    suspend fun getMenu(): MenuResponse

    /**
     * Fetches a single item by id (1..6).
     * The UI uses cached menu list data; this endpoint is wired for completeness
     * and can power a "refresh single item" use-case in future.
     */
    @GET("api/items/{id}.json")
    suspend fun getItem(@Path("id") id: Int): MenuItemDto
}

package com.brewkery.app

import com.brewkery.app.data.remote.BrewkeryApi
import com.brewkery.app.data.repository.CartRepository
import com.brewkery.app.data.repository.MenuRepository
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Manual dependency wiring — no DI framework.
 * Created once in BrewkeryApp and accessed via the Application context.
 */
class AppContainer {

    private val gson = GsonBuilder().serializeNulls().create()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            // Logging interceptor enabled only in debug builds to avoid leaking request bodies
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                )
            }
        }.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/VivekShah138/Brewkery/main/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: BrewkeryApi by lazy { retrofit.create(BrewkeryApi::class.java) }

    val menuRepository: MenuRepository by lazy { MenuRepository(api) }

    /** Singleton cart + order state shared across all screens. */
    val cartRepository: CartRepository by lazy { CartRepository() }
}

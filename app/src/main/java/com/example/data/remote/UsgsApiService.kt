package com.example.data.remote

import com.example.data.model.UsgsGeoJsonResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface UsgsApiService {
    @GET("fdsnws/event/1/query")
    suspend fun getRecentEarthquakes(
        @Query("format") format: String = "geojson",
        @Query("limit") limit: Int = 100,
        @Query("orderby") orderby: String = "time",
        @Query("minmagnitude") minMagnitude: Double? = 1.0
    ): UsgsGeoJsonResponse

    @GET("earthquakes/feed/v1.0/summary/all_day.geojson")
    suspend fun getSummaryAllDay(): UsgsGeoJsonResponse

    @GET("earthquakes/feed/v1.0/summary/all_week.geojson")
    suspend fun getSummaryAllWeek(): UsgsGeoJsonResponse

    companion object {
        private const val BASE_URL = "https://earthquake.usgs.gov/"

        fun create(): UsgsApiService {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(UsgsApiService::class.java)
        }
    }
}


package com.example.nutritiontracker.data.fdc

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit code was referenced from
// https://www.geeksforgeeks.org/kotlin/retrofit-with-kotlin-coroutine-in-android/ and
// https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet?authuser=6#7
object RetrofitHelper {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FoodDataApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: FoodDataApiService by lazy {
        retrofit.create(FoodDataApiService::class.java)
    }
}
package com.example.nutritiontracker.data.fdc


import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query



// The API structure was referenced the the FDC API Guide, https://fdc.nal.usda.gov/api-guide
interface FoodDataApiService {

    companion object {
        const val API_KEY = "CKFbXyrnFTUKuaKSpZBSqYaBFlAHUM0birgVFLkL"
        const val BASE_URL = "https://api.nal.usda.gov/fdc/v1/"
    }

    @GET("foods/search")
    suspend fun searchFoodByBarcode(
        @Query("query") barcode: String,
        @Query("api_key") apiKey: String = API_KEY,
        @Query("dataType") dataType: List<String> = listOf("Branded"),
        @Query("pageSize") pageSize: Int = 1
    ): BrandedFoodsFound

    @GET("food/{fdcId}")
    suspend fun getFoodDetails(
        @Path("fdcId") fdcId: Int,
        @Query("api_key") apiKey: String = API_KEY
    ): FdcIdDetails
}
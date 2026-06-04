package com.example.nutritiontracker.data.fdc

import android.util.Log

fun convertBarcodeStringToFDCFormat(rawBarcode: String?): String? {
    if (rawBarcode == null) return null
    return rawBarcode.filter { it.isDigit() }
}

class FDCHelper {

    //TODO: Update function to check for food category as drinks are measured by volume
    private fun scaleServingSize(
        details: FdcIdDetails,
        nutrientID: Int,
    ): Double?{

        val energyFact = details.foodNutrients?.find { nutrient ->
            nutrient.nutrient?.id == nutrientID
        }
        val servingSizeGrams = details.servingSize

        if (energyFact == null || servingSizeGrams == null || servingSizeGrams <= 0) {
            return null
        }

        val amountPer100G = energyFact.amount ?: return null

        return (amountPer100G / 100.0) * servingSizeGrams
    }

    private fun addNutritionFactsToSummary(details: FdcIdDetails): NutritionSummary {

        val scaledCalories = scaleServingSize(details, FdcNutrientIDs.ENERGY.id)
        val scaledProtein = scaleServingSize(details, FdcNutrientIDs.PROTEIN.id)
        val scaledTotalCarbs = scaleServingSize(details, FdcNutrientIDs.TOTAL_CARBS.id)
        val scaledTotalFat = scaleServingSize(details, FdcNutrientIDs.TOTAL_FAT.id)
        val scaledFiber = scaleServingSize(details, FdcNutrientIDs.FIBER.id)
        val scaledVitaminC = scaleServingSize(details, FdcNutrientIDs.VITAMIN_C.id)
        val scaledVitaminD = scaleServingSize(details, FdcNutrientIDs.VITAMIN_D.id)
        val scaledCalcium = scaleServingSize(details, FdcNutrientIDs.CALCIUM.id)
        val scaledIron = scaleServingSize(details, FdcNutrientIDs.IRON.id)


        return NutritionSummary(
            description = details.description ?: "Unknown Product",
            calories = scaledCalories,
            protein = scaledProtein,
            totalCarbs = scaledTotalCarbs,
            totalFat = scaledTotalFat,
            fiber = scaledFiber,
            vitaminC = scaledVitaminC,
            vitaminD = scaledVitaminD,
            calcium = scaledCalcium,
            iron = scaledIron
        )
    }

    private val apiService = RetrofitHelper.apiService

    suspend fun fetchFdcIdFromBarcode(barcode: String): FdcIdDetails{
        val searchFDC = apiService.searchFoodByBarcode(barcode)

        val fdcID = searchFDC.foods?.firstOrNull()?.fdcId
            ?: throw NoSuchElementException("No FdcId found for barcode: $barcode")

        return apiService.getFoodDetails(fdcID)
    }

    suspend fun getNutritionFactsFromBarcodeType(rawBarcode: String?): NutritionResults{
        val formattedBarcode = convertBarcodeStringToFDCFormat(rawBarcode)
            ?: throw IllegalArgumentException("Invalid barcode.")

        var details: FdcIdDetails? = null

        details = try{
            fetchFdcIdFromBarcode(formattedBarcode)
        } catch (e: NoSuchElementException){
            val leadingZeroBarcode = "00$formattedBarcode"

            try {
                fetchFdcIdFromBarcode(leadingZeroBarcode)
            } catch (e: Exception){
                throw NoSuchElementException("Could not find nutritional facts barcode format")
            }
        }
        val finalDetails = details
        val summary = addNutritionFactsToSummary(finalDetails)

        return NutritionResults(
            details = finalDetails,
            summary = summary
        )
    }
}
package com.example.nutritiontracker.data.fdc

data class BrandedFoodsFound(
    val foods: List<GetFdcIdFromBranded>?
)

data class GetFdcIdFromBranded(
    val fdcId: Int,
    val description: String?,
)

data class FdcIdDetails(
    val fdcId: Int,
    val description: String?,
    val foodNutrients: List<NutritionFacts>?,
    val servingSize: Double?,
    val servingSizeUnit: String?
)

data class NutritionFacts(
    val nutrient: NutritionFactLookup?,
    val amount: Double?,
    val unitName: String?
)

data class NutritionFactLookup(
    val id: Int,
    val name: String?
)

data class NutritionSummary(
    val description: String,
    val calories: Double?,
    val protein: Double?,
    val totalCarbs: Double?,
    val totalFat: Double?,
    val fiber: Double?,
    val vitaminC: Double?,
    val vitaminD: Double?,
    val calcium: Double?,
    val iron: Double?
)

data class NutritionResults(
    val details: FdcIdDetails,
    val summary: NutritionSummary
)

enum class FdcNutrientIDs(val id: Int){
    ENERGY(1008),
    PROTEIN(1003),
    TOTAL_CARBS(1005),
    TOTAL_FAT(1004),
    FIBER(1079),

    VITAMIN_C(1162),
    VITAMIN_D(1114),
    CALCIUM(1087),

    IRON(1089);
}
package com.example.nutritiontracker.utils

import com.example.nutritiontracker.data.RDIRequirements

object RDICalculator {

    fun calculateRDI(
        age: Int,
        gender: String,
        weight: Double = 70.0, // kg
        height: Double = 170.0, // cm
        activityLevel: String = "Moderately Active (3-5 days/week)"
    ): RDIRequirements {

        // Calculate BMR (Basal Metabolic Rate) using Mifflin-St Jeor Equation
        val bmr = if (gender.equals("Male", ignoreCase = true)) {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }

        // Apply activity multiplier
        val activityMultiplier = when {
            activityLevel.contains("Sedentary", ignoreCase = true) -> 1.2
            activityLevel.contains("Lightly Active", ignoreCase = true) -> 1.375
            activityLevel.contains("Moderately Active", ignoreCase = true) -> 1.55
            activityLevel.contains("Very Active", ignoreCase = true) -> 1.725
            activityLevel.contains("Extra Active", ignoreCase = true) -> 1.9
            else -> 1.55
        }

        val calories = (bmr * activityMultiplier).toInt()

        // Macronutrients
        val protein = getProteinRDA(age, gender, weight)
        val carbohydrates = 130.0 // RDA minimum in grams
        val fiber = getFiberRDA(age, gender)

        // Minerals
        val calcium = getCalciumRDA(age, gender)
        val iron = getIronRDA(age, gender)
        val magnesium = getMagnesiumRDA(age, gender)
        val phosphorus = getPhosphorusRDA(age, gender)
        val potassium = getPotassiumRDA(age, gender)
        val sodium = 2300 // UL (upper limit) in mg
        val zinc = getZincRDA(age, gender)

        // Vitamins
        val vitaminA = getVitaminARDA(age, gender)
        val vitaminC = getVitaminCRDA(age, gender)
        val vitaminD = getVitaminDRDA(age)
        val vitaminE = getVitaminERDA(age, gender)
        val vitaminK = getVitaminKRDA(age, gender)
        val thiamin = getThiaminRDA(age, gender)
        val riboflavin = getRiboflavinRDA(age, gender)
        val niacin = getNiacinRDA(age, gender)
        val vitaminB6 = getVitaminB6RDA(age, gender)
        val folate = getFolateRDA(age, gender)
        val vitaminB12 = getVitaminB12RDA(age)

        return RDIRequirements(
            calories = calories,
            protein = protein,
            carbohydrates = carbohydrates,
            fiber = fiber,
            calcium = calcium,
            iron = iron,
            magnesium = magnesium,
            phosphorus = phosphorus,
            potassium = potassium,
            sodium = sodium,
            zinc = zinc,
            vitaminA = vitaminA,
            vitaminC = vitaminC,
            vitaminD = vitaminD,
            vitaminE = vitaminE,
            vitaminK = vitaminK,
            thiamin = thiamin,
            riboflavin = riboflavin,
            niacin = niacin,
            vitaminB6 = vitaminB6,
            folate = folate,
            vitaminB12 = vitaminB12
        )
    }

    private fun getProteinRDA(age: Int, gender: String, weight: Double): Double {
        // RDA is 0.8g per kg body weight for adults
        return 0.8 * weight
    }

    private fun getFiberRDA(age: Int, gender: String): Double {
        return when {
            age <= 8 -> if (gender.equals("Male", ignoreCase = true)) 25.0 else 25.0
            age <= 13 -> if (gender.equals("Male", ignoreCase = true)) 31.0 else 26.0
            age <= 18 -> if (gender.equals("Male", ignoreCase = true)) 38.0 else 26.0
            age <= 50 -> if (gender.equals("Male", ignoreCase = true)) 38.0 else 25.0
            else -> if (gender.equals("Male", ignoreCase = true)) 30.0 else 21.0
        }
    }

    private fun getCalciumRDA(age: Int, gender: String): Int {
        return when {
            age <= 3 -> 700
            age <= 8 -> 1000
            age <= 18 -> 1300
            age <= 50 -> 1000
            age <= 70 && gender.equals("Male", ignoreCase = true) -> 1000
            age <= 70 -> 1200
            else -> 1200
        }
    }

    private fun getIronRDA(age: Int, gender: String): Double {
        return when {
            age <= 3 -> 7.0
            age <= 8 -> 10.0
            age <= 13 -> 8.0
            age <= 18 && gender.equals("Male", ignoreCase = true) -> 11.0
            age <= 18 -> 15.0
            age <= 50 && gender.equals("Male", ignoreCase = true) -> 8.0
            age <= 50 -> 18.0
            else -> 8.0
        }
    }

    private fun getMagnesiumRDA(age: Int, gender: String): Int {
        return when {
            age <= 3 -> 80
            age <= 8 -> 130
            age <= 13 -> if (gender.equals("Male", ignoreCase = true)) 240 else 240
            age <= 18 -> if (gender.equals("Male", ignoreCase = true)) 410 else 360
            age <= 30 -> if (gender.equals("Male", ignoreCase = true)) 400 else 310
            else -> if (gender.equals("Male", ignoreCase = true)) 420 else 320
        }
    }

    private fun getPhosphorusRDA(age: Int, gender: String): Int {
        return when {
            age <= 3 -> 460
            age <= 8 -> 500
            age <= 18 -> 1250
            else -> 700
        }
    }

    private fun getPotassiumRDA(age: Int, gender: String): Int {
        return when {
            age <= 3 -> 3000
            age <= 8 -> 3800
            age <= 13 -> 4500
            age <= 18 -> if (gender.equals("Male", ignoreCase = true)) 3000 else 2300
            else -> if (gender.equals("Male", ignoreCase = true)) 3400 else 2600
        }
    }

    private fun getZincRDA(age: Int, gender: String): Double {
        return when {
            age <= 3 -> 3.0
            age <= 8 -> 5.0
            age <= 13 -> 8.0
            age <= 18 && gender.equals("Male", ignoreCase = true) -> 11.0
            age <= 18 -> 9.0
            else -> if (gender.equals("Male", ignoreCase = true)) 11.0 else 8.0
        }
    }

    private fun getVitaminARDA(age: Int, gender: String): Int {
        // In mcg RAE
        return when {
            age <= 3 -> 300
            age <= 8 -> 400
            age <= 13 -> 600
            age <= 18 && gender.equals("Male", ignoreCase = true) -> 900
            age <= 18 -> 700
            else -> if (gender.equals("Male", ignoreCase = true)) 900 else 700
        }
    }

    private fun getVitaminCRDA(age: Int, gender: String): Int {
        // In mg
        return when {
            age <= 3 -> 15
            age <= 8 -> 25
            age <= 13 -> 45
            age <= 18 && gender.equals("Male", ignoreCase = true) -> 75
            age <= 18 -> 65
            else -> if (gender.equals("Male", ignoreCase = true)) 90 else 75
        }
    }

    private fun getVitaminDRDA(age: Int): Int {
        // In mcg (IU)
        return when {
            age <= 70 -> 15
            else -> 20
        }
    }

    private fun getVitaminERDA(age: Int, gender: String): Double {
        // In mg
        return when {
            age <= 3 -> 6.0
            age <= 8 -> 7.0
            age <= 13 -> 11.0
            else -> 15.0
        }
    }

    private fun getVitaminKRDA(age: Int, gender: String): Int {
        // In mcg
        return when {
            age <= 3 -> 30
            age <= 8 -> 55
            age <= 13 -> 60
            age <= 18 && gender.equals("Male", ignoreCase = true) -> 75
            age <= 18 -> 75
            else -> if (gender.equals("Male", ignoreCase = true)) 120 else 90
        }
    }

    private fun getThiaminRDA(age: Int, gender: String): Double {
        // In mg
        return when {
            age <= 3 -> 0.5
            age <= 8 -> 0.6
            age <= 13 -> 0.9
            age <= 18 && gender.equals("Male", ignoreCase = true) -> 1.2
            age <= 18 -> 1.0
            else -> if (gender.equals("Male", ignoreCase = true)) 1.2 else 1.1
        }
    }

    private fun getRiboflavinRDA(age: Int, gender: String): Double {
        // In mg
        return when {
            age <= 3 -> 0.5
            age <= 8 -> 0.6
            age <= 13 -> 0.9
            age <= 18 && gender.equals("Male", ignoreCase = true) -> 1.3
            age <= 18 -> 1.0
            else -> if (gender.equals("Male", ignoreCase = true)) 1.3 else 1.1
        }
    }

    private fun getNiacinRDA(age: Int, gender: String): Double {
        // In mg NE
        return when {
            age <= 3 -> 6.0
            age <= 8 -> 8.0
            age <= 13 -> 12.0
            age <= 18 && gender.equals("Male", ignoreCase = true) -> 16.0
            age <= 18 -> 14.0
            else -> if (gender.equals("Male", ignoreCase = true)) 16.0 else 14.0
        }
    }

    private fun getVitaminB6RDA(age: Int, gender: String): Double {
        // In mg
        return when {
            age <= 3 -> 0.5
            age <= 8 -> 0.6
            age <= 13 -> 1.0
            age <= 18 -> 1.3
            age <= 50 -> 1.3
            else -> if (gender.equals("Male", ignoreCase = true)) 1.7 else 1.5
        }
    }

    private fun getFolateRDA(age: Int, gender: String): Int {
        // In mcg DFE
        return when {
            age <= 3 -> 150
            age <= 8 -> 200
            age <= 13 -> 300
            else -> 400
        }
    }

    private fun getVitaminB12RDA(age: Int): Double {
        // In mcg
        return when {
            age <= 3 -> 0.9
            age <= 8 -> 1.2
            age <= 13 -> 1.8
            else -> 2.4
        }
    }
}

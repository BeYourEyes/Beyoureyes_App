package com.dna.beyoureyes

import android.util.Log

/*
2. OPENAPI 싱글톤 객체 
*/

object OpenAPIProcessor {
    data class NutrientResult(
        val prot: Int,
        val fatce: Int,
        val chocdf: Int,
        val sugar: Int,
        val nat: Int,
        val chole: Int,
        val fasat: Int
    )

    suspend fun fetchNutrientData(
        pageNo: String,
        numOfRows: String,
        type: String,
        itemMnftrRptNo: String
    ): List<NutrientResult>? {
        return try {
            val response = RetrofitClient.apiService.getFood(
                pageNo = pageNo,
                numOfRows = numOfRows,
                type = type,
                itemMnftrRptNp = itemMnftrRptNo
            )
            val items = response.response.body.items
            calculateNutrients(items)
        } catch (e: Exception) {
            Log.e("OpenAPIProcessor", "API call failed: ${e.message}")
            null
        }
    }

    private fun calculateNutrients(items: List<ApiResponse.Item>): List<NutrientResult> {
        return items.map { item ->
            val foodSize = item.foodSize?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 1.0
            NutrientResult(
                prot = ((item.prot.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt(),
                fatce = ((item.fatce.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt(),
                chocdf = ((item.chocdf.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt(),
                sugar = ((item.sugar.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt(),
                nat = ((item.nat.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt(),
                chole = ((item.chole.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt(),
                fasat = ((item.fasat.toDoubleOrNull() ?: 0.0) / 100.0 * foodSize).toInt()
            )
        }
    }



}
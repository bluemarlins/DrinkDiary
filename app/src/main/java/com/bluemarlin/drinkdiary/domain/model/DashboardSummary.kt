package com.bluemarlin.drinkdiary.domain.model

data class DashboardSummary(
    val totalCount: Int,
    val averageRating: Double?,
    val wineCount: Int,
    val whiskeyCount: Int,
    val beerCount: Int,
    val wineAverageRating: Double?,
    val whiskeyAverageRating: Double?,
    val beerAverageRating: Double?,
    val repurchaseCount: Int,
    val notForMeCount: Int,
    val repurchaseRecords: List<DrinkRecord>,
    val notForMeRecords: List<DrinkRecord>,
) {
    companion object {
        val Empty = DashboardSummary(
            totalCount = 0,
            averageRating = null,
            wineCount = 0,
            whiskeyCount = 0,
            beerCount = 0,
            wineAverageRating = null,
            whiskeyAverageRating = null,
            beerAverageRating = null,
            repurchaseCount = 0,
            notForMeCount = 0,
            repurchaseRecords = emptyList(),
            notForMeRecords = emptyList(),
        )
    }
}

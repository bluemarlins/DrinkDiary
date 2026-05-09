package com.bluemarlin.drinkdiary.domain.model

data class DashboardSummary(
    val totalCount: Int,
    val averageRating: Double?,
    val wineCount: Int,
    val whiskeyCount: Int,
    val beerCount: Int,
    val repurchaseCount: Int,
    val notForMeCount: Int,
    val totalSpent: Long,
    val averageSpent: Long?,
    val pricedRecordCount: Int,
    val normalRecords: List<DrinkRecord>,
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
            repurchaseCount = 0,
            notForMeCount = 0,
            totalSpent = 0L,
            averageSpent = null,
            pricedRecordCount = 0,
            normalRecords = emptyList(),
            repurchaseRecords = emptyList(),
            notForMeRecords = emptyList(),
        )
    }
}

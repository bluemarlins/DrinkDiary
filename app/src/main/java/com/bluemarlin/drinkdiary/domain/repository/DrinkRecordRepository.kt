package com.bluemarlin.drinkdiary.domain.repository

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import kotlinx.coroutines.flow.Flow

interface DrinkRecordRepository {
    fun observeRecords(filter: DrinkRecordFilter): Flow<List<DrinkRecord>>
    fun observeRecord(id: Long): Flow<DrinkRecord?>
    fun observeRecordsByPeriod(startMillis: Long, endMillis: Long): Flow<List<DrinkRecord>>
    fun observeSearchResults(query: String): Flow<List<DrinkRecord>>
    suspend fun save(record: DrinkRecord): AppResult<Long>
    suspend fun deleteById(id: Long): AppResult<Unit>
}

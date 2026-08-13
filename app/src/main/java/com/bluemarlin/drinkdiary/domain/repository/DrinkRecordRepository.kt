package com.bluemarlin.drinkdiary.domain.repository

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import kotlinx.coroutines.flow.Flow

interface DrinkRecordRepository {
    fun observeRecords(type: DrinkType? = null): Flow<List<DrinkRecord>>

    fun observeRecord(id: Long): Flow<DrinkRecord?>

    fun observeSearchResults(query: String): Flow<List<DrinkRecord>>

    suspend fun save(record: DrinkRecord): AppResult<Long>

    suspend fun deleteById(id: Long): AppResult<Unit>
}

package com.bluemarlin.drinkdiary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkRecordDao {
    @Query(
        """
        SELECT * FROM drink_records
        WHERE (:type IS NULL OR type = :type)
          AND (:collectionStatus IS NULL OR collectionStatus = :collectionStatus)
        ORDER BY recordedAtMillis DESC
        """,
    )
    fun observeRecords(
        type: String?,
        collectionStatus: String?,
    ): Flow<List<DrinkRecordEntity>>

    @Query("SELECT * FROM drink_records WHERE id = :id")
    fun observeRecord(id: Long): Flow<DrinkRecordEntity?>

    @Query("SELECT COUNT(*) FROM drink_records")
    fun observeRecordsCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM drink_records
        WHERE recordedAtMillis BETWEEN :startMillis AND :endMillis
        ORDER BY recordedAtMillis DESC
        """,
    )
    fun observeRecordsByPeriod(
        startMillis: Long,
        endMillis: Long,
    ): Flow<List<DrinkRecordEntity>>

    @Query(
        """
        SELECT * FROM drink_records
        WHERE LOWER(name) LIKE '%' || :query || '%' ESCAPE '\'
           OR LOWER(COALESCE(place, '')) LIKE '%' || :query || '%' ESCAPE '\'
           OR LOWER(COALESCE(tastingNote, '')) LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY recordedAtMillis DESC
        """,
    )
    fun observeSearchResults(query: String): Flow<List<DrinkRecordEntity>>

    @Query("SELECT * FROM drink_records WHERE id = :id")
    suspend fun getRecord(id: Long): DrinkRecordEntity?

    @Insert
    suspend fun insert(record: DrinkRecordEntity): Long

    @Update
    suspend fun update(record: DrinkRecordEntity): Int

    @Query("DELETE FROM drink_records WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}

package com.bluemarlin.drinkdiary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkRecordDao {
    @Transaction
    @Query("SELECT * FROM drink_records ORDER BY recordedAtMillis DESC")
    fun observeAll(): Flow<List<DrinkRecordWithAnswers>>

    @Transaction
    @Query("SELECT * FROM drink_records WHERE type = :type ORDER BY recordedAtMillis DESC")
    fun observeByType(type: String): Flow<List<DrinkRecordWithAnswers>>

    @Transaction
    @Query("SELECT * FROM drink_records WHERE id = :id")
    fun observeById(id: Long): Flow<DrinkRecordWithAnswers?>

    @Transaction
    @Query("SELECT * FROM drink_records WHERE name LIKE '%' || :query || '%' ORDER BY recordedAtMillis DESC")
    fun observeBySearch(query: String): Flow<List<DrinkRecordWithAnswers>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(entity: DrinkRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<TraitAnswerEntity>)

    @Query("DELETE FROM trait_answers WHERE recordId = :recordId")
    suspend fun deleteAnswers(recordId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<RecordTagEntity>)

    @Query("DELETE FROM record_tags WHERE recordId = :recordId")
    suspend fun deleteTags(recordId: Long)

    @Query("DELETE FROM drink_records WHERE id = :id")
    suspend fun deleteRecord(id: Long): Int

    // 답과 태그를 지우고 다시 넣는다. 수정 시 옛 값이 남아 쌓이면 판정이 오염된다.
    @Transaction
    suspend fun saveWithAnswers(
        entity: DrinkRecordEntity,
        answers: (Long) -> List<TraitAnswerEntity>,
        tags: (Long) -> List<RecordTagEntity> = { emptyList() },
    ): Long {
        val id = upsertRecord(entity)
        val target = if (entity.id == 0L) id else entity.id
        deleteAnswers(target)
        insertAnswers(answers(target))
        deleteTags(target)
        insertTags(tags(target))
        return target
    }
}

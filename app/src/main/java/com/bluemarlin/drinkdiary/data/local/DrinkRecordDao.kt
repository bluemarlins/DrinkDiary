package com.bluemarlin.drinkdiary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
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

    // REPLACE가 아니라 UPSERT다. REPLACE는 **기존 행을 지우고 새로 넣기** 때문에
    // ON DELETE CASCADE가 걸린 답·태그가 그 순간 같이 날아간다. 지금은 바로 뒤에서 다시 넣어
    // 결과가 같지만, 그건 우연이다 — 편집 화면이 붙으면 이 경로가 가장 자주 도는 길이 된다.
    // UPSERT는 있으면 UPDATE라 자식 행을 건드리지 않는다.
    @Upsert
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
    //
    // **`upsertRecord`의 반환값은 새로 넣었을 때만 쓸 수 있다.** UPDATE 경로에서는 Room이 `-1`을
    // 돌려주므로, 그 값을 그대로 recordId로 쓰면 답·태그가 존재하지 않는 기록에 붙는다(외래키가
    // 걸려 있어 실제로는 저장이 통째로 실패한다). 편집은 id를 이미 알고 있으니 그걸 쓴다.
    @Transaction
    suspend fun saveWithAnswers(
        entity: DrinkRecordEntity,
        answers: (Long) -> List<TraitAnswerEntity>,
        tags: (Long) -> List<RecordTagEntity> = { emptyList() },
    ): Long {
        val insertedId = upsertRecord(entity)
        val target = if (entity.id == 0L) insertedId else entity.id
        deleteAnswers(target)
        insertAnswers(answers(target))
        deleteTags(target)
        insertTags(tags(target))
        return target
    }
}

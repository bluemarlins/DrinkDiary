package com.bluemarlin.drinkdiary.data.repository

import com.bluemarlin.drinkdiary.data.local.DrinkRecordDao
import com.bluemarlin.drinkdiary.data.mapper.toAnswerEntities
import com.bluemarlin.drinkdiary.data.mapper.toDomain
import com.bluemarlin.drinkdiary.data.mapper.toEntity
import com.bluemarlin.drinkdiary.data.mapper.toTagEntities
import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DrinkRecordRepositoryImpl(
    private val dao: DrinkRecordDao,
) : DrinkRecordRepository {
    override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> {
        val flow =
            if (type == null) {
                dao.observeAll()
            } else {
                dao.observeByType(type.name)
            }
        return flow.map { list -> list.mapNotNull { it.toDomain() } }
    }

    override fun observeRecord(id: Long): Flow<DrinkRecord?> = dao.observeById(id).map { it?.toDomain() }

    override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> =
        dao.observeBySearch(query).map { list ->
            list.mapNotNull {
                it.toDomain()
            }
        }

    override suspend fun save(record: DrinkRecord): AppResult<Long> =
        runCatching {
            dao.saveWithAnswers(
                entity = record.toEntity(),
                answers = { recordId -> record.toAnswerEntities(recordId) },
                tags = { recordId -> record.toTagEntities(recordId) },
            )
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Failure(AppError.Storage) },
        )

    override suspend fun deleteById(id: Long): AppResult<Unit> =
        runCatching {
            val deletedCount = dao.deleteRecord(id)
            if (deletedCount == 0) {
                AppResult.Failure(AppError.NotFound)
            } else {
                AppResult.Success(Unit)
            }
        }.getOrElse { AppResult.Failure(AppError.Storage) }

    override suspend fun deleteByIds(ids: Set<Long>): AppResult<Int> {
        if (ids.isEmpty()) return AppResult.Success(0)

        return runCatching {
            val deletedCount = dao.deleteRecords(ids)
            if (deletedCount == 0) {
                AppResult.Failure(AppError.NotFound)
            } else {
                // 요청 건수보다 적게 지워진 것은 실패가 아니다. 다른 경로에서 이미 지워진 기록이
                // 섞여 있을 수 있고, 사용자가 원한 최종 상태(그 기록들이 없는 상태)는 달성됐다.
                AppResult.Success(deletedCount)
            }
        }.getOrElse { AppResult.Failure(AppError.Storage) }
    }
}

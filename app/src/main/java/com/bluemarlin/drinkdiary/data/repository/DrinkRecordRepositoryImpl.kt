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
}

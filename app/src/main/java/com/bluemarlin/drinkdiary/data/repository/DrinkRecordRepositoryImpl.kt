package com.bluemarlin.drinkdiary.data.repository

import com.bluemarlin.drinkdiary.data.local.DrinkRecordDao
import com.bluemarlin.drinkdiary.data.mapper.toDomain
import com.bluemarlin.drinkdiary.data.mapper.toEntity
import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkRecordFilter
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DrinkRecordRepositoryImpl(
    private val dao: DrinkRecordDao,
) : DrinkRecordRepository {
    override fun observeRecords(filter: DrinkRecordFilter): Flow<List<DrinkRecord>> =
        dao
            .observeRecords(
                type = filter.drinkType?.name,
                collectionStatus = filter.collectionStatus?.name,
            ).map { records -> records.mapNotNull { it.toDomain() } }

    override fun observeRecord(id: Long): Flow<DrinkRecord?> = dao.observeRecord(id).map { it?.toDomain() }

    override fun observeRecordsByPeriod(
        startMillis: Long,
        endMillis: Long,
    ): Flow<List<DrinkRecord>> =
        dao.observeRecordsByPeriod(startMillis, endMillis).map { records ->
            records.mapNotNull { it.toDomain() }
        }

    override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> =
        dao.observeSearchResults(query.toSearchPattern()).map { records ->
            records.mapNotNull { it.toDomain() }
        }

    override fun observeRecordsCount(): Flow<Int> = dao.observeRecordsCount()

    override suspend fun save(record: DrinkRecord): AppResult<Long> =
        runCatching {
            val now = System.currentTimeMillis()
            if (record.id == 0L) {
                dao.insert(record.toEntity(createdAtMillis = now, updatedAtMillis = now))
            } else {
                val current = dao.getRecord(record.id) ?: return AppResult.Failure(AppError.NotFound)
                val updated =
                    record.toEntity(
                        createdAtMillis = current.createdAtMillis,
                        updatedAtMillis = now,
                    )
                if (dao.update(updated) == 0) {
                    return AppResult.Failure(AppError.NotFound)
                }
                record.id
            }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Failure(AppError.Storage) },
        )

    override suspend fun deleteById(id: Long): AppResult<Unit> =
        runCatching {
            if (dao.deleteById(id) == 0) AppResult.Failure(AppError.NotFound) else AppResult.Success(Unit)
        }.getOrElse {
            AppResult.Failure(AppError.Storage)
        }

    private fun String.toSearchPattern(): String =
        trim()
            .lowercase()
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
}

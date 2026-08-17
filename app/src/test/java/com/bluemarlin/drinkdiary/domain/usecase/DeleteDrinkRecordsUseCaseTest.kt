package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteDrinkRecordsUseCaseTest {
    private fun record(
        id: Long,
        photo: String? = null,
    ) = DrinkRecord(
        id = id,
        type = DrinkType.Whiskey,
        name = "record$id",
        rating = 3.0,
        imageUri = photo,
        recordedAtMillis = id,
    )

    private val stored =
        listOf(
            record(1, "file:///photos/one.jpg"),
            record(2, "file:///photos/two.jpg"),
            record(3),
        )

    private fun useCase(
        records: FakeRecords = FakeRecords(stored),
        photos: FakePhotos = FakePhotos(),
    ) = Triple(DeleteDrinkRecordsUseCase(records, photos), records, photos)

    @Test
    fun `deleting a record deletes its photo`() {
        val (useCase, records, photos) = useCase()

        runBlocking { useCase(1L) }

        assertEquals(setOf(1L), records.deleted)
        assertEquals(listOf("file:///photos/one.jpg"), photos.deleted)
    }

    @Test
    fun `deleting several records deletes each photo`() {
        val (useCase, _, photos) = useCase()

        runBlocking { useCase(setOf(1L, 2L)) }

        assertEquals(setOf("file:///photos/one.jpg", "file:///photos/two.jpg"), photos.deleted.toSet())
    }

    @Test
    fun `a record without a photo deletes nothing extra`() {
        val (useCase, _, photos) = useCase()

        runBlocking { useCase(3L) }

        assertTrue(photos.deleted.isEmpty())
    }

    // 순서를 뒤집으면 행 삭제가 실패했을 때 아직 살아 있는 기록의 사진만 사라진다.
    // 되돌리기가 없는 기능이라 사용자가 복구할 방법이 없다.
    @Test
    fun `a failed row delete leaves the photo alone`() {
        val (useCase, _, photos) = useCase(records = FakeRecords(stored, fail = true))

        val result = runBlocking { useCase(1L) }

        assertTrue(result is AppResult.Failure)
        assertTrue("행이 안 지워졌는데 사진이 사라졌다", photos.deleted.isEmpty())
    }

    // 사용자가 요청한 것은 기록을 없애는 것이고 그것은 이루어졌다. 남는 것은 아무도
    // 참조하지 않는 파일 하나뿐이라, 여기서 실패를 돌려주면 이미 지워진 기록을 두고
    // "지우지 못했어요"라고 말하게 된다.
    @Test
    fun `a failed photo cleanup does not fail the deletion`() {
        val (useCase, _, _) = useCase(photos = FakePhotos(fail = true))

        assertTrue(runBlocking { useCase(1L) } is AppResult.Success)
    }

    // 행이 사라지면 그 기록이 무슨 사진을 가리켰는지도 함께 사라진다.
    @Test
    fun `the photo uris are read before the rows go`() {
        val records = FakeRecords(stored)
        val (useCase, _, photos) = useCase(records = records)

        runBlocking { useCase(setOf(1L)) }

        assertTrue("행을 지운 뒤에 조회했다", records.readBeforeDelete)
        assertEquals(listOf("file:///photos/one.jpg"), photos.deleted)
    }

    @Test
    fun `deleting nothing touches nothing`() {
        val (useCase, records, photos) = useCase()

        runBlocking { useCase(emptySet()) }

        assertTrue(records.deleted.isEmpty())
        assertTrue(photos.deleted.isEmpty())
    }

    private class FakeRecords(
        private val records: List<DrinkRecord>,
        private val fail: Boolean = false,
    ) : DrinkRecordRepository {
        var deleted: Set<Long> = emptySet()
            private set
        var readBeforeDelete = false
            private set

        override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> {
            if (deleted.isEmpty()) readBeforeDelete = true
            return flowOf(records)
        }

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(records.find { it.id == id })

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(record.id)

        override suspend fun deleteById(id: Long): AppResult<Unit> {
            if (fail) return AppResult.Failure(AppError.Storage)
            deleted = deleted + id
            return AppResult.Success(Unit)
        }

        override suspend fun deleteByIds(ids: Set<Long>): AppResult<Int> {
            if (fail) return AppResult.Failure(AppError.Storage)
            deleted = deleted + ids
            return AppResult.Success(ids.size)
        }
    }

    private class FakePhotos(
        private val fail: Boolean = false,
    ) : PhotoRepository {
        var deleted: List<String> = emptyList()
            private set

        override suspend fun import(sourceUri: String): AppResult<String> = AppResult.Success(sourceUri)

        override suspend fun delete(uri: String): AppResult<Unit> {
            if (fail) return AppResult.Failure(AppError.Storage)
            deleted = deleted + uri
            return AppResult.Success(Unit)
        }
    }
}

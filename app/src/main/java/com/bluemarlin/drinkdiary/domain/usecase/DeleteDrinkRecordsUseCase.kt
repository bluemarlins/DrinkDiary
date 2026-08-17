package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.first

// 기록을 지우면 그 사진도 함께 지운다.
//
// **행을 먼저 지우고 파일을 나중에 지운다.** 순서를 뒤집으면 행 삭제가 실패했을 때 아직
// 살아 있는 기록의 사진만 사라진다 — 되돌리기가 없는 기능이라(prd.md F1-2) 그 상태를
// 사용자가 복구할 방법이 없다.
//
// **사진 URI는 행을 지우기 전에 읽어 둔다.** 답·태그가 `ON DELETE CASCADE`로 함께 사라지듯
// 행이 사라지면 그 기록이 무슨 사진을 가리켰는지 알 방법도 함께 사라진다.
//
// **사진 정리 실패는 삭제 실패가 아니다.** 사용자가 요청한 것은 기록을 없애는 것이고 그것은
// 이루어졌다. 남는 것은 아무도 참조하지 않는 파일 하나뿐이라 화면에 보이지도 않는다 —
// 여기서 실패를 돌려주면 이미 지워진 기록을 두고 "지우지 못했어요"라고 말하게 된다.
class DeleteDrinkRecordsUseCase(
    private val records: DrinkRecordRepository,
    private val photos: PhotoRepository,
) {
    suspend operator fun invoke(id: Long): AppResult<Unit> {
        val doomed = photoUris(setOf(id))
        return records.deleteById(id).also { result ->
            if (result is AppResult.Success) discard(doomed)
        }
    }

    suspend operator fun invoke(ids: Set<Long>): AppResult<Int> {
        if (ids.isEmpty()) return records.deleteByIds(ids)
        val doomed = photoUris(ids)
        return records.deleteByIds(ids).also { result ->
            if (result is AppResult.Success) discard(doomed)
        }
    }

    private suspend fun photoUris(ids: Set<Long>): List<String> =
        records
            .observeRecords()
            .first()
            .filter { it.id in ids }
            .mapNotNull { it.imageUri }

    private suspend fun discard(uris: List<String>) = uris.forEach { photos.delete(it) }
}

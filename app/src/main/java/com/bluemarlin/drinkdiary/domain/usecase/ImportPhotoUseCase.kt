package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.repository.PhotoRepository

// 사진을 고른 직후 부른다. **저장 시점이 아니다** — 마법사를 저장까지 끌고 가는 동안
// 프로세스가 죽으면 그 URI는 이미 못 읽는 것이 되어 있다.
class ImportPhotoUseCase(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(sourceUri: String): AppResult<String> = repository.import(sourceUri)
}

package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.repository.PhotoRepository

// 아무도 참조하지 않게 된 사진 하나를 치운다.
//
// 기록 삭제 말고도 사진이 버려지는 길이 둘 있다. **사진을 고르고 다시 고르는 경우**(앞의 것은
// 저장된 적이 없다), 그리고 **편집에서 사진을 바꿔 저장하는 경우**(원본이 참조를 잃는다).
// 둘 다 화면에는 보이지 않아서, 치우지 않으면 저장소만 조용히 늘어난다.
class DeletePhotoUseCase(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(uri: String): AppResult<Unit> = repository.delete(uri)
}

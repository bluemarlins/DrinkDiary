package com.bluemarlin.drinkdiary.data.repository

import android.content.Context
import android.net.Uri
import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

// 원본을 앱 전용 디렉터리로 복사하고 그 경로를 돌려준다.
//
// 복사본을 두는 이유는 권한만이 아니다. 사용자가 갤러리에서 원본을 지워도 기록은 남아야 하고,
// 로컬 우선 앱에서 남의 저장소를 가리키는 참조는 우리가 통제할 수 없는 의존이다.
//
// 저장 형식은 `file://` URI다. 화면은 이미 URI 문자열을 받도록 돼 있어서 표시 경로가 갈리지 않는다.
class PhotoRepositoryImpl(
    private val context: Context,
    private val directory: File = File(context.filesDir, "photos"),
) : PhotoRepository {
    override suspend fun import(sourceUri: String): AppResult<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                directory.mkdirs()
                val target = File(directory, "${UUID.randomUUID()}.jpg")
                val opened =
                    context.contentResolver.openInputStream(Uri.parse(sourceUri))
                        ?: error("사진을 열 수 없다: $sourceUri")
                opened.use { input -> target.outputStream().use(input::copyTo) }
                Uri.fromFile(target).toString()
            }.fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { AppResult.Failure(AppError.Storage) },
            )
        }
}

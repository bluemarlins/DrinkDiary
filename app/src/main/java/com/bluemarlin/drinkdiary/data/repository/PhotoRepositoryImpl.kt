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

    override suspend fun delete(uri: String): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val target = ownedFile(uri) ?: return@runCatching Unit
                // 이미 없는 파일은 지울 것이 없는 것이지 실패가 아니다.
                if (target.exists() && !target.delete()) error("사진을 지우지 못했다: $uri")
                Unit
            }.fold(
                onSuccess = { AppResult.Success(Unit) },
                onFailure = { AppResult.Failure(AppError.Storage) },
            )
        }

    // 우리 디렉터리 안의 `file://`만 우리 것이다. 경로를 정규화한 뒤에 비교하는 이유는
    // `..`가 섞인 경로가 디렉터리를 빠져나갈 수 있기 때문이다.
    private fun ownedFile(uri: String): File? {
        val parsed = Uri.parse(uri)
        if (parsed.scheme != "file") return null
        val path = parsed.path ?: return null
        val file = File(path).canonicalFile
        val root = directory.canonicalFile
        return if (file.parentFile == root) file else null
    }
}

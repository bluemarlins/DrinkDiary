package com.bluemarlin.drinkdiary.data

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.bluemarlin.drinkdiary.data.repository.PhotoRepositoryImpl
import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PhotoRepositoryTest {
    private lateinit var context: Context
    private lateinit var repository: PhotoRepositoryImpl
    private lateinit var source: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = PhotoRepositoryImpl(context)
        source = File.createTempFile("picked", ".jpg").apply { writeBytes(byteArrayOf(1, 2, 3, 4, 5)) }
    }

    private fun import(uri: String) = runBlocking { repository.import(uri) }

    private fun succeed(result: AppResult<String>): String {
        assertTrue("실패했다: $result", result is AppResult.Success)
        return (result as AppResult.Success).value
    }

    // 이것이 이 클래스의 존재 이유다. 갤러리 URI를 그대로 저장하면 프로세스가 죽는 순간
    // 못 읽는 참조가 되고, 사진 자리는 조용히 빈다(prd.md F1-3의 WARNING).
    @Test
    fun `the imported photo lives inside the app, not in the gallery`() {
        val stored = succeed(import(Uri.fromFile(source).toString()))
        val file = File(Uri.parse(stored).path!!)

        assertTrue("$stored 가 앱 저장소 밖이다", file.canonicalPath.startsWith(context.filesDir.canonicalPath))
        assertTrue(file.exists())
    }

    @Test
    fun `the copy keeps the original bytes`() {
        val stored = succeed(import(Uri.fromFile(source).toString()))

        assertEquals(
            source.readBytes().toList(),
            File(Uri.parse(stored).path!!).readBytes().toList(),
        )
    }

    // 원본이 갤러리에서 지워져도 기록은 남아야 한다.
    @Test
    fun `deleting the original leaves the record's photo intact`() {
        val stored = succeed(import(Uri.fromFile(source).toString()))
        assertTrue(source.delete())

        assertTrue(File(Uri.parse(stored).path!!).exists())
    }

    // 같은 사진을 두 번 골라도 서로 덮어쓰지 않는다.
    @Test
    fun `two imports never collide`() {
        val first = succeed(import(Uri.fromFile(source).toString()))
        val second = succeed(import(Uri.fromFile(source).toString()))

        assertNotEquals(first, second)
        assertTrue(File(Uri.parse(first).path!!).exists())
        assertTrue(File(Uri.parse(second).path!!).exists())
    }

    // 읽지 못하면 실패를 돌려준다. 빈 문자열이나 원본 URI를 대신 돌려주면
    // 화면은 사진이 붙은 줄 알고 넘어간다(harness.md §7).
    @Test
    fun `an unreadable source fails instead of pretending`() {
        val result = import("file:///data/없는파일.jpg")

        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.Storage, (result as AppResult.Failure).error)
    }
}

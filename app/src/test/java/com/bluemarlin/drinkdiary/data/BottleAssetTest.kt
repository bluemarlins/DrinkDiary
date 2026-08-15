package com.bluemarlin.drinkdiary.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.bluemarlin.drinkdiary.data.local.AssetBottleDictionary
import com.bluemarlin.drinkdiary.domain.model.CaskGroup
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.WineStyle
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 실제로 배포되는 assets/bottles.json 을 읽는다. 코드가 아니라 **데이터**를 지키는 테스트다 —
// 사전은 사람이 손으로 고칠 파일이고, 오타 하나가 조용히 항목을 삭제한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BottleAssetTest {
    private val assets = AssetBottleDictionary(ApplicationProvider.getApplicationContext<Context>())
    private val matcher = BottleMatcher { assets.entries }

    @Test
    fun `the shipped dictionary loads`() {
        // 파싱이 실패하면 조용히 빈 목록이 된다. 그러면 사전이 통째로 사라져도 아무도 모른다.
        assertTrue("사전이 비었다 — JSON 파싱 실패 가능성", assets.entries.size > 50)
    }

    @Test
    fun `every entry knows at least one fact`() {
        assets.entries.forEach { entry ->
            val known = entry.facts.cask != null || entry.facts.wineStyle != null
            assertTrue("${entry.displayName}: 아는 것이 없는 항목", known)
        }
    }

    // 주종과 사실의 종류가 어긋나면 판정에서 조용히 빠진다.
    @Test
    fun `facts match the drink type`() {
        assets.entries.forEach { entry ->
            when (entry.type) {
                DrinkType.Whiskey -> {
                    assertTrue("${entry.displayName}: 위스키인데 캐스크가 없다", entry.facts.cask != null)
                    assertNull("${entry.displayName}: 위스키에 와인 스타일", entry.facts.wineStyle)
                }

                DrinkType.Wine -> {
                    assertTrue("${entry.displayName}: 와인인데 스타일이 없다", entry.facts.wineStyle != null)
                    assertNull("${entry.displayName}: 와인에 캐스크", entry.facts.cask)
                }
            }
        }
    }

    @Test
    fun `no two entries claim the same name`() {
        val names = assets.entries.map { it.displayName }
        assertEquals(names.size, names.toSet().size)
    }

    // 검증에서 확인한 사실 몇 가지를 고정한다. 사전을 고치다 뒤집히면 여기서 잡힌다.
    @Test
    fun `known bottles resolve to the verified answer`() {
        assertEquals(CaskGroup.Sherry, matcher.lookup(DrinkType.Whiskey, "글렌드로낙 12")?.cask)
        assertEquals(CaskGroup.Bourbon, matcher.lookup(DrinkType.Whiskey, "아드벡 10")?.cask)
        assertEquals(CaskGroup.Mixed, matcher.lookup(DrinkType.Whiskey, "발베니 12 더블우드")?.cask)
        assertEquals(WineStyle.FullRed, matcher.lookup(DrinkType.Wine, "바롤로")?.wineStyle)
        assertEquals(WineStyle.AromaticWhite, matcher.lookup(DrinkType.Wine, "모스카토 다스티")?.wineStyle)
    }

    // 아메리칸 위스키는 법적으로 새 오크를 쓴다. 하나라도 다르면 데이터가 틀린 것이다.
    @Test
    fun `american whiskeys are all virgin oak`() {
        listOf("메이커스 마크", "와일드터키 101", "버팔로 트레이스", "놉크릭 9", "불렛 라이").forEach { name ->
            assertEquals(name, CaskGroup.VirginOak, matcher.lookup(DrinkType.Whiskey, name)?.cask)
        }
    }

    // 로제는 4개 스타일 그룹에 자리가 없어 일부러 뺐다. 조용히 되살아나면 안 된다.
    @Test
    fun `rose was left out on purpose`() {
        assertNull(matcher.lookup(DrinkType.Wine, "프로방스 로제"))
    }
}

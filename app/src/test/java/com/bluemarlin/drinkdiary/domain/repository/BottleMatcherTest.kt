package com.bluemarlin.drinkdiary.domain.repository

import com.bluemarlin.drinkdiary.domain.model.BottleEntry
import com.bluemarlin.drinkdiary.domain.model.BottleFacts
import com.bluemarlin.drinkdiary.domain.model.CaskGroup
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.WineStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BottleMatcherTest {
    private fun whiskey(
        name: String,
        cask: CaskGroup,
        vararg aliases: String,
    ) = BottleEntry(DrinkType.Whiskey, name, setOf(name) + aliases, BottleFacts(cask = cask))

    private val entries =
        listOf(
            whiskey("발베니 12 더블우드", CaskGroup.Mixed, "Balvenie 12 DoubleWood"),
            whiskey("발베니 14 캐리비안캐스크", CaskGroup.Mixed),
            whiskey("아드벡 10", CaskGroup.Bourbon, "Ardbeg 10"),
            whiskey("아드벡 안오", CaskGroup.Mixed),
            whiskey("글렌드로낙 12", CaskGroup.Sherry),
            BottleEntry(
                DrinkType.Wine,
                "바롤로",
                setOf("바롤로", "Barolo"),
                BottleFacts(wineStyle = WineStyle.FullRed),
            ),
        )

    private val matcher = BottleMatcher { entries }

    private fun cask(name: String) = matcher.lookup(DrinkType.Whiskey, name)?.cask

    @Test
    fun `an exact name matches`() {
        assertEquals(CaskGroup.Sherry, cask("글렌드로낙 12"))
    }

    // 사용자가 이름을 어떻게 적을지 통제할 수 없다.
    @Test
    fun `spacing and 년 do not change the match`() {
        assertEquals(CaskGroup.Bourbon, cask("아드벡10"))
        assertEquals(CaskGroup.Bourbon, cask("아드벡 10년"))
        assertEquals(CaskGroup.Bourbon, cask("  아드벡  10  "))
    }

    @Test
    fun `english aliases match, case insensitively`() {
        assertEquals(CaskGroup.Mixed, cask("balvenie 12 doublewood"))
        assertEquals(CaskGroup.Bourbon, cask("Ardbeg 10"))
    }

    @Test
    fun `extra words the user typed do not break the match`() {
        assertEquals(CaskGroup.Mixed, cask("발베니 12 더블우드 한 잔"))
    }

    // 별칭이 입력의 부분 문자열이어야 하므로, 어느 아드벡인지 모르면 매칭되지 않는다.
    @Test
    fun `an ambiguous name matches nothing`() {
        assertNull(cask("아드벡"))
        assertNull(cask("발베니"))
    }

    @Test
    fun `the most specific alias wins`() {
        // "발베니 12"와 "발베니 14"가 함께 걸릴 수 있는 입력이 아니라,
        // 더 긴 별칭이 이기는지를 본다.
        val ambiguous =
            BottleMatcher {
                listOf(
                    whiskey("발베니", CaskGroup.Bourbon),
                    whiskey("발베니 12 더블우드", CaskGroup.Mixed),
                )
            }

        assertEquals(
            CaskGroup.Mixed,
            ambiguous.lookup(DrinkType.Whiskey, "발베니 12 더블우드")?.cask,
        )
    }

    // 같은 이름이 다른 주종에 있어도 섞이지 않는다.
    @Test
    fun `drink type scopes the lookup`() {
        assertNull(matcher.lookup(DrinkType.Whiskey, "바롤로"))
        assertEquals(WineStyle.FullRed, matcher.lookup(DrinkType.Wine, "바롤로")?.wineStyle)
    }

    @Test
    fun `an unknown bottle is null, not a guess`() {
        assertNull(cask("듣도보도 못한 위스키"))
        assertNull(cask(""))
        assertNull(cask("   "))
    }
}

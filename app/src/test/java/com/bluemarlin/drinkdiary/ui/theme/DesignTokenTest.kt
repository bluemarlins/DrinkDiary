package com.bluemarlin.drinkdiary.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

// 2026-08-16 감사에서 명세 3.1절 30개 값 중 6개만 코드와 일치한다는 것이 드러났고, 명도 대비는
// 손으로 계산해서 확인했다. **같은 계산을 다음 회귀 때 또 손으로 하지 않기 위해** 테스트로 옮긴다.
//
// 이 테스트가 지키는 것은 두 가지다.
//  1. 팔레트 hex가 명세 표와 같은가 — 표와 코드가 조용히 갈라지는 것을 막는다.
//  2. 실제로 화면에서 겹쳐 그려지는 조합이 명세 2절의 대비 기준을 넘는가 —
//     이 검사가 없어서 명세 자신이 위스키 뱃지를 4.11:1로 통과시켰다.
//
// 대비 대상은 `Theme.kt`의 슬롯 매핑을 따라 손으로 짝지은 것이다. 매핑을 바꾸면 여기도 바꾼다.
class DesignTokenTest {
    @Test
    fun `light palette matches the spec table`() {
        assertEquals(hex("FFF8F2"), PaperLight)
        assertEquals(hex("FFFFFF"), SurfaceLight)
        assertEquals(hex("F6EDE4"), SurfaceSunkLight)
        assertEquals(hex("241E19"), InkLight)
        assertEquals(hex("6B5F56"), InkSoftLight)
        assertEquals(hex("9C8F84"), InkFaintLight)
        assertEquals(hex("E7DACC"), LineLight)
        assertEquals(hex("D3C2B0"), LineStrongLight)
        assertEquals(hex("2F6F4E"), PrimaryLight)
        assertEquals(hex("E4EFE8"), PrimaryContainerLight)
        assertEquals(hex("93425E"), WineLight)
        assertEquals(hex("F6E6EC"), WineContainerLight)
        assertEquals(hex("8A5A1B"), MaltLight)
        assertEquals(hex("F8ECDA"), MaltContainerLight)
        assertEquals(hex("BA1A1A"), DestructiveLight)
    }

    @Test
    fun `dark palette matches the spec table`() {
        assertEquals(hex("15110E"), PaperDark)
        assertEquals(hex("221C17"), SurfaceDark)
        assertEquals(hex("1B1612"), SurfaceSunkDark)
        assertEquals(hex("F2E9E0"), InkDark)
        assertEquals(hex("B3A498"), InkSoftDark)
        assertEquals(hex("7D7066"), InkFaintDark)
        assertEquals(hex("3A302A"), LineDark)
        assertEquals(hex("4E4139"), LineStrongDark)
        assertEquals(hex("6FBF93"), PrimaryDark)
        assertEquals(hex("1E3229"), PrimaryContainerDark)
        assertEquals(hex("DB90AC"), WineDark)
        assertEquals(hex("38222B"), WineContainerDark)
        assertEquals(hex("DFA75B"), MaltDark)
        assertEquals(hex("38290F"), MaltContainerDark)
        assertEquals(hex("FFB4AB"), DestructiveDark)
    }

    // 명세 2절: 본문 7.0:1 (AAA)
    @Test
    fun `body text reaches AAA on both themes`() {
        assertContrast("Ink on Paper (light)", InkLight, PaperLight, AAA)
        assertContrast("Ink on Surface (light)", InkLight, SurfaceLight, AAA)
        assertContrast("Ink on Paper (dark)", InkDark, PaperDark, AAA)
        assertContrast("Ink on Surface (dark)", InkDark, SurfaceDark, AAA)
    }

    // 명세 2절: 보조 4.5:1 (AA)
    @Test
    fun `secondary text reaches AA on both themes`() {
        assertContrast("InkSoft on Paper (light)", InkSoftLight, PaperLight, AA)
        assertContrast("InkSoft on Surface (light)", InkSoftLight, SurfaceLight, AA)
        assertContrast("InkSoft on SurfaceSunk (light)", InkSoftLight, SurfaceSunkLight, AA)
        assertContrast("InkSoft on Paper (dark)", InkSoftDark, PaperDark, AA)
        assertContrast("InkSoft on Surface (dark)", InkSoftDark, SurfaceDark, AA)
        assertContrast("InkSoft on SurfaceSunk (dark)", InkSoftDark, SurfaceSunkDark, AA)
    }

    // 뱃지 레이블은 `LabelSmall` 11sp라 본문 취급이다. **이 조합이 감사에서 걸린 자리다** —
    // Malt Light가 `#9C6722`일 때 4.11:1로 미달이었다.
    @Test
    fun `badge labels reach AA on their own container`() {
        assertContrast("Primary on PrimaryContainer (light)", PrimaryLight, PrimaryContainerLight, AA)
        assertContrast("Wine on WineContainer (light)", WineLight, WineContainerLight, AA)
        assertContrast("Malt on MaltContainer (light)", MaltLight, MaltContainerLight, AA)
        assertContrast("Primary on PrimaryContainer (dark)", PrimaryDark, PrimaryContainerDark, AA)
        assertContrast("Wine on WineContainer (dark)", WineDark, WineContainerDark, AA)
        assertContrast("Malt on MaltContainer (dark)", MaltDark, MaltContainerDark, AA)
    }

    @Test
    fun `action and error colors reach AA on their surface`() {
        assertContrast("Primary on Surface (light)", PrimaryLight, SurfaceLight, AA)
        assertContrast("Paper on Primary (light)", PaperLight, PrimaryLight, AA)
        assertContrast("Destructive on Surface (light)", DestructiveLight, SurfaceLight, AA)
        assertContrast("Primary on Surface (dark)", PrimaryDark, SurfaceDark, AA)
        assertContrast("Paper on Primary (dark)", PaperDark, PrimaryDark, AA)
        assertContrast("Destructive on Surface (dark)", DestructiveDark, SurfaceDark, AA)
    }

    // InkFaint(비활성 텍스트)와 Line(1px 테두리)은 WCAG 대비 요건 대상이 아니다.
    // 대신 **보조 텍스트로 잘못 쓰이는 것**을 막는다 — 그 용도라면 이 값들은 통과하지 못한다.
    @Test
    fun `inkFaint stays below the secondary-text threshold`() {
        assertTrue(
            "InkFaint가 AA를 넘으면 보조 텍스트와 구분되지 않아 비활성 표시가 사라진다",
            contrast(InkFaintLight, SurfaceSunkLight) < AA,
        )
        assertTrue(
            "InkFaint가 AA를 넘으면 보조 텍스트와 구분되지 않아 비활성 표시가 사라진다",
            contrast(InkFaintDark, SurfaceSunkDark) < AA,
        )
    }

    private fun assertContrast(
        what: String,
        foreground: Color,
        background: Color,
        minimum: Double,
    ) {
        val ratio = contrast(foreground, background)
        assertTrue(
            "$what: %.2f:1 — 명세 2절이 요구하는 %.1f:1 미달".format(ratio, minimum),
            ratio >= minimum,
        )
    }

    // WCAG 2.1 상대 휘도 및 대비 공식.
    private fun contrast(
        a: Color,
        b: Color,
    ): Double {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        val lighter = maxOf(la, lb)
        val darker = minOf(la, lb)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun channel(raw: Float): Double {
            val c = raw.toDouble()
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }

    private fun hex(value: String): Color = Color(0xFF000000L or value.toLong(16))

    private companion object {
        const val AA = 4.5
        const val AAA = 7.0
    }
}

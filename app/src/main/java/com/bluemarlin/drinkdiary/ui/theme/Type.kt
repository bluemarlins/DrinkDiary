package com.bluemarlin.drinkdiary.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 81가지 취향 유형 코드 (Serif Bold)
val DisplayTasteCode =
    TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.05.sp,
    )

// 취향 요약 핵심 문장 (Sans SemiBold)
val HeadlineSentence =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.02).sp,
    )

val CompactTitle =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    )

val CompactLabel =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    )

val Typography =
    Typography(
        displayLarge = DisplayTasteCode,
        headlineMedium = HeadlineSentence,
        headlineSmall = CompactTitle,
        titleLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.01).sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.01.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.01.sp,
            ),
        labelSmall = CompactLabel,
    )

val DrinkDiaryShapes =
    Shapes(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(18.dp),
    )

// 확정 명세 `../../../../../../docs/specs/designer/design-system.md` 3.3절의 간격 스케일과
// **이름까지 일치시킨다.** 이전 코드는 20dp가 아예 없고 `lg`가 24dp였는데, 명세의 `lg`는 20dp다.
// 이름이 어긋난 토큰은 안 쓰는 것만 못하다 — 문서를 보고 `lg`를 쓴 사람이 다른 값을 얻는다.
//
// (명세 2절 표는 토큰 목록을 `4·8·12·16·24·32`로 적어 20dp가 빠져 있다. 3.3절이 간격을 정의하는
//  절이므로 그쪽을 따랐다. 두 절의 불일치는 디자인 부서에 남겨 둔다.)
object DrinkDiarySpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
}

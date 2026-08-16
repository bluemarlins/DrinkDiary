package com.bluemarlin.drinkdiary.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 명세 `../../../../../../docs/specs/designer/design-system.md` 3.2절의 Tracking 열은 `em`인데
// 코드는 그 숫자를 그대로 `.sp`로 적고 있었다 — 32sp에 `0.05.sp`는 0.0016em이라 자간이 사실상 없었다.
//
// **그렇다고 `.em`을 쓸 수는 없다.** Compose는 letterSpacing을 보간할 때 Em과 Sp를 섞지 못하고
// `IllegalArgumentException: Cannot perform operation for Em and Sp`로 죽는다. `OutlinedTextField`의
// 라벨이 `bodyLarge`(우리 것)와 `bodySmall`(Material 기본값, `.sp`) 사이를 오가면서 실제로 그렇게 됐다 —
// 컴파일·lint·유닛테스트는 전부 통과하고 화면을 열어야 터진다.
//
// 그래서 em 값을 **폰트 크기로 환산해 sp로 적는다.** 곱셈을 소스에 남겨 두는 것은 명세의 em 값이
// 코드에서 그대로 읽히게 하기 위해서다 — fontSize를 바꾸면 이 식도 함께 고쳐야 한다.
//
// 롤은 명세대로 8종이며, 여기 없는 롤은 만들지 않는다.

// 81가지 취향 유형 코드 (Serif Bold)
val DisplayTasteCode =
    TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (32 * 0.05).sp,
    )

// 취향 요약 핵심 문장 (Sans SemiBold)
val HeadlineSentence =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (20 * -0.02).sp,
    )

val Typography =
    Typography(
        displayLarge = DisplayTasteCode,
        headlineMedium = HeadlineSentence,
        titleLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = (18 * -0.01).sp,
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
                letterSpacing = (13 * 0.01).sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = (14 * 0.01).sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = (11 * 0.03).sp,
            ),
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

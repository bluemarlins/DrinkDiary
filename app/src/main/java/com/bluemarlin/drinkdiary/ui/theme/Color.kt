package com.bluemarlin.drinkdiary.ui.theme

import androidx.compose.ui.graphics.Color

// 이름과 값 모두 확정 명세 `../../../../../../docs/specs/designer/design-system.md` 3.1절 표에서 온다.
// 이전 코드는 `DrinkPaperLight`/`CellarInkDark`처럼 다른 이름 체계를 써서, 명세를 읽고 `InkFaint`를
// 찾으면 코드에 없고 `LineStrong`을 찾으면 다른 색이 나왔다. 표를 고치면 여기도 고친다 —
// `DesignTokenTest`가 두 쪽이 갈라지는 것을 막는다.

val PaperLight = Color(0xFFFFF8F2)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceSunkLight = Color(0xFFF6EDE4)
val InkLight = Color(0xFF241E19)
val InkSoftLight = Color(0xFF6B5F56)
val InkFaintLight = Color(0xFF9C8F84)
val LineLight = Color(0xFFE7DACC)
val LineStrongLight = Color(0xFFD3C2B0)
val PrimaryLight = Color(0xFF2F6F4E)
val PrimaryContainerLight = Color(0xFFE4EFE8)
val WineLight = Color(0xFF93425E)
val WineContainerLight = Color(0xFFF6E6EC)
val MaltLight = Color(0xFF8A5A1B)
val MaltContainerLight = Color(0xFFF8ECDA)
val DestructiveLight = Color(0xFFBA1A1A)

val PaperDark = Color(0xFF15110E)
val SurfaceDark = Color(0xFF221C17)
val SurfaceSunkDark = Color(0xFF1B1612)
val InkDark = Color(0xFFF2E9E0)
val InkSoftDark = Color(0xFFB3A498)
val InkFaintDark = Color(0xFF7D7066)
val LineDark = Color(0xFF3A302A)
val LineStrongDark = Color(0xFF4E4139)
val PrimaryDark = Color(0xFF6FBF93)
val PrimaryContainerDark = Color(0xFF1E3229)
val WineDark = Color(0xFFDB90AC)
val WineContainerDark = Color(0xFF38222B)
val MaltDark = Color(0xFFDFA75B)
val MaltContainerDark = Color(0xFF38290F)
val DestructiveDark = Color(0xFFFFB4AB)

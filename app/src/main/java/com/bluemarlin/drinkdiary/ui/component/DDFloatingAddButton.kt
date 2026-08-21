package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.ui.navigation.DDBottomNavigationBarHeight
import com.bluemarlin.drinkdiary.ui.navigation.LocalHazeState
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * 플로팅 UI 컴포넌트 규격에 맞춘 원형 Glassmorphic 추가 버튼.
 *
 * DDFloatingTopAppBar의 원형 액션 버튼 및 DDBottomNavigationBar와 동일한
 * 조형 언어(CircleShape, Haze 블러 20dp, 노이즈 0.08, outlineVariant 1dp 테두리)를 따른다.
 */
@Composable
fun DDFloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = DDBottomNavigationBarHeight - DrinkDiarySpacing.xs * 2,
    hazeState: HazeState? = LocalHazeState.current,
    contentDescription: String = "기록 추가",
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(
                            state = hazeState,
                            style =
                                HazeStyle(
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    tint = HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)),
                                    blurRadius = 20.dp,
                                    noiseFactor = 0.08f,
                                ),
                        )
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f), CircleShape)
                    },
                ).border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                ).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    role = Role.Button,
                    onClick = onClick,
                ).semantics {
                    this.contentDescription = contentDescription
                },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}

package com.bluemarlin.drinkdiary.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DD5AxisRadarCard
import com.bluemarlin.drinkdiary.ui.component.DDDrinkHighlightRow
import com.bluemarlin.drinkdiary.ui.component.DDRecentTrendCard
import com.bluemarlin.drinkdiary.ui.component.DDStatWidgetCard
import com.bluemarlin.drinkdiary.ui.component.DDTagContrastCard
import com.bluemarlin.drinkdiary.ui.component.DDTasteIdentityCard
import com.bluemarlin.drinkdiary.ui.component.DDTastingGapCard
import com.bluemarlin.drinkdiary.ui.component.DDTastingGapLine
import com.bluemarlin.drinkdiary.ui.component.RadarAxisData
import com.bluemarlin.drinkdiary.ui.navigation.DDBottomNavigationBarHeight
import com.bluemarlin.drinkdiary.ui.navigation.DDWindowSize
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDWindowSize
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onSelectScope: (TypeScope) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    val twoPane = LocalDDWindowSize.current != DDWindowSize.Compact
    val fabClearance = DDBottomNavigationBarHeight + LocalDDScreenMargin.current

    val accentColor =
        when (state.scope) {
            TypeScope.Wine -> Color(0xFFE05375)
            TypeScope.Whiskey -> Color(0xFFF4B266)
            TypeScope.Combined -> MaterialTheme.colorScheme.primary
        }

    val drinkType =
        when (state.scope) {
            TypeScope.Wine -> DrinkType.Wine
            TypeScope.Whiskey -> DrinkType.Whiskey
            TypeScope.Combined -> null
        }

    if (!twoPane) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight + fabClearance)
                        .padding(
                            start = LocalDDScreenMargin.current,
                            end = LocalDDScreenMargin.current,
                            top = contentPadding.calculateTopPadding(),
                            bottom = fabClearance,
                        ),
                verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.md),
            ) {
                // Scope Switcher
                ScopeSelector(
                    currentScope = state.scope,
                    onSelectScope = onSelectScope,
                    accentColor = accentColor,
                )

                // 1. [2x1] Hero 술 정체성 카드
                DDTasteIdentityCard(
                    readiness = state.readiness,
                    profile = state.profile,
                    reflection = state.reflection,
                    accentColor = accentColor,
                )

                // 2. [2x2] 5축 테이스팅 레이더 차트
                val radarAxes = computeRadarAxes(state)
                val subTitle =
                    when (state.scope) {
                        TypeScope.Wine -> "와인 ${state.profile?.recordCount ?: 0}잔 평균"
                        TypeScope.Whiskey -> "위스키 ${state.profile?.recordCount ?: 0}잔 평균"
                        TypeScope.Combined -> "전체 ${state.profile?.recordCount ?: 0}잔 평균"
                    }
                DD5AxisRadarCard(
                    axes = radarAxes,
                    accentColor = accentColor,
                    subTitle = subTitle,
                )

                // 3. [1x1 + 1x1] 미니 통계 카드 Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
                ) {
                    val countIcon = if (state.scope == TypeScope.Whiskey) "🥃" else "🍷"
                    val thisMonthCount = state.monthly.total
                    DDStatWidgetCard(
                        icon = countIcon,
                        title = "8월 마신 잔",
                        badge = "월간",
                        statValue = "${thisMonthCount}잔",
                        deltaText = if (thisMonthCount > 0) "▲ 기록이 쌓이는 중" else "이번 달 첫 기록을 남겨보세요",
                        accentColor = accentColor,
                        modifier = Modifier.weight(1f),
                        progressFraction = (thisMonthCount.toFloat() / 15f).coerceIn(0.1f, 1f),
                    )

                    val repurchaseRate =
                        if (thisMonthCount > 0) {
                            "${((state.monthly.repurchaseCount.toFloat() / thisMonthCount.toFloat()) * 100).toInt()}%"
                        } else {
                            "0%"
                        }
                    DDStatWidgetCard(
                        icon = "🔄",
                        title = "재구매 의사",
                        badge = "선호도",
                        statValue = repurchaseRate,
                        deltaText = "${thisMonthCount}잔 중 ${state.monthly.repurchaseCount}잔 '또 살래요'",
                        accentColor = Color(0xFF4EBA87),
                        deltaColor = Color(0xFF4EBA87),
                        modifier = Modifier.weight(1f),
                        progressFraction =
                            if (thisMonthCount >
                                0
                            ) {
                                state.monthly.repurchaseCount.toFloat() / thisMonthCount.toFloat()
                            } else {
                                0f
                            },
                    )
                }

                // 4. [2x1] 인생술 하이라이트 (TOP RATED)
                if (state.highlights.isNotEmpty()) {
                    DDDrinkHighlightRow(cards = state.highlights.map(DrinkHighlightCopy::card))
                }

                // 5. [2x1] 라벨 선호 대조
                DDTagContrastCard(
                    preferences = state.tagPreferences,
                    drinkType = drinkType,
                    accentColor = accentColor,
                )

                // 6. [2x1] 미개척 영역 탐색 가이드
                if (state.tastingGaps.isNotEmpty()) {
                    DDTastingGapCard(
                        lines =
                            state.tastingGaps.map {
                                DDTastingGapLine(
                                    label = TastingGapCopy.label(it),
                                    sentence = TastingGapCopy.sentence(it, drinkType),
                                )
                            },
                    )
                }

                // 7. 최근 흐름
                state.recentTrend?.let { trend ->
                    DDRecentTrendCard(
                        caption = RecentTrendCopy.caption(trend),
                        shiftLine = RecentTrendCopy.shiftLine(trend),
                        recent = RecentTrendCopy.recentBar(trend),
                        earlier = RecentTrendCopy.earlierBar(trend),
                        verdict = RecentTrendCopy.verdict(trend),
                    )
                }
            }
        }
        return
    }

    // 2-pane tablet layout
    Row(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    start = LocalDDScreenMargin.current,
                    end = LocalDDScreenMargin.current,
                    top = LocalDDScreenMargin.current,
                ),
        horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xl),
    ) {
        Column(
            modifier = Modifier.weight(0.45f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.md),
        ) {
            ScopeSelector(
                currentScope = state.scope,
                onSelectScope = onSelectScope,
                accentColor = accentColor,
            )

            DDTasteIdentityCard(
                readiness = state.readiness,
                profile = state.profile,
                reflection = state.reflection,
                accentColor = accentColor,
            )

            val radarAxes = computeRadarAxes(state)
            DD5AxisRadarCard(
                axes = radarAxes,
                accentColor = accentColor,
                subTitle = "${state.profile?.recordCount ?: 0}잔 평균",
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(0.55f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = fabClearance),
            verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
            ) {
                val thisMonthCount = state.monthly.total
                DDStatWidgetCard(
                    icon = "🍷",
                    title = "8월 마신 잔",
                    badge = "월간",
                    statValue = "${thisMonthCount}잔",
                    deltaText = "기록이 꾸준히 쌓여요",
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f),
                )

                val repurchaseRate =
                    if (thisMonthCount > 0) {
                        "${((state.monthly.repurchaseCount.toFloat() / thisMonthCount.toFloat()) * 100).toInt()}%"
                    } else {
                        "0%"
                    }
                DDStatWidgetCard(
                    icon = "🔄",
                    title = "재구매 의사",
                    badge = "선호도",
                    statValue = repurchaseRate,
                    deltaText = "${thisMonthCount}잔 중 ${state.monthly.repurchaseCount}잔 '또 살래요'",
                    accentColor = Color(0xFF4EBA87),
                    deltaColor = Color(0xFF4EBA87),
                    modifier = Modifier.weight(1f),
                )
            }

            if (state.highlights.isNotEmpty()) {
                DDDrinkHighlightRow(cards = state.highlights.map(DrinkHighlightCopy::card))
            }

            DDTagContrastCard(
                preferences = state.tagPreferences,
                drinkType = drinkType,
                accentColor = accentColor,
            )

            if (state.tastingGaps.isNotEmpty()) {
                DDTastingGapCard(
                    lines =
                        state.tastingGaps.map {
                            DDTastingGapLine(
                                label = TastingGapCopy.label(it),
                                sentence = TastingGapCopy.sentence(it, drinkType),
                            )
                        },
                )
            }
        }
    }
}

@Composable
private fun ScopeSelector(
    currentScope: TypeScope,
    onSelectScope: (TypeScope) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ScopeButton(
                label = "🍷 와인",
                selected = currentScope == TypeScope.Wine,
                activeColor = Color(0xFFE05375),
                onClick = { onSelectScope(TypeScope.Wine) },
                modifier = Modifier.weight(1f),
            )
            ScopeButton(
                label = "🥃 위스키",
                selected = currentScope == TypeScope.Whiskey,
                activeColor = Color(0xFFF4B266),
                onClick = { onSelectScope(TypeScope.Whiskey) },
                modifier = Modifier.weight(1f),
            )
            ScopeButton(
                label = "🌐 통합",
                selected = currentScope == TypeScope.Combined,
                activeColor = MaterialTheme.colorScheme.primary,
                onClick = { onSelectScope(TypeScope.Combined) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ScopeButton(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (selected) activeColor.copy(alpha = 0.2f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, activeColor.copy(alpha = 0.4f)) else null,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

private fun computeRadarAxes(state: ProfileUiState): List<RadarAxisData> {
    val profile = state.profile ?: return emptyList()
    val traits =
        when (state.scope) {
            TypeScope.Wine -> Trait.wineTraits
            TypeScope.Whiskey -> Trait.whiskyTraits
            TypeScope.Combined -> Trait.shared
        }

    return traits.map { trait ->
        val pref = profile.preference(trait)
        val avg = pref?.averageLevel ?: 0.0
        RadarAxisData(
            label = DrinkLabels.trait(trait),
            value = if (avg > 0.0) avg else 2.5,
        )
    }
}

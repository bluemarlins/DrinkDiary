package com.bluemarlin.drinkdiary.ui.profile

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                        badge = "이번 달",
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

                // 4. [2x1] 최고 평점 하이라이트 (있을 때만)
                if (state.highlights.isNotEmpty()) {
                    DDDrinkHighlightRow(cards = state.highlights.map(DrinkHighlightCopy::card))
                }

                // 5. [2x2] 라벨 대조 바
                DDTagContrastCard(
                    preferences = state.tagPreferences,
                    drinkType = drinkType,
                    accentColor = accentColor,
                )

                // 6. [2x2] 탐색 갭 가이드 (있을 때만)
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
                val countIcon = if (state.scope == TypeScope.Whiskey) "🥃" else "🍷"
                val thisMonthCount = state.monthly.total
                DDStatWidgetCard(
                    icon = countIcon,
                    title = "8월 마신 잔",
                    badge = "이번 달",
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

private fun computeRadarAxes(state: ProfileUiState): List<RadarAxisData> {
    val traitOrder =
        when (state.scope) {
            TypeScope.Whiskey ->
                listOf(
                    Trait.Sweetness to 5,
                    Trait.Smokiness to 5,
                    Trait.AlcoholFeel to 5,
                    Trait.Body to 5,
                    Trait.Finish to 5,
                )
            TypeScope.Wine, TypeScope.Combined ->
                listOf(
                    Trait.Sweetness to 5,
                    Trait.Acidity to 5,
                    Trait.Tannin to 5,
                    Trait.Body to 5,
                    Trait.Finish to 5,
                )
        }

    val prefs = state.traitPreferences.associateBy { it.trait }

    return traitOrder.map { (trait, maxScale) ->
        val pref = prefs[trait]
        val value = pref?.averageLevel ?: 0.0f
        RadarAxisData(
            label = DrinkLabels.trait(trait),
            value = value,
            maxScale = maxScale,
        )
    }
}

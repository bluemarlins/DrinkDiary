package com.bluemarlin.drinkdiary.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.AnswerReflection
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TagValueRating
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDChip
import com.bluemarlin.drinkdiary.ui.component.DDDrinkHighlightRow
import com.bluemarlin.drinkdiary.ui.component.DDMonthlySummaryCard
import com.bluemarlin.drinkdiary.ui.component.DDProfileProgressCard
import com.bluemarlin.drinkdiary.ui.component.DDRecentTrendCard
import com.bluemarlin.drinkdiary.ui.component.DDTasteSentenceCard
import com.bluemarlin.drinkdiary.ui.component.DDTasteTypeBadge
import com.bluemarlin.drinkdiary.ui.component.DDTastingGapCard
import com.bluemarlin.drinkdiary.ui.component.DDTastingGapLine
import com.bluemarlin.drinkdiary.ui.navigation.DDWindowSize
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDWindowSize
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// F3 — 문장 우선, 차트는 보조(software-architecture.md 6절).
// N개 미만이어도 화면을 비우지 않고 "아직 이르다"와 근거를 함께 보여준다(prd.md F3).
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onScopeChange: (TypeScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    val twoPane = LocalDDWindowSize.current != DDWindowSize.Compact
    val fabClearance = 96.dp

    if (!twoPane) {
        // FAB는 내용 위에 떠 있다. 아래 여백만 주면 부족한데, **내용이 화면보다 짧으면 스크롤이
        // 아예 생기지 않아** FAB 밑에 깔린 줄을 걷어낼 방법이 없기 때문이다 —
        // 실제로 "버번 캐스크 2.3점"이 통째로 가려졌다(에뮬레이터에서 확인된 결함).
        // 최소 높이를 화면보다 FAB만큼 크게 잡아 **항상 그만큼은 스크롤되도록** 보장한다.
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
                            top = LocalDDScreenMargin.current,
                            bottom = fabClearance,
                        ),
                verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xl),
            ) {
                SummarySection(state = state, onScopeChange = onScopeChange)
                DDMonthlySummaryCard(summary = state.monthly)
                TraitSections(state = state)
            }
        }
        return
    }

    // 명세 4절: 2단에서는 요약(좌 40%)과 축 목록(우 60%)을 나눈다. 요약은 짧고 축 목록은 길어서
    // 한 스크롤에 묶으면 왼쪽에 빈 스크롤이 생긴다 — 칸마다 따로 스크롤한다.
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
        SummarySection(
            state = state,
            onScopeChange = onScopeChange,
            modifier = Modifier.weight(0.4f).verticalScroll(rememberScrollState()),
        )
        // FAB는 오른쪽 아래에 뜬다. 가릴 내용이 있는 쪽은 이 칸뿐이다.
        TraitSections(
            state = state,
            modifier =
                Modifier
                    .weight(0.6f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = fabClearance),
        )
    }
}

@Composable
private fun SummarySection(
    state: ProfileUiState,
    onScopeChange: (TypeScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xl),
    ) {
        ScopeSelector(selected = state.scope, onSelect = onScopeChange)
        SummaryHeadline(
            readiness = state.readiness,
            profile = state.profile,
            reflection = state.reflection,
        )
        // 결론 바로 다음이 사진이다. 사용자가 "한눈에 안 들어온다"고 한 자리가 여기이고,
        // 글자보다 먼저 보여야 할 것이 사진이다(prd.md F3-4 (a)).
        DDDrinkHighlightRow(cards = state.highlights.map(DrinkHighlightCopy::card))
        // 유형 **아래**다. 유형은 잘 안 바뀌는 것이 설계이므로, 새 기록이 화면을 바꾸는 일은
        // 이 층이 맡는다(prd.md F3-3 (a)).
        state.recentTrend?.let { trend ->
            DDRecentTrendCard(
                caption = RecentTrendCopy.caption(trend),
                lines = RecentTrendCopy.lines(trend),
            )
        }
        // 2단에서는 요약 칸이 짧아 빈 스크롤이 생겼다. 월 요약이 그 자리를 채우면서
        // 1단과 같은 순서(취향 먼저, 회고 나중)를 유지한다. 1단에서는 바깥에서 그린다.
        if (LocalDDWindowSize.current != DDWindowSize.Compact) {
            DDMonthlySummaryCard(summary = state.monthly)
        }
    }
}

@Composable
private fun TraitSections(
    state: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xl),
    ) {
        // 축별 판정 상태 목록은 걷어냈다(2026-08-17 사용자 지시) — 사용자는 "어떻게 그 결론이
        // 나왔는지"를 묻지 않는다. 남긴 것은 **매장에 가져갈 수 있는** 라벨 기준뿐이다.
        if (state.tagPreferences.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.md)) {
                Text("라벨로 본 취향", style = MaterialTheme.typography.titleMedium)
                Text(
                    // **무엇인지를 먼저 말한다.** 이쪽은 집계이고 유형은 상관에서 나온다.
                    // 그 차이를 밝히지 않으면 이 숫자들이 유형의 근거로 읽히고, 그 순간
                    // 화면이 근거라고 내놓는 것이 실제 판정 근거와 달라진다(prd.md F3-3 (c)).
                    // 뒷문장은 이쪽 결과가 감각 축과 달리 **가져갈 수 있다**는 뜻이다.
                    text = "기록한 만족도를 라벨별로 모은 거예요. 매장에서 라벨만 보고도 쓸 수 있어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.tagPreferences.forEach { pref ->
                    TagPreferenceBlock(pref, state.scope)
                }
            }
        }

        // 있는 것 바로 다음에 없는 것을 둔다. 태그 선호가 아직 안 나온 사용자에게도 보여야
        // 하므로 위 블록과 독립이다 — 오히려 그쪽이 이 안내가 가장 쓸모 있는 상태다.
        if (state.tastingGaps.isNotEmpty()) {
            DDTastingGapCard(
                lines =
                    state.tastingGaps.map { gap ->
                        DDTastingGapLine(
                            label = TastingGapCopy.label(gap),
                            sentence = TastingGapCopy.sentence(gap, drinkTypeOf(state.scope)),
                        )
                    },
            )
        }
    }
}

// 통합 스코프에서는 주종을 특정할 수 없다. 도수 구간의 경계가 주종마다 달라서, 모를 때는
// 순서로만 말해야 한다.
private fun drinkTypeOf(scope: TypeScope): DrinkType? =
    when (scope) {
        TypeScope.Wine -> DrinkType.Wine
        TypeScope.Whiskey -> DrinkType.Whiskey
        TypeScope.Combined -> null
    }

@Composable
private fun ScopeSelector(
    selected: TypeScope,
    onSelect: (TypeScope) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            TypeScope.Wine to "와인",
            TypeScope.Whiskey to "위스키",
            TypeScope.Combined to "통합",
        ).forEach { (scope, label) ->
            DDChip(
                label = label,
                selected = selected == scope,
                onClick = { onSelect(scope) },
            )
        }
    }
}

@Composable
private fun SummaryHeadline(
    readiness: ProfileReadiness,
    profile: TasteProfile?,
    reflection: AnswerReflection,
) {
    val recordCount = profile?.recordCount ?: 0

    when (readiness) {
        is ProfileReadiness.Ready -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DDTasteTypeBadge(
                    code = readiness.type.code,
                    name = TasteTypeCopy.shortName(readiness.type),
                )
                DDTasteSentenceCard(
                    sentence = TasteTypeCopy.sentence(readiness.type),
                    details = listOf("기록 ${recordCount}개를 바탕으로 판정했어요."),
                )
            }
        }

        is ProfileReadiness.NotReady -> {
            // 게이지는 남기고 **숫자 약속만** 걷어냈다. 남은 거리는 게이지가 말한다
            // (prd.md 7절-2 · F3-3 (d)).
            DDProfileProgressCard(
                title = AnswerReflectionCopy.TITLE,
                description = AnswerReflectionCopy.description(recordCount, reflection),
                currentCount = recordCount,
                targetCount = recordCount + readiness.recordsNeeded,
                details = AnswerReflectionCopy.lines(reflection),
            )
        }
    }
}

@Composable
private fun TagPreferenceBlock(
    pref: TagPreference,
    scope: TypeScope,
) {
    val drinkType = drinkTypeOf(scope)
    val contrast = pref.contrast

    Column(verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(DrinkLabels.tagCategory(pref.category), style = MaterialTheme.typography.bodyLarge)
            if (contrast == null) {
                // 차이가 없으면 없다고 말한다. 순위만 보여주면 없는 선호가 있는 것처럼 읽힌다.
                Text(
                    text = "아직 차이가 뚜렷하지 않아요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // 대조가 성립하면 양 끝을 먼저 보여준다. Vivino의 What You Like / Dislike와 같은
        // 자리이되, **판정을 대신 내려주지 않는다** — "좋아하는 쪽"이 아니라 "높게 준 쪽"이다.
        // 실제로 있었던 일만 말하면 훈계할 자리가 남지 않는다(branding.md 2-1).
        if (contrast != null) {
            TagValueRow("높게 준 쪽", contrast.higher, pref.category, drinkType, emphasised = true)
            TagValueRow("낮게 준 쪽", contrast.lower, pref.category, drinkType, emphasised = true)
        }

        // 대조에 뽑히지 않은 값도 남긴다. 감추면 사용자가 근거를 확인할 수 없다(prd.md F3).
        pref.values
            .filter { it != contrast?.higher && it != contrast?.lower }
            .forEach { value ->
                TagValueRow(null, value, pref.category, drinkType, emphasised = false)
            }
    }
}

@Composable
private fun TagValueRow(
    role: String?,
    value: TagValueRating,
    category: TagCategory,
    drinkType: DrinkType?,
    emphasised: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // 표본 수는 값을 한정하는 말이므로 값 옆에 둔다. 오른쪽 끝은 FAB가 떠 있는
        // 자리라, 거기 두면 화면 아래쪽에서 잘린다(에뮬레이터에서 확인된 결함).
        val label = "${DrinkLabels.tagValue(category, value.value, drinkType)} · ${value.samples}잔"
        Text(
            text = if (role == null) label else "$role · $label",
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (emphasised) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        Text(
            text = DrinkLabels.rating(value.averageRating),
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (emphasised) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}

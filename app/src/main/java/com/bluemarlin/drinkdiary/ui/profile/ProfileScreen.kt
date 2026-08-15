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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDProfileProgressCard
import com.bluemarlin.drinkdiary.ui.component.DDTasteSentenceCard
import com.bluemarlin.drinkdiary.ui.component.DDTasteTypeBadge

// F3 — 문장 우선, 차트는 보조(software-architecture.md 6절).
// N개 미만이어도 화면을 비우지 않고 "아직 이르다"와 근거를 함께 보여준다(prd.md F3).
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onScopeChange: (TypeScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    // FAB는 내용 위에 떠 있다. 아래 여백만 주면 부족한데, **내용이 화면보다 짧으면 스크롤이
    // 아예 생기지 않아** FAB 밑에 깔린 줄을 걷어낼 방법이 없기 때문이다 —
    // 실제로 "버번 캐스크 2.3점"이 통째로 가려졌다(에뮬레이터에서 확인된 결함).
    // 최소 높이를 화면보다 FAB만큼 크게 잡아 **항상 그만큼은 스크롤되도록** 보장한다.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val fabClearance = 96.dp

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight + fabClearance)
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = fabClearance),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            ScopeSelector(selected = state.scope, onSelect = onScopeChange)
            SummaryHeadline(readiness = state.readiness, profile = state.profile)

            val preferences = state.profile?.preferences.orEmpty()

            // 유형은 공통 축만으로 성립한다. 주종 고유 축을 같은 목록에 섞으면 유형까지의 거리가
            // 실제보다 멀어 보이고, 기본 입력 경로가 묻지도 않는 축에 "더 기록하면 된다"고
            // 약속하게 된다(실기기에서 확인된 결함).
            val (shared, specific) = preferences.partition { it.trait.shared }

            if (shared.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("유형을 만드는 축", style = MaterialTheme.typography.titleMedium)
                    shared.forEach { pref -> TraitStatusRow(pref) }
                }
            }

            // 고유 축은 확장 입력 경로로만 채워진다. 답이 하나도 없으면 아예 보여주지 않는다 —
            // 채울 방법이 없는 항목을 미완성으로 걸어두지 않는다.
            val answeredSpecific = specific.filter { it.samples > 0 }
            if (answeredSpecific.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("추가로 기록한 축", style = MaterialTheme.typography.titleMedium)
                    answeredSpecific.forEach { pref -> TraitStatusRow(pref) }
                }
            }

            if (state.tagPreferences.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("라벨로 본 취향", style = MaterialTheme.typography.titleMedium)
                    Text(
                        // 이쪽 결과는 가져갈 수 있다는 점이 감각 축과 다르다.
                        text = "매장에서 라벨만 보고도 쓸 수 있는 기준이에요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    state.tagPreferences.forEach { pref ->
                        TagPreferenceBlock(pref, state.scope)
                    }
                }
            }
        }
    }
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
            FilterChip(
                selected = selected == scope,
                onClick = { onSelect(scope) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun SummaryHeadline(
    readiness: ProfileReadiness,
    profile: TasteProfile?,
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
            val description =
                if (recordCount == 0) {
                    "첫 기록을 남기면 여기서 취향이 보이기 시작해요."
                } else {
                    "지금까지 ${recordCount}개를 기록했어요. ${readiness.recordsNeeded}개만 더 남기면 유형이 나와요."
                }
            val target = recordCount + readiness.recordsNeeded
            DDProfileProgressCard(
                title = "아직 취향을 판단하기엔 일러요",
                description = description,
                currentCount = recordCount,
                targetCount = target,
            )
        }
    }
}

@Composable
private fun TagPreferenceBlock(
    pref: TagPreference,
    scope: TypeScope,
) {
    // 통합 스코프에서는 주종을 특정할 수 없으므로 도수 구간을 순서로만 말한다.
    val drinkType =
        when (scope) {
            TypeScope.Wine -> DrinkType.Wine
            TypeScope.Whiskey -> DrinkType.Whiskey
            TypeScope.Combined -> null
        }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(DrinkLabels.tagCategory(pref.category), style = MaterialTheme.typography.bodyLarge)
            if (!pref.meaningfulGap) {
                // 차이가 없으면 없다고 말한다. 순위만 보여주면 없는 선호가 있는 것처럼 읽힌다.
                Text(
                    text = "아직 차이가 뚜렷하지 않아요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        pref.values.forEach { value ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // 표본 수는 값을 한정하는 말이므로 값 옆에 둔다. 오른쪽 끝은 FAB가 떠 있는
                // 자리라, 거기 두면 화면 아래쪽에서 잘린다(에뮬레이터에서 확인된 결함).
                Text(
                    text = "${DrinkLabels.tagValue(pref.category, value.value, drinkType)} · ${value.samples}잔",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "%.1f점".format(value.averageRating),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TraitStatusRow(pref: TraitPreference) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(DrinkLabels.trait(pref.trait), style = MaterialTheme.typography.bodyLarge)

        when (traitStatus(pref)) {
            TraitStatus.Resolved ->
                AssistChip(
                    onClick = {},
                    label = { Text(DrinkLabels.preference(pref.trait, pref.preference!!)) },
                    colors =
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                )

            // 결핍이 아니라 결론이다. "아직"이라고 말하지 않는다.
            TraitStatus.Neutral ->
                Text(
                    text = "크게 가리지 않으세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )

            TraitStatus.NeedsRecords ->
                Text(
                    text = "${recordsNeeded(pref)}개 더 필요해요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
        }
    }
}

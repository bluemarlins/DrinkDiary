package com.bluemarlin.drinkdiary.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TraitPreference
import com.bluemarlin.drinkdiary.domain.model.TypeScope

// F3 — 문장 우선, 차트는 보조(software-architecture.md 6절).
// N개 미만이어도 화면을 비우지 않고 "아직 이르다"와 근거를 함께 보여준다(prd.md F3).
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onScopeChange: (TypeScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
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
        val answeredSpecific =
            specific.filter { it.highSamples + it.lowSamples + it.unsureSamples > 0 }
        if (answeredSpecific.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("추가로 기록한 축", style = MaterialTheme.typography.titleMedium)
                answeredSpecific.forEach { pref -> TraitStatusRow(pref) }
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = readiness.type.code,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(TasteTypeCopy.shortName(readiness.type), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = TasteTypeCopy.sentence(readiness.type) + ".",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "기록 ${recordCount}개를 바탕으로 판정했어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("아직 취향을 판단하기엔 일러요", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text =
                        if (recordCount == 0) {
                            "첫 기록을 남기면 여기서 취향이 보이기 시작해요."
                        } else {
                            "지금까지 ${recordCount}개를 기록했어요. 아래에서 축별로 얼마나 남았는지 볼 수 있어요."
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Text(TasteTypeCopy.traitLabel(pref.trait), style = MaterialTheme.typography.bodyLarge)

        when (val status = traitStatus(pref)) {
            TraitStatus.Resolved -> {
                AssistChip(
                    onClick = {},
                    label = { Text(TasteTypeCopy.poleLabel(pref.trait, pref.direction!!)) },
                    colors =
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                )
            }

            TraitStatus.NeedsSamples -> {
                Text(
                    text = "적어도 ${minimumRecordsNeeded(pref)}개는 더 필요해요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }

            TraitStatus.NeedsClearerGap -> {
                Text(
                    text = "판단 보류 — 아직 뚜렷하지 않아요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }

            TraitStatus.MostlyUnsure -> {
                Text(
                    text = "아직 잘 느껴지지 않는 축이에요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

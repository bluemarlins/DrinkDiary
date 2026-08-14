package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDUriImage

@Composable
fun RecordDetailScreen(
    record: DrinkRecord?,
    onDelete: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    if (record == null) {
        // 삭제 직후에도 잠깐 지나가는 상태다. "없어졌다"고 단정하지 않는다.
        Column(modifier = modifier.fillMaxWidth().padding(contentPadding).padding(20.dp)) {
            Text("기록을 불러오는 중이에요.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    var confirming by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        record.imageUri?.let {
            DDUriImage(
                imageUri = it,
                contentDescription = "${record.name} 사진",
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(MaterialTheme.shapes.medium),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(record.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = DrinkLabels.subtitle(record),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DetailRow("만족도", "%.0f / 5".format(record.rating))
        DetailRow("다시 살까", DrinkLabels.collectionStatus(record.collectionStatus))
        record.price?.let { DetailRow("가격", DrinkLabels.price(it)) }
        record.place?.let { DetailRow("어디에서", it) }

        record.memo?.let {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("메모", style = MaterialTheme.typography.labelLarge)
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }

        HorizontalDivider()

        Text("그때 남긴 취향", style = MaterialTheme.typography.titleMedium)
        if (record.taste.answers.isEmpty()) {
            Text(
                text = "취향 입력이 없는 기록이에요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // 저장된 순서가 아니라 축 순서로 보여준다 — 기록마다 순서가 달라지면 비교가 안 된다.
            Trait.entries.forEach { trait ->
                record.taste[trait]?.let { answer ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(DrinkLabels.trait(trait), style = MaterialTheme.typography.bodyLarge)
                        AssistChip(onClick = {}, label = { Text(DrinkLabels.answer(trait, answer)) })
                    }
                }
            }
        }

        HorizontalDivider()

        OutlinedButton(
            onClick = { confirming = true },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("이 기록 지우기", color = MaterialTheme.colorScheme.error) }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("${record.name} 기록을 지울까요?") },
            // 되돌릴 수 없다는 사실을 누르기 전에 말한다.
            text = { Text("취향 답까지 함께 지워지고, 되돌릴 수 없어요. 취향 유형도 이 기록만큼 다시 계산됩니다.") },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    onDelete()
                }) { Text("지우기", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) { Text("그대로 두기") }
            },
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = Color.Unspecified)
    }
}

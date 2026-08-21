package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDChip
import com.bluemarlin.drinkdiary.ui.component.DDPrimaryButton
import com.bluemarlin.drinkdiary.ui.component.DDSecondaryButton
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin

@Composable
fun RecordOptionalDetailStep(
    type: DrinkType,
    form: RecordForm,
    alwaysAskTags: Set<TagCategory>,
    onFormChange: (RecordForm) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    saving: Boolean,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
) {
    val folded = remainingTags(type)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(LocalDDScreenMargin.current),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("추가 정보 남기기", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "선택 입력 항목입니다. 작성하지 않고 바로 저장할 수 있어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (type) {
            DrinkType.Wine -> {
                OutlinedTextField(
                    value = form.vintage,
                    onValueChange = { onFormChange(form.copy(vintage = it.filter { ch -> ch.isDigit() })) },
                    label = { Text("빈티지 (생산 연도)") },
                    placeholder = { Text("예: 2020") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            DrinkType.Whiskey -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("어떻게 드셨나요?", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ServingStyle.entries.forEach { style ->
                            DDChip(
                                label = DrinkLabels.servingStyle(style),
                                selected = form.servingStyle == style,
                                onClick = {
                                    val next = if (form.servingStyle == style) null else style
                                    onFormChange(form.copy(servingStyle = next))
                                },
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = form.price,
            onValueChange = { onFormChange(form.copy(price = it.filter { ch -> ch.isDigit() })) },
            label = { Text("가격 (원)") },
            placeholder = { Text("예: 50000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = form.place,
            onValueChange = { onFormChange(form.copy(place = it)) },
            label = { Text("구매처 / 마신 곳") },
            placeholder = { Text("예: 이마트, 와인바, 집") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = form.memo,
            onValueChange = { onFormChange(form.copy(memo = it)) },
            label = { Text("메모") },
            placeholder = { Text("함께 먹은 음식, 분위기, 특별한 인상 등") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth(),
        )

        TagPicker(
            type = type,
            tags = form.tags,
            categories = folded,
            onTagsChange = { onFormChange(form.copy(tags = it)) },
            title = "추가 태그",
        )

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DDSecondaryButton(
                text = "이전",
                onClick = onBack,
                modifier = Modifier.weight(1f),
            )
            DDPrimaryButton(
                text = if (saving) "저장 중..." else "기록 완료하기",
                onClick = onSave,
                enabled = form.isSavable && !saving,
                modifier = Modifier.weight(1.5f),
            )
        }
    }
}

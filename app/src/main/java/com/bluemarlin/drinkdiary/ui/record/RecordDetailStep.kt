package com.bluemarlin.drinkdiary.ui.record

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDPrimaryButton
import com.bluemarlin.drinkdiary.ui.component.DDUriImage

// 취향 입력 이후 단계. 이름과 만족도만 필수이고 나머지는 접어둔다 —
// 기본 경로를 무겁게 만들면 F2에서 지킨 마찰 예산이 여기서 무너진다.
@Composable
fun RecordDetailStep(
    type: DrinkType,
    form: RecordForm,
    alwaysAskTags: Set<TagCategory>,
    onFormChange: (RecordForm) -> Unit,
    onSave: () -> Unit,
    saving: Boolean,
    modifier: Modifier = Modifier,
    // 편집 화면도 이 폼을 그대로 쓴다. 폼을 복제하면 저장 규칙이 한쪽에서만 지켜진다.
    saveLabel: String = "저장",
    // 편집에서만 채워지는 자리. 작성 경로는 마법사가 이미 취향을 물었으므로 비어 있다 —
    // 같은 것을 두 번 물으면 F2의 탭 예산이 무너진다.
    tasteSection: @Composable () -> Unit = {},
) {
    // 사용자가 "매번 물어봐 달라"고 고른 것은 접지 않고 늘 보인다.
    val (always, folded) = remainingTags(type).partition { it in alwaysAskTags }
    var expanded by remember { mutableStateOf(false) }
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) onFormChange(form.copy(imageUri = uri.toString()))
        }

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("무엇이었나요?", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = form.name,
            onValueChange = { onFormChange(form.copy(name = it)) },
            label = { Text("이름") },
            singleLine = true,
            isError = form.name.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )

        Text("얼마나 좋았나요?", style = MaterialTheme.typography.titleMedium)
        RatingPicker(
            rating = form.rating,
            onRatingChange = { onFormChange(form.copy(rating = it)) },
        )

        Text("다시 살 건가요?", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CollectionStatus.entries.forEach { status ->
                FilterChip(
                    selected = form.collectionStatus == status,
                    onClick = { onFormChange(form.copy(collectionStatus = status)) },
                    label = { Text(DrinkLabels.collectionStatus(status)) },
                )
            }
        }

        tasteSection()

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            ) { Text(if (form.imageUri == null) "사진 넣기" else "사진 바꾸기") }

            form.imageUri?.let {
                DDUriImage(
                    imageUri = it,
                    contentDescription = "선택한 사진",
                    modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.small),
                )
            }
        }

        TagPicker(
            type = type,
            tags = form.tags,
            categories = always,
            onTagsChange = { onFormChange(form.copy(tags = it)) },
            title = null,
        )

        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "덜 남기기" else "더 남기기 (선택)")
        }

        if (expanded) {
            OptionalFields(
                type = type,
                form = form,
                foldedTags = folded,
                onFormChange = onFormChange,
            )
        }

        DDPrimaryButton(
            text = if (saving) "저장 중" else saveLabel,
            onClick = onSave,
            enabled = form.isSavable && !saving,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RatingPicker(
    rating: Double,
    onRatingChange: (Double) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { value ->
            val selected = rating >= value
            val containerColor =
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            val contentColor =
                if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            val borderColor =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }

            Card(
                onClick = { onRatingChange(value.toDouble()) },
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors =
                    CardDefaults.cardColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                border = BorderStroke(1.dp, borderColor),
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionalFields(
    type: DrinkType,
    form: RecordForm,
    foldedTags: List<TagCategory>,
    onFormChange: (RecordForm) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TagPicker(
            type = type,
            tags = form.tags,
            categories = foldedTags,
            onTagsChange = { onFormChange(form.copy(tags = it)) },
        )

        // P4 — 같은 이름이 같은 맛을 보장하지 않게 만드는 변수들
        if (type == DrinkType.Wine) {
            OutlinedTextField(
                value = form.vintage,
                onValueChange = { onFormChange(form.copy(vintage = it.filter(Char::isDigit).take(4))) },
                label = { Text("빈티지") },
                singleLine = true,
                keyboardOptions =
                    androidx.compose.foundation.text
                        .KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text("어떻게 마셨나요?", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ServingStyle.entries.forEach { style ->
                    FilterChip(
                        selected = form.servingStyle == style,
                        onClick = { onFormChange(form.copy(servingStyle = style)) },
                        label = { Text(DrinkLabels.servingStyle(style)) },
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.price,
            onValueChange = { onFormChange(form.copy(price = it.filter(Char::isDigit))) },
            label = { Text("가격") },
            singleLine = true,
            keyboardOptions =
                androidx.compose.foundation.text
                    .KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.place,
            onValueChange = { onFormChange(form.copy(place = it)) },
            label = { Text("어디에서") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.memo,
            onValueChange = { onFormChange(form.copy(memo = it)) },
            label = { Text("메모") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// 라벨은 ui.DrinkLabels 한 곳에서 온다 — 기록 화면과 컬렉션 화면이 같은 것을 다르게 부르지 않도록.

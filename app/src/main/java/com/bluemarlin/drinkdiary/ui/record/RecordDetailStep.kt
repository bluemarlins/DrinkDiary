package com.bluemarlin.drinkdiary.ui.record

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.ui.component.DDUriImage

// 취향 입력 이후 단계. 이름과 만족도만 필수이고 나머지는 접어둔다 —
// 기본 경로를 무겁게 만들면 F2에서 지킨 마찰 예산이 여기서 무너진다.
@Composable
fun RecordDetailStep(
    type: DrinkType,
    form: RecordForm,
    onFormChange: (RecordForm) -> Unit,
    onSave: () -> Unit,
    saving: Boolean,
    modifier: Modifier = Modifier,
) {
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
                    label = { Text(statusLabel(status)) },
                )
            }
        }

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
                    modifier = Modifier.size(64.dp),
                )
            }
        }

        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "덜 남기기" else "더 남기기 (선택)")
        }

        if (expanded) {
            OptionalFields(type = type, form = form, onFormChange = onFormChange)
        }

        Button(
            onClick = onSave,
            enabled = form.isSavable && !saving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(if (saving) "저장 중" else "저장") }
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
            Card(
                onClick = { onRatingChange(value.toDouble()) },
                modifier = Modifier.size(56.dp),
                colors =
                    androidx.compose.material3.CardDefaults.cardColors(
                        containerColor =
                            if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                    ),
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun OptionalFields(
    type: DrinkType,
    form: RecordForm,
    onFormChange: (RecordForm) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        label = { Text(servingLabel(style)) },
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

private fun statusLabel(status: CollectionStatus): String =
    when (status) {
        CollectionStatus.Normal -> "그냥 그래요"
        CollectionStatus.Repurchase -> "또 살래요"
        CollectionStatus.NotForMe -> "안 맞아요"
    }

private fun servingLabel(style: ServingStyle): String =
    when (style) {
        ServingStyle.Neat -> "니트"
        ServingStyle.OnTheRocks -> "온더락"
        ServingStyle.WithWater -> "물 타서"
        ServingStyle.Highball -> "하이볼"
    }

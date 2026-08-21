package com.bluemarlin.drinkdiary.ui.record

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDChip
import com.bluemarlin.drinkdiary.ui.component.DDPhotoField
import com.bluemarlin.drinkdiary.ui.component.DDPrimaryButton
import com.bluemarlin.drinkdiary.ui.component.DDRatingInput
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin

@Composable
fun DrinkBasicInfoStep(
    imageUri: String?,
    name: String,
    rating: Double,
    collectionStatus: CollectionStatus,
    onPhotoPicked: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onRatingChange: (Double) -> Unit,
    onCollectionStatusChange: (CollectionStatus) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) onPhotoPicked(uri.toString())
        }
    val pickPhoto = {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(LocalDDScreenMargin.current),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DDPhotoField(imageUri = imageUri, onPick = pickPhoto)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("무엇이었나요?", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("술 이름") },
                placeholder = { Text("예: 샤또 마고, 발베니 12년") },
                singleLine = true,
                isError = name.isBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("얼마나 좋았나요?", style = MaterialTheme.typography.titleMedium)
            DDRatingInput(
                rating = rating,
                onRatingChange = onRatingChange,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("또 살래요?", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CollectionStatus.entries.forEach { status ->
                    DDChip(
                        label = DrinkLabels.collectionStatus(status),
                        selected = collectionStatus == status,
                        onClick = { onCollectionStatusChange(status) },
                    )
                }
            }
        }

        DDPrimaryButton(
            text = "다음: 맛 기록하기",
            onClick = onNext,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

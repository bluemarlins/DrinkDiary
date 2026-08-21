package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDChip
import com.bluemarlin.drinkdiary.ui.component.DDPrimaryButton
import com.bluemarlin.drinkdiary.ui.component.DDRatingInput
import com.bluemarlin.drinkdiary.ui.component.DDSecondaryButton
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin

@Composable
fun DrinkRatingStep(
    rating: Double,
    collectionStatus: CollectionStatus,
    onRatingChange: (Double) -> Unit,
    onCollectionStatusChange: (CollectionStatus) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LocalDDScreenMargin.current),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("얼마나 좋으셨나요?", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "별점과 재구매 의사는 취향 분석의 중요한 기준이 돼요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("만족도", style = MaterialTheme.typography.titleMedium)
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

        Spacer(modifier = Modifier.height(16.dp))

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
                text = "다음: 맛 기록하기",
                onClick = onNext,
                enabled = rating > 0.0,
                modifier = Modifier.weight(1.5f),
            )
        }
    }
}

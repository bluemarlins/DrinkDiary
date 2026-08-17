package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 비선호를 오류 색으로 칠하지 않는다. 잘못된 상태가 아니라 **취향이 아니었다**는 뜻이고,
// 부정 신호는 눈에 띄기만 하면 되지 소리칠 필요가 없다.
@Composable
private fun statusContainerColor(status: CollectionStatus) =
    when (status) {
        CollectionStatus.Repurchase -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

@Composable
private fun statusContentColor(status: CollectionStatus) =
    when (status) {
        CollectionStatus.Repurchase -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

// 명세 5.3절 `DDDrinkRecordCard`.
//
// M3의 `Card(onClick = ...)`은 롱프레스를 노출하지 않는다. 그래서 클릭 없는 Card에
// `combinedClickable`을 건다 — 선택 모드 진입이 롱프레스이기 때문이다(prd.md F1-2).
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DDDrinkRecordCard(
    record: DrinkRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick =
                        onLongClick?.let { action ->
                            {
                                // 선택 모드로 들어간 것을 화면을 보지 않고도 알아야 한다.
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                action()
                            }
                        },
                ),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        border =
            BorderStroke(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(DrinkDiarySpacing.md),
            horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            record.imageUri?.let {
                // **기록 폼·대시보드와 같은 4:5다**(design-system.md 5.3).
                // 여기만 정사각(56dp)이었는데, 컬렉션이 사진을 가장 많이 보는 자리다 —
                // 기록 하나당 대시보드에서는 많아야 한 번, 목록에서는 스크롤할 때마다 본다.
                // 세로로 찍은 병을 여기서만 자르면 **라벨 위아래가 사라진다**(prd.md F1-3).
                DDUriImage(
                    imageUri = it,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(width = 48.dp, height = 60.dp)
                            .clip(MaterialTheme.shapes.small),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xxs),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xxs),
                ) {
                    // 선택을 색으로만 표시하지 않는다. 배경색 차이는 명도 대비가 낮고,
                    // 색만으로 상태를 말하면 색각 이상에서 무엇이 선택됐는지 알 수 없다.
                    if (selected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = "선택됨",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = record.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                }
                Text(
                    text = DrinkLabels.subtitle(record),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )

                // 이 줄이 있어야 목록을 훑어서 "다음에 뭘 살까"를 판단할 수 있다.
                //
                // 이전에는 재구매만 `★` 한 글자로 표시하고 비선호는 **아무것도 그리지 않았다.**
                // 그러면 빈칸이 "그냥 그래요"와 "안 맞아요"를 같은 것으로 만든다 — 구매 판단에서
                // 가장 중요한 신호가 사라진다. 같은 축은 같은 방식으로 적는다.
                //
                // '그냥 그래요'는 여전히 안 그린다. 모든 행에 붙으면 소음이고, 아무 표시가 없는 것이
                // 그 자체로 "특별할 것 없었다"는 뜻으로 읽힌다.
                if (record.collectionStatus != CollectionStatus.Normal) {
                    DDSemanticBadge(
                        text = DrinkLabels.collectionStatus(record.collectionStatus),
                        containerColor = statusContainerColor(record.collectionStatus),
                        contentColor = statusContentColor(record.collectionStatus),
                    )
                }
            }

            // **만족도 숫자에 액센트를 쓰지 않는다**(design-system.md 2절 1번).
            // 이 숫자는 **모든 행에** 있어서, 여덟 행이면 브랜드색이 여덟 번 찍힌다 —
            // 그러면 강조가 강조로 읽히지 않는다. 액센트는 횟수로 센다.
            //
            // 크기(`titleLarge`)가 이미 강조를 맡고 있어 색까지 줄 필요가 없다. 이 행에서
            // 브랜드색을 갖는 것은 재구매 뱃지 하나이며, 그것이 매장에서 3초 만에 확인해야
            // 하는 신호다(5.3 `DDRepurchaseBadge`).
            Text(
                text = DrinkLabels.rating(record.rating),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

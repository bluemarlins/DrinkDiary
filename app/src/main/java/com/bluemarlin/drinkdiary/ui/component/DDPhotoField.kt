package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// **병은 세로로 길다.** 가로 배너로 자르면 라벨이 잘리고, 라벨은 이 앱에서 사진을 찍는 이유 그 자체다.
// 그래서 4:5 세로다(prd.md F1-3).
private const val BOTTLE_ASPECT = 4f / 5f

// 폭을 다 쓰지 않는 이유는 자리다툼이다. 전체폭 4:5는 화면의 절반을 넘겨서 이름·만족도가
// 첫 화면에서 사라진다. 사진을 앞에 두는 것과 사진만 보이게 하는 것은 다르다.
private const val FIELD_WIDTH_FRACTION = 0.66f

// 기록 폼의 첫 자리(prd.md F1-3). **빈 상태와 채워진 상태의 크기가 같다** —
// 사진을 넣었다고 아래 필드가 밀려 내려가면 사용자가 방금 뭘 눌렀는지 놓친다.
//
// 저장을 막지 않으므로 이 자리는 관문이 아니다. 비어 있음이 보이기만 하면 된다 —
// 자리를 보여주는 것과 나무라는 것은 다르다(branding.md 2-1 '초보자 훈계 금지').
@Composable
fun DDPhotoField(
    imageUri: String?,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs),
    ) {
        Surface(
            onClick = onPick,
            modifier =
                Modifier
                    .fillMaxWidth(FIELD_WIDTH_FRACTION)
                    .aspectRatio(BOTTLE_ASPECT)
                    .semantics {
                        contentDescription = if (imageUri == null) "사진 넣기" else "사진 바꾸기"
                    },
            shape = MaterialTheme.shapes.large,
            color =
                if (imageUri == null) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surface
                },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            if (imageUri == null) {
                EmptySlot()
            } else {
                DDUriImage(
                    imageUri = imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // 사진이 있을 때만 글자를 둔다. 빈 상태는 칸 자체가 이미 "누르세요"라고 말하고 있어서
        // 아래에 같은 말을 또 쓰면 그게 텍스트 과다다.
        if (imageUri != null) {
            TextButton(onClick = onPick) { Text("사진 바꾸기") }
        }
    }
}

@Composable
private fun EmptySlot() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "사진 넣기",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

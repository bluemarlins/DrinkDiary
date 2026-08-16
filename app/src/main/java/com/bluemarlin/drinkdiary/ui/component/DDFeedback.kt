package com.bluemarlin.drinkdiary.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 명세 5.4절 Feedback Components. 되돌릴 수 없는 액션의 확인 창은 화면마다 다시 조립할 것이
// 아니다 — 모양(ShapeLarge)과 "위험한 쪽이 error 색"이라는 규칙이 한곳에 있어야 한다.
@Composable
fun DDConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

// CTA는 `actionLabel`과 `onAction`이 **둘 다** 있을 때만 그린다. 누를 것이 없는 버튼을
// 빈 화면에 놓으면 사용자는 앱이 고장난 줄 안다.
@Composable
fun DDEmptyContent(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(DrinkDiarySpacing.xl))
            DDPrimaryButton(
                text = actionLabel,
                onClick = onAction,
            )
        }
    }
}

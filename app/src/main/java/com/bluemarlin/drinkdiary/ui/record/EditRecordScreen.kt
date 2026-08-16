package com.bluemarlin.drinkdiary.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin

// 기록 작성과 **같은 폼**을 쓴다. 취향 편집만 슬롯으로 끼워 넣는다 —
// 작성 경로는 마법사가 이미 물었으므로 그 자리를 비워 둔다.
@Composable
fun EditRecordScreen(
    state: EditUiState,
    onFormChange: (RecordForm) -> Unit,
    onAnswer: (Trait, TraitAnswer) -> Unit,
    onSave: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    if (!state.loaded) {
        Column(modifier = modifier.fillMaxWidth().padding(contentPadding).padding(LocalDDScreenMargin.current)) {
            Text("기록을 불러오는 중이에요.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    if (state.missing) {
        // 목록에서 이미 지워졌을 수 있다. 빈 폼을 보여주면 저장할 때 **새 기록이 생긴다.**
        Column(modifier = modifier.fillMaxWidth().padding(contentPadding).padding(LocalDDScreenMargin.current)) {
            Text("이 기록을 찾을 수 없어요.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    RecordDetailStep(
        type = state.type,
        form = state.form,
        alwaysAskTags = state.alwaysAskTags,
        onFormChange = onFormChange,
        onSave = onSave,
        saving = state.saving,
        modifier = modifier.padding(contentPadding),
        saveLabel = "고친 내용 저장",
        tasteSection = {
            HorizontalDivider()
            TasteEditor(taste = state.taste, onAnswer = onAnswer)
        },
    )
}

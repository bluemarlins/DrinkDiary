package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.ui.component.DDDrinkRecordCard
import com.bluemarlin.drinkdiary.ui.component.DDEmptyContent
import com.bluemarlin.drinkdiary.ui.navigation.DDBottomNavigationBarHeight
import com.bluemarlin.drinkdiary.ui.navigation.LocalDDScreenMargin
import com.bluemarlin.drinkdiary.ui.theme.DrinkDiarySpacing

// 매장 조회(`../../../../../../docs/specs/planner/prd.md` F5). **이 제품의 존재 이유가 걸린 화면이다**(prd.md S3).
//
// F5가 요구하는 세 가지를 그대로 따른다.
//
// - **오프라인에서 동작한다** — 이미 메모리에 올라온 기록을 거르기만 하므로 네트워크도 새 조회도 없다.
// - **이름 일부로 찾는다** — 앞글자 일치가 아니라 부분 일치다. 진열대에서 기억나는 건 보통
//   앞부분이 아니라 가운데 한 조각이다("몽페라", "12년").
// - **진입에서 결과까지 화면 전환이 최소화된다** — 그래서 검색 결과가 별도 화면으로 넘어가지 않고
//   입력창 바로 아래에 즉시 깔린다. 확인 버튼도 없다. 매장에서는 한 번의 전환도 비싸다.
//
// **"좋아했는지"는 `DDDrinkRecordCard`가 이미 답한다** — 만족도 숫자와 재구매 뱃지가 그 자리에 있다.
// 여기서 새 표현을 만들지 않는 이유이기도 하다. 같은 사실을 두 가지 모양으로 보여주면
// 매장에서 판단이 한 박자 늦는다.
@Composable
fun SearchScreen(
    records: List<DrinkRecord>,
    onOpen: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // 탭을 옮겼다 돌아와도 방금 친 이름이 남아 있어야 한다. 진열대 앞에서 다시 치게 하면
    // F5의 "전환 최소화"가 무너진다.
    var query by rememberSaveable { mutableStateOf("") }

    val trimmed = query.trim()
    val matches =
        if (trimmed.isEmpty()) {
            emptyList()
        } else {
            records.filter { it.name.contains(trimmed, ignoreCase = true) }
        }

    // 인셋은 바깥 padding이 아니라 각 요소가 받는다 — 콘텐츠가 플로팅 바 뒤로 흘러야 한다.
    //
    // **입력창은 목록 안으로 넣지 않는다.** 매장에서 이름을 고쳐 치는 화면이라(F5) 스크롤하다
    // 입력창이 사라지면 다시 위로 올라와야 한다 — 그게 F5가 없애려던 전환 비용이다.
    // 그래서 여기서 바 뒤로 흐르는 것은 결과 목록이고, 입력창은 그 위에 남는다.
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            label = { Text("이름 일부") },
            placeholder = { Text("예: 몽페라, 12년") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = contentPadding.calculateTopPadding())
                    .padding(
                        horizontal = LocalDDScreenMargin.current,
                        vertical = DrinkDiarySpacing.sm,
                    ),
        )

        when {
            // 빈 입력은 "결과 없음"이 아니다. 아직 묻지 않은 상태를 못 찾은 상태로 말하면
            // 사용자는 기록이 사라진 줄 안다.
            trimmed.isEmpty() ->
                DDEmptyContent(
                    title = "찾을 이름을 입력해 주세요",
                    description = "기억나는 한 조각이면 돼요. 앞글자가 아니어도 찾아요.",
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = LocalDDScreenMargin.current),
                )

            matches.isEmpty() ->
                // 없다는 것도 매장에서는 쓸모 있는 답이다 — "안 마셔본 것"이라는 뜻이므로
                // 나무라지 않고 사실만 말한다(branding.md 2-1).
                DDEmptyContent(
                    title = "\"$trimmed\"로 남긴 기록이 없어요",
                    description = "아직 안 마셔본 술이거나, 다른 이름으로 적어 두셨을 수 있어요.",
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = LocalDDScreenMargin.current),
                )

            else -> {
                Text(
                    text = "${matches.size}개 찾았어요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier.padding(
                            horizontal = LocalDDScreenMargin.current,
                            vertical = DrinkDiarySpacing.xxs,
                        ),
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start = LocalDDScreenMargin.current,
                            end = LocalDDScreenMargin.current,
                            top = DrinkDiarySpacing.xs,
                            bottom = DDBottomNavigationBarHeight + LocalDDScreenMargin.current,
                        ),
                    verticalArrangement = Arrangement.spacedBy(DrinkDiarySpacing.sm),
                ) {
                    // 선택 모드를 여기에 두지 않는다. 지우러 오는 화면이 아니라
                    // 살지 말지 정하러 오는 화면이다(F5).
                    items(matches, key = { it.id }) { record ->
                        DDDrinkRecordCard(record = record, onClick = { onOpen(record.id) })
                    }
                }
            }
        }
    }
}

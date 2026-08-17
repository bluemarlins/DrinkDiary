package com.bluemarlin.drinkdiary.ui.profile

import com.bluemarlin.drinkdiary.domain.model.DrinkHighlight
import com.bluemarlin.drinkdiary.domain.model.HighlightKind
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.component.DDHighlightCard

// 하이라이트 카드의 문구(prd.md F3-4 (a)).
//
// **레이블은 어미를 갖지 않는다**(branding.md 2-3). 그리고 "최고"·"베스트" 같은 등급어를 쓰지
// 않는다 — 남과 겨룬 결과가 아니라 내 기록 안에서의 사실이다.
object DrinkHighlightCopy {
    fun card(highlight: DrinkHighlight): DDHighlightCard =
        DDHighlightCard(
            id = highlight.record.id,
            label = label(highlight.kind),
            name = highlight.record.name,
            detail = detail(highlight),
            imageUri = highlight.record.imageUri,
        )

    private fun label(kind: HighlightKind): String =
        when (kind) {
            HighlightKind.TopRated -> "가장 높게 준"
            HighlightKind.MostRepeated -> "여러 번 마신"
            HighlightKind.Latest -> "가장 최근"
        }

    private fun detail(highlight: DrinkHighlight): String =
        when (highlight.kind) {
            HighlightKind.TopRated -> DrinkLabels.rating(highlight.record.rating)
            HighlightKind.MostRepeated -> "${highlight.repeatCount}번"
            HighlightKind.Latest -> DrinkLabels.date(highlight.record.recordedAtMillis)
        }
}

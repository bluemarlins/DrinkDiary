package com.bluemarlin.drinkdiary.ui.record

import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.Origin
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EditRecordTest {
    private val original =
        DrinkRecord(
            id = 7L,
            type = DrinkType.Whiskey,
            name = "Ardbeg 10",
            servingStyle = ServingStyle.Neat,
            taste =
                TasteInput()
                    .with(Trait.Body, TraitAnswer.High)
                    // 편집 화면이 보여주지 않는 고유 축.
                    .with(Trait.Peat, TraitAnswer.High),
            tags = DrinkTags(origin = Origin.Scotland),
            rating = 4.0,
            collectionStatus = CollectionStatus.Repurchase,
            price = 90_000L,
            place = "집",
            memo = "재구매",
            recordedAtMillis = 1_000L,
        )

    private fun form() = RecordForm.of(original)

    @Test
    fun `the drinking time is not the editing time`() {
        val edited = original.applying(form().copy(name = "Ardbeg Ten"), original.taste)

        assertEquals(1_000L, edited.recordedAtMillis)
        assertEquals(7L, edited.id)
    }

    // 편집 화면은 공통 축만 보여준다. 저장이 답을 통째로 갈아끼우므로, 안 보이는 축의 답이
    // 여기서 빠지면 **영영 사라진다** — 사용자는 건드린 적도 없는데.
    @Test
    fun `an answer the editor never shows is not dropped`() {
        val taste = original.taste.with(Trait.Body, TraitAnswer.Low)
        val edited = original.applying(form(), taste)

        assertEquals(TraitAnswer.Low, edited.taste[Trait.Body])
        assertEquals(TraitAnswer.High, edited.taste[Trait.Peat])
    }

    @Test
    fun `the form round-trips every field it owns`() {
        val edited = original.applying(form(), original.taste)

        assertEquals(original, edited)
    }

    @Test
    fun `blank optional text becomes null rather than an empty string`() {
        val edited = original.applying(form().copy(place = "  ", memo = "", price = ""), original.taste)

        assertNull(edited.place)
        assertNull(edited.memo)
        assertNull(edited.price)
    }

    @Test
    fun `whitespace around the name is trimmed`() {
        val edited = original.applying(form().copy(name = "  Ardbeg 10  "), original.taste)

        assertEquals("Ardbeg 10", edited.name)
    }

    // 와인에는 음용 방법을 묻지 않는다. 주종을 바꾸는 편집은 없으므로 원본 주종을 그대로 쓴다.
    @Test
    fun `a wine keeps no serving style even if the form carries one`() {
        val wine = original.copy(type = DrinkType.Wine, servingStyle = null)

        val edited = wine.applying(RecordForm.of(wine).copy(servingStyle = ServingStyle.Highball), wine.taste)

        assertNull(edited.servingStyle)
        assertEquals(DrinkType.Wine, edited.type)
    }
}

package com.bluemarlin.drinkdiary.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 한 기록에서 한 축의 답은 하나뿐이다. 이 불변식이 깨지면 **판정이 조용히 틀린다** —
// 선호 판정은 이 행들의 상관계수라 중복 한 줄이 계수를 끌어당기고, 에러는 나지 않는다.
// 그래서 유니크 인덱스로 DB가 직접 막는다. DAO의 "지우고 다시 넣기" 규율만으로는
// 그 규율을 지나치는 경로가 하나 생기는 순간 끝이다.
//
// recordId가 맨 앞이라 이 인덱스가 외래키 인덱스 역할까지 겸한다 — 따로 두지 않는다.
@Entity(
    tableName = "trait_answers",
    foreignKeys = [
        ForeignKey(
            entity = DrinkRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["recordId", "trait"], unique = true)],
)
data class TraitAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val recordId: Long,
    val trait: String,
    val answer: String,
)

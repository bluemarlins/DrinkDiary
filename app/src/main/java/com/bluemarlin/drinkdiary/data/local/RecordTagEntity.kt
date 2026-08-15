package com.bluemarlin.drinkdiary.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// TraitAnswerEntity와 같은 이유로 행 단위다 — 태그 집합이 아직 가설이라 컬럼으로 고정하면
// 태그를 하나 바꿀 때마다 스키마가 흔들린다(software-architecture.md 5절).
//
// 유니크 제약을 두는 이유도 같다. 한 기록에 같은 분류의 태그가 둘이면 어느 쪽이 맞는지
// 알 수 없고, 태그 선호는 값별 평균이라 중복이 그 값의 표본 수를 부풀린다.
@Entity(
    tableName = "record_tags",
    foreignKeys = [
        ForeignKey(
            entity = DrinkRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["recordId", "category"], unique = true)],
)
data class RecordTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val recordId: Long,
    val category: String,
    val value: String,
)

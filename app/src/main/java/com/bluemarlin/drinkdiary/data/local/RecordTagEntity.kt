package com.bluemarlin.drinkdiary.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// TraitAnswerEntity와 같은 이유로 행 단위다 — 태그 집합이 아직 가설이라 컬럼으로 고정하면
// 태그를 하나 바꿀 때마다 스키마가 흔들린다(software-architecture.md 5절).
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
    indices = [Index("recordId")],
)
data class RecordTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val recordId: Long,
    val category: String,
    val value: String,
)

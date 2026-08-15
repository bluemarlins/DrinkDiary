package com.bluemarlin.drinkdiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// 출시 전이라 마이그레이션을 쌓지 않고 version을 올린다(harness.md §1 예외).
// 설치본이 있는 기기는 AppContainer의 fallbackToDestructiveMigration으로 초기화된다.
@Database(
    entities = [DrinkRecordEntity::class, TraitAnswerEntity::class, RecordTagEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class DrinkDiaryDatabase : RoomDatabase() {
    abstract fun drinkRecordDao(): DrinkRecordDao
}

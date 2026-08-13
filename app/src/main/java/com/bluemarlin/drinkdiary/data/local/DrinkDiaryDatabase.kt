package com.bluemarlin.drinkdiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DrinkRecordEntity::class, TraitAnswerEntity::class], version = 1, exportSchema = true)
abstract class DrinkDiaryDatabase : RoomDatabase() {
    abstract fun drinkRecordDao(): DrinkRecordDao
}

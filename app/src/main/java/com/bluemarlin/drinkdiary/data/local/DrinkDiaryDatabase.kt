package com.bluemarlin.drinkdiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DrinkRecordEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class DrinkDiaryDatabase : RoomDatabase() {
    abstract fun drinkRecordDao(): DrinkRecordDao

    companion object {
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE drink_records_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            type TEXT NOT NULL,
                            name TEXT NOT NULL,
                            imageUri TEXT,
                            price INTEGER,
                            place TEXT,
                            tastingNote TEXT,
                            rating REAL NOT NULL,
                            detailRating1 REAL NOT NULL,
                            detailRating2 REAL NOT NULL,
                            detailRating3 REAL NOT NULL,
                            detailRating4 REAL NOT NULL,
                            collectionStatus TEXT NOT NULL,
                            recordedAtMillis INTEGER NOT NULL,
                            createdAtMillis INTEGER NOT NULL,
                            updatedAtMillis INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        """
                        INSERT INTO drink_records_new (
                            id, type, name, imageUri, price, place, tastingNote,
                            rating, detailRating1, detailRating2, detailRating3, detailRating4,
                            collectionStatus, recordedAtMillis, createdAtMillis, updatedAtMillis
                        )
                        SELECT
                            id, type, name, imageUri, price, place, tastingNote,
                            rating * 1.0, rating * 1.0, rating * 1.0, rating * 1.0, rating * 1.0,
                            collectionStatus, recordedAtMillis, createdAtMillis, updatedAtMillis
                        FROM drink_records
                        """.trimIndent(),
                    )
                    db.execSQL("DROP TABLE drink_records")
                    db.execSQL("ALTER TABLE drink_records_new RENAME TO drink_records")
                    db.execSQL("CREATE INDEX index_drink_records_recordedAtMillis ON drink_records(recordedAtMillis)")
                    db.execSQL("CREATE INDEX index_drink_records_type ON drink_records(type)")
                    db.execSQL("CREATE INDEX index_drink_records_collectionStatus ON drink_records(collectionStatus)")
                    db.execSQL(
                        "CREATE INDEX index_drink_records_type_collectionStatus ON drink_records(type, collectionStatus)",
                    )
                    db.execSQL(
                        "CREATE INDEX index_drink_records_recordedAtMillis_collectionStatus ON drink_records(recordedAtMillis, collectionStatus)",
                    )
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE drink_records ADD COLUMN detailRating5 REAL NOT NULL DEFAULT 2.5")
                    db.execSQL(
                        """
                        UPDATE drink_records
                        SET
                            detailRating1 = ROUND(detailRating1 * 2.0) / 2.0,
                            detailRating2 = ROUND(detailRating2 * 2.0) / 2.0,
                            detailRating3 = ROUND(detailRating3 * 2.0) / 2.0,
                            detailRating4 = ROUND(detailRating4 * 2.0) / 2.0
                        """.trimIndent(),
                    )
                }
            }
    }
}

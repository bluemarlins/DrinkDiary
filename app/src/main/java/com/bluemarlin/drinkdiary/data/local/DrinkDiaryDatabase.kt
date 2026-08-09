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
        val MIGRATION_1_2 = object : Migration(1, 2) {
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
                db.execSQL("CREATE INDEX index_drink_records_type_collectionStatus ON drink_records(type, collectionStatus)")
                db.execSQL("CREATE INDEX index_drink_records_recordedAtMillis_collectionStatus ON drink_records(recordedAtMillis, collectionStatus)")
            }
        }

        /**
         * Adds tasting tags plus the ABV/volume fields that the drinking-amount insights need,
         * and drops the four per-criterion rating columns that tags replace.
         *
         * Unlike MIGRATION_1_2 this does not rebuild the table: minSdk 35 ships SQLite 3.44,
         * well past the 3.35 that introduced `ALTER TABLE ... DROP COLUMN`, and none of the
         * dropped columns participate in an index, so the simpler form is safe here and leaves
         * the five existing indexes untouched.
         *
         * The detail ratings are discarded rather than converted. There is no honest mapping
         * from a number to a flavor word — a 3.5 on 산도 is neither 새콤함 nor 시큼함 — and in
         * practice the four values are just copies of the representative rating, because
         * MIGRATION_1_2 seeded them that way and the detail screen only renders them when they
         * differ from it.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE drink_records ADD COLUMN tastingTags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE drink_records ADD COLUMN abv REAL")
                db.execSQL("ALTER TABLE drink_records ADD COLUMN volumeMl INTEGER")
                db.execSQL("ALTER TABLE drink_records DROP COLUMN detailRating1")
                db.execSQL("ALTER TABLE drink_records DROP COLUMN detailRating2")
                db.execSQL("ALTER TABLE drink_records DROP COLUMN detailRating3")
                db.execSQL("ALTER TABLE drink_records DROP COLUMN detailRating4")
            }
        }
    }
}

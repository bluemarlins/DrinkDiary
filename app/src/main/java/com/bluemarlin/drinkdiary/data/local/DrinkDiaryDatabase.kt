package com.bluemarlin.drinkdiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// v3부터 **실제 마이그레이션을 쌓는다.** v2까지는 재정의 중이라 스키마가 바뀌면 데이터를 버렸는데
// (fallbackToDestructiveMigration), 그 상태로 출시하면 업데이트마다 사용자 기록이 사라진다.
// 스키마가 이제 안정됐으므로 파괴적 폴백을 걷어냈다 — 되돌리려면 사용자 데이터를 버려도 되는
// 이유를 먼저 대야 한다.
@Database(
    entities = [DrinkRecordEntity::class, TraitAnswerEntity::class, RecordTagEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class DrinkDiaryDatabase : RoomDatabase() {
    abstract fun drinkRecordDao(): DrinkRecordDao
}

// 답·태그에 유니크 제약을 세운다.
//
// **인덱스를 만들기 전에 중복부터 지운다.** 지금 코드는 중복을 만들지 않지만, v2로 저장된 DB에
// 중복이 한 줄이라도 있으면 CREATE UNIQUE INDEX가 실패하고 **앱이 시작조차 못 한다.**
// 마이그레이션은 "우리 코드가 옳게 굴렀다면"이 아니라 **실제로 거기 있는 데이터**를 상대해야 한다.
// 남길 행은 id가 가장 큰 것 — 마지막에 쓴 값이 현재 값이다.
val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "DELETE FROM trait_answers WHERE id NOT IN " +
                    "(SELECT MAX(id) FROM trait_answers GROUP BY recordId, trait)",
            )
            db.execSQL("DROP INDEX IF EXISTS `index_trait_answers_recordId`")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_trait_answers_recordId_trait` " +
                    "ON `trait_answers` (`recordId`, `trait`)",
            )

            db.execSQL(
                "DELETE FROM record_tags WHERE id NOT IN " +
                    "(SELECT MAX(id) FROM record_tags GROUP BY recordId, category)",
            )
            db.execSQL("DROP INDEX IF EXISTS `index_record_tags_recordId`")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_record_tags_recordId_category` " +
                    "ON `record_tags` (`recordId`, `category`)",
            )
        }
    }

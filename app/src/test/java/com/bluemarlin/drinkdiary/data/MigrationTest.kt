package com.bluemarlin.drinkdiary.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.local.MIGRATION_2_3
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 마이그레이션은 **실제로 거기 있는 데이터**를 상대로 돌아야 한다. 그래서 v2 스키마를 손으로 세우고
// v2 시절 그대로의 행을 넣은 뒤 진짜 Room으로 연다.
//
// `room-testing`의 MigrationTestHelper를 쓰지 않은 이유는 라이브러리를 새로 들이지 않기 위해서다
// (harness.md §10). 대신 앱이 실제로 실행하는 경로 — Room이 user_version을 보고 onUpgrade를
// 태우는 그 경로 — 를 그대로 탄다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MigrationTest {
    private val dbName = "migration_test.db"
    private lateinit var context: Context
    private var migrated: DrinkDiaryDatabase? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        migrated?.close()
        context.deleteDatabase(dbName)
    }

    // 2.json에서 그대로 옮긴 v2 스키마. 여기를 손대면 더는 v2가 아니므로 테스트가 무의미해진다.
    private fun createV2(seed: (SupportSQLiteDatabase) -> Unit) {
        val callback =
            object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `drink_records` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, " +
                            "`name` TEXT NOT NULL, `vintage` INTEGER, `servingStyle` TEXT, " +
                            "`rating` REAL NOT NULL, `collectionStatus` TEXT NOT NULL, `imageUri` TEXT, " +
                            "`price` INTEGER, `place` TEXT, `memo` TEXT, `recordedAtMillis` INTEGER NOT NULL)",
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `trait_answers` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordId` INTEGER NOT NULL, " +
                            "`trait` TEXT NOT NULL, `answer` TEXT NOT NULL, " +
                            "FOREIGN KEY(`recordId`) REFERENCES `drink_records`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE )",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_trait_answers_recordId` ON `trait_answers` (`recordId`)",
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `record_tags` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordId` INTEGER NOT NULL, " +
                            "`category` TEXT NOT NULL, `value` TEXT NOT NULL, " +
                            "FOREIGN KEY(`recordId`) REFERENCES `drink_records`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE )",
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_record_tags_recordId` ON `record_tags` (`recordId`)")

                    // Room이 무결성 확인에 쓰는 표. 진짜 v2 DB에는 이게 있으므로 여기서도 세운다.
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS room_master_table " +
                            "(id INTEGER PRIMARY KEY, identity_hash TEXT)",
                    )
                    db.execSQL(
                        "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                            "VALUES(42, '2e4057ab17814738e8472db602a3138d')",
                    )
                    seed(db)
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int,
                ) = Unit
            }

        val helper =
            FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration
                    .builder(context)
                    .name(dbName)
                    .callback(callback)
                    .build(),
            )
        helper.writableDatabase.close()
        helper.close()
    }

    private fun openMigrated(): DrinkDiaryDatabase =
        Room
            .databaseBuilder(context, DrinkDiaryDatabase::class.java, dbName)
            .addMigrations(MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()
            .also {
                migrated = it
                it.openHelper.writableDatabase // 실제로 열어야 onUpgrade가 돈다
            }

    private fun DrinkDiaryDatabase.count(sql: String): Int =
        openHelper.readableDatabase.query(sql).use {
            it.moveToFirst()
            it.getInt(0)
        }

    @Test
    fun `v2 data survives the upgrade to v3`() {
        createV2 { db ->
            db.execSQL(
                "INSERT INTO drink_records (id, type, name, rating, collectionStatus, recordedAtMillis) " +
                    "VALUES (1, 'Wine', 'Barolo', 4.5, 'Normal', 100)",
            )
            db.execSQL("INSERT INTO trait_answers (recordId, trait, answer) VALUES (1, 'Body', 'High')")
            db.execSQL("INSERT INTO record_tags (recordId, category, value) VALUES (1, 'Origin', 'OldWorld')")
        }

        val db = openMigrated()

        assertEquals(1, db.count("SELECT COUNT(*) FROM drink_records"))
        assertEquals(1, db.count("SELECT COUNT(*) FROM trait_answers"))
        assertEquals(1, db.count("SELECT COUNT(*) FROM record_tags"))
    }

    // 이 케이스가 이 테스트의 존재 이유다. 중복을 먼저 지우지 않으면 CREATE UNIQUE INDEX가 실패하고
    // **앱이 켜지지 않는다.** "우리 코드는 중복을 안 만든다"는 마이그레이션의 전제가 될 수 없다.
    @Test
    fun `duplicate answers left by an older build are collapsed instead of blocking startup`() {
        createV2 { db ->
            db.execSQL(
                "INSERT INTO drink_records (id, type, name, rating, collectionStatus, recordedAtMillis) " +
                    "VALUES (1, 'Wine', 'Barolo', 4.5, 'Normal', 100)",
            )
            db.execSQL("INSERT INTO trait_answers (id, recordId, trait, answer) VALUES (1, 1, 'Body', 'Low')")
            db.execSQL("INSERT INTO trait_answers (id, recordId, trait, answer) VALUES (2, 1, 'Body', 'High')")
            db.execSQL("INSERT INTO record_tags (id, recordId, category, value) VALUES (1, 1, 'Origin', 'OldWorld')")
            db.execSQL("INSERT INTO record_tags (id, recordId, category, value) VALUES (2, 1, 'Origin', 'NewWorld')")
        }

        val db = openMigrated()

        assertEquals(1, db.count("SELECT COUNT(*) FROM trait_answers"))
        assertEquals(1, db.count("SELECT COUNT(*) FROM record_tags"))

        // 마지막에 쓴 값이 현재 값이다.
        val answer =
            db.openHelper.readableDatabase.query("SELECT answer FROM trait_answers").use {
                it.moveToFirst()
                it.getString(0)
            }
        assertEquals("High", answer)
    }

    @Test
    fun `the upgraded database rejects a second answer for the same trait`() {
        createV2 { db ->
            db.execSQL(
                "INSERT INTO drink_records (id, type, name, rating, collectionStatus, recordedAtMillis) " +
                    "VALUES (1, 'Wine', 'Barolo', 4.5, 'Normal', 100)",
            )
            db.execSQL("INSERT INTO trait_answers (recordId, trait, answer) VALUES (1, 'Body', 'High')")
        }

        val db = openMigrated()

        val failed =
            runCatching {
                db.openHelper.writableDatabase.execSQL(
                    "INSERT INTO trait_answers (recordId, trait, answer) VALUES (1, 'Body', 'Low')",
                )
            }.isFailure

        assertTrue("유니크 제약이 없으면 중복이 조용히 쌓여 판정을 왜곡한다", failed)
    }
}

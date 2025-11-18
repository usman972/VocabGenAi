package com.rameez.hel.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.utils.ApplicationClass

@Database(entities = [WIPModel::class], version = 2)
@TypeConverters(Converters::class)
abstract class WIPDatabase : RoomDatabase() {

    abstract fun wipDao(): WIPDao

    companion object {

        @Volatile
        private var INSTANCE: WIPDatabase? = null

        fun getDatabase(): WIPDatabase? {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        ApplicationClass.application.baseContext,
                        WIPDatabase::class.java,
                        "wips_database"
                    )
                        .addMigrations(MIGRATION_1_2)
                        .build()
                }
            }
            return INSTANCE
        }

        // Migration adds createdAt, updatedAt, uploadedAt columns.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // new columns are nullable in DB to keep import simple; we set default NULL.
                database.execSQL("ALTER TABLE WIP_LIST ADD COLUMN createdAt INTEGER")
                database.execSQL("ALTER TABLE WIP_LIST ADD COLUMN updatedAt INTEGER")
                database.execSQL("ALTER TABLE WIP_LIST ADD COLUMN uploadedAt INTEGER")
            }
        }
    }
}

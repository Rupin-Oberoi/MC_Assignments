package com.cse535.assignment3

import android.app.Application
import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Entity(tableName = "accelerometer_data")
data class AccelerometerData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val time: Long,
    val freq: Int,
    val x: Float,
    val y: Float,
    val z: Float
)

@Database(entities = [AccelerometerData::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accelerometerDataDao(): AccelerometerDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).addMigrations(MIGRATION_1_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_3 = object : Migration(1, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Drop the old table
        db.execSQL("DROP TABLE IF EXISTS `accelerometer_data`")
        // Create a new table
        db.execSQL("CREATE TABLE IF NOT EXISTS `accelerometer_data` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `time` INTEGER NOT NULL, `freq` INTEGER NOT NULL, `x` REAL NOT NULL, `y` REAL NOT NULL, `z` REAL NOT NULL)")
    }
}


@Dao
interface AccelerometerDataDao {
    @Insert
    suspend fun insert(accelerometerData: AccelerometerData)

    @Query("SELECT * FROM accelerometer_data ORDER BY time DESC LIMIT 50")
    suspend fun getLast50(): List<AccelerometerData>

    @Query("SELECT * FROM accelerometer_data ORDER BY time DESC")
    suspend fun getAll(): List<AccelerometerData>
}

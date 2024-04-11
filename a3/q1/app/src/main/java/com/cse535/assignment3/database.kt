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


@Entity(tableName = "accelerometer_data")
data class AccelerometerData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val time: Long,
    val x: Float,
    val y: Float,
    val z: Float
)

@Database(entities = [AccelerometerData::class], version = 1, exportSchema = false)
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface AccelerometerDataDao {
    @Insert
    suspend fun insert(accelerometerData: AccelerometerData)

    @Query("SELECT * FROM accelerometer_data ORDER BY time DESC LIMIT 200")
    suspend fun getLast200(): List<AccelerometerData>

    @Query("SELECT * FROM accelerometer_data ORDER BY time DESC")
    suspend fun getAll(): List<AccelerometerData>
}

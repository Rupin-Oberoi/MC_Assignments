package com.cse535.assignment2_v3

import android.app.Application
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "temperature_data")
data class TemperatureData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val date: String,
    val maxTemperature: Double,
    val minTemperature: Double
)

@Dao
interface TemperatureDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemperatureData(temperatureData: TemperatureData)

    @Query("SELECT * FROM temperature_data WHERE date = :date LIMIT 1")
    fun getTemperatureDataByDate(date: String): TemperatureData?
}

@Database(entities = [TemperatureData::class], version = 1)
abstract class TemperatureDatabase : RoomDatabase() {
    abstract fun temperatureDataDao(): TemperatureDataDao

}
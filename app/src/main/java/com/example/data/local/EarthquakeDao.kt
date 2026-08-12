package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EarthquakeDao {
    @Query("SELECT * FROM earthquakes WHERE isSimulated = 0 ORDER BY timeMillis DESC")
    fun getAllEarthquakes(): Flow<List<EarthquakeEntity>>

    @Query("SELECT * FROM earthquakes WHERE isSimulated = 0 AND magnitude >= :minMag ORDER BY timeMillis DESC")
    fun getEarthquakesByMinMagnitude(minMag: Double): Flow<List<EarthquakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarthquakes(earthquakes: List<EarthquakeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarthquake(earthquake: EarthquakeEntity)

    @Query("DELETE FROM earthquakes WHERE isSimulated = 0 AND cachedAtMillis < :threshold")
    suspend fun deleteOldCache(threshold: Long)

    @Query("DELETE FROM earthquakes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM earthquakes")
    suspend fun clearAll()
}

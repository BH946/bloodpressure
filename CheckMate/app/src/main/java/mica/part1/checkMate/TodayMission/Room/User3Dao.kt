package mica.part1.checkMate.TodayMission.Room

import androidx.room.*

@Dao
interface User3Dao {
    @Query("SELECT * FROM User3")
    fun getAll(): List<User3>

    @Insert
    fun insert(user3: User3)

    @Update
    fun update(user3: User3)

    @Delete
    fun delete(user3: User3)

    @Query("DELETE FROM User3")
    fun deleteAll()
}
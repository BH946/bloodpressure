package mica.part1.checkMate.HealthReport.Room

import androidx.room.*

@Dao
interface User4Dao {
    @Query("SELECT * FROM User4")
    fun getAll(): List<User4>

    @Insert
    fun insert(user4: User4)

    @Update
    fun update(user4: User4)

    @Delete
    fun delete(user4: User4)

    @Query("DELETE FROM User4")
    fun deleteAll()
}
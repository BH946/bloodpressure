package mica.part1.checkMate.HealthReport.Room

import androidx.room.*

@Dao
interface User5Dao {
    @Query("SELECT * FROM User5")
    fun getAll(): List<User5>

    @Insert
    fun insert(user5: User5)

    @Update
    fun update(user5: User5)

    @Delete
    fun delete(user5: User5)

    @Query("DELETE FROM User5")
    fun deleteAll()
}
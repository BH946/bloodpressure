package mica.part1.checkMate.Home.BloodPressure.Room

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAll(): List<User>

//    @Query("SELECT count FROM Todo")
//    fun getCount(): Int
    @Insert
    fun insert(todo: User)

    @Update
    fun update(todo: User)

    @Delete
    fun delete(todo: User)

    @Query("DELETE FROM User ")
    fun deleteAll()
}
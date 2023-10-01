package mica.part1.checkMate.TodayMission.Room

import androidx.room.*

@Dao
interface User2Dao {
    @Query("SELECT * FROM User2")
    fun getAll(): List<User2>

    //    @Query("SELECT count FROM User2")
    //    fun getCount(): Int

    @Query("UPDATE User2 SET everydayCheck=1 WHERE title = :userTitle AND time = :userTime AND dayOfWeek = :userDayOfWeek")
    fun everydayCheckUpdateOne(userTitle : String, userTime : String, userDayOfWeek : String) // 타이틀, 타임, 요일 같은 행만 체크 1로 바꿔주는 함수

    @Query("UPDATE User2 SET everydayCheck=0 WHERE title = :userTitle AND time = :userTime AND dayOfWeek = :userDayOfWeek")
    fun everydayCheckUpdateZero(userTitle : String, userTime : String, userDayOfWeek : String) // 타이틀, 타임, 요일 같은 행만 체크 0로 바꿔주는 함수

    @Insert
    fun insert(user2: User2)

    @Update
    fun update(user2: User2)

    @Delete
    fun delete(user2: User2)

    @Query("DELETE FROM User2")
    fun deleteAll()

}
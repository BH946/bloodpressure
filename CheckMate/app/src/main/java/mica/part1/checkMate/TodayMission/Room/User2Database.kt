package mica.part1.checkMate.TodayMission.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [User2::class], version = 1)
abstract class User2Database : RoomDatabase() {
    abstract fun user2Dao(): User2Dao

    companion object {
        private var instance: User2Database? = null

        @Synchronized
        fun getInstance(context: Context): User2Database? {
            if (instance == null) {
                synchronized(User2Database::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        User2Database::class.java,
                        "database-time1" // 실험용으로 time1으로 이름 지정
                    ).allowMainThreadQueries() // 여기서도 임시방편
                        .build()
                }
            }
            return instance
        }
    }
}
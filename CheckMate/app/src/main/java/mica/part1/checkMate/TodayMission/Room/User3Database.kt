package mica.part1.checkMate.TodayMission.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [User3::class], version = 1)
abstract class User3Database : RoomDatabase() {
    abstract fun user3Dao(): User3Dao

    companion object {
        private var instance: User3Database? = null

        @Synchronized
        fun getInstance(context: Context): User3Database? {
            if (instance == null) {
                synchronized(User3Database::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        User3Database::class.java,
                        "database-time2" // 실험용으로 time1으로 이름 지정
                    ).allowMainThreadQueries() // 여기서도 임시방편
                        .build()
                }
            }
            return instance
        }
    }


}
package mica.part1.checkMate.HealthReport.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User4::class], version = 1)
abstract class User4Database : RoomDatabase() {
    abstract fun user4Dao(): User4Dao

    companion object {
        private var instance: User4Database? = null

        @Synchronized
        fun getInstance(context: Context): User4Database? {
            if (instance == null) {
                synchronized(User4Database::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        User4Database::class.java,
                        "database-time3" // 실험용으로 time1으로 이름 지정
                    ).allowMainThreadQueries() // 여기서도 임시방편
                        .build()
                }
            }
            return instance
        }
    }


}
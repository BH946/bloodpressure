package mica.part1.checkMate.HealthReport.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User5::class], version = 1)
abstract class User5Database : RoomDatabase() {
    abstract fun user5Dao(): User5Dao

    companion object {
        private var instance: User5Database? = null

        @Synchronized
        fun getInstance(context: Context): User5Database? {
            if (instance == null) {
                synchronized(User5Database::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        User5Database::class.java,
                        "database-time4" // 실험용으로 time1으로 이름 지정
                    ).allowMainThreadQueries() // 여기서도 임시방편
                        .build()
                }
            }
            return instance
        }
    }


}
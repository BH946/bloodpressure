package mica.part1.checkMate.TodayMission.Room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User2(
    var title: String,
    var dayOfWeek: String,
    var time: String,
    var everydayCheck: Int // 0은 매일X, 1은 매일O
) {
    @PrimaryKey(autoGenerate = true) var id : Int = 0
}

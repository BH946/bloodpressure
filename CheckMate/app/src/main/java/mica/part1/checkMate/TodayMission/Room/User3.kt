package mica.part1.checkMate.TodayMission.Room

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class User3 (
    var title : String,
    var time : String
        )
{
    @PrimaryKey(autoGenerate = true) var id : Int = 0
}
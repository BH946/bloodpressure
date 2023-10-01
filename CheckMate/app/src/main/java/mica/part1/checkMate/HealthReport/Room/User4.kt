package mica.part1.checkMate.HealthReport.Room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User4 (
    var title : String,
    var complete : Int,
    var year : Int,
    var month : Int,
    var day : Int
)
{
    @PrimaryKey(autoGenerate = true) var id : Int = 0
}
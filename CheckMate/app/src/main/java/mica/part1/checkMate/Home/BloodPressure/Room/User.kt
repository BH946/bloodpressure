package mica.part1.checkMate.Home.BloodPressure.Room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    var date: String,
    var bloodPressure: String
) {
    @PrimaryKey(autoGenerate = true) var id : Int = 0
}

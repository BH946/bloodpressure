package mica.part1.checkMate.Home.BloodPressureReport

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import mica.part1.checkMate.Home.BloodPressure.Room.User
import mica.part1.checkMate.Home.BloodPressure.Room.UserDatabase
import mica.part1.checkMate.databinding.ActivityBloodPressureReportBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.EnumSet.range

class BloodPressureReportActivity : AppCompatActivity() {
    val binding by lazy {ActivityBloodPressureReportBinding.inflate(layoutInflater)}

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var db : UserDatabase? = UserDatabase.getInstance(this)
        // 데이터가공..
        var dataArrList : List<User>? = db?.userDao()?.getAll()
        var dataArr = dataArrList?.toMutableList()
        var dataTotal : String = ""
        while(dataArr!!.size > 15) {
            dataArr.removeAt(0)
        }
        for(i in 0..dataArr!!.size-1) {
            var date = dataArr!![i].date
            date = date.slice(IntRange(0,15))
            var bP = dataArr!![i].bloodPressure
            var arr = bP.split("\n")
            var data = date +" 수축기:" + arr[0] + "이완기:" + arr[1]
            dataTotal = dataTotal + data +"\n"
        }

        binding.bloodPressureReportTextView.text = "${dataTotal}"
    }
}
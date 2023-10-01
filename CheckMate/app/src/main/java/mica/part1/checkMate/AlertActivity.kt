package mica.part1.checkMate

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import mica.part1.checkMate.HealthReport.Room.User4
import mica.part1.checkMate.HealthReport.Room.User4Database
import mica.part1.checkMate.SharedPreference.MyApplication
import mica.part1.checkMate.TodayMission.Room.User3
import mica.part1.checkMate.TodayMission.Room.User3Database
import mica.part1.checkMate.databinding.ActivityAlertBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AlertActivity : AppCompatActivity() {
    val binding by lazy{ActivityAlertBinding.inflate(layoutInflater)}
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // 초기화
        var db3 : User3Database? =  User3Database.getInstance(binding.root.context)
        var db4 : User4Database? = User4Database.getInstance(binding.root.context)
        var titleNum = MyApplication.prefs.getString("alertTitleNum", "0").toInt() // 기본값 0(널)
        var time = MyApplication.prefs.getString("alertTime", "00:00") // 기본값 00:00(널)

        // 뷰 초기화
        initView(titleNum, time)

        // 버튼 초기화 => finish()말고 나중엔 완전 어플 종료되게 하기!
        buttonClick1(titleNum, time, db3, db4) // 완료되게 하기
        buttonClick2() // 애매하네
        buttonClick3() // 그냥 종료하기

    }


    // 버튼 1 초기화
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buttonClick1(titleNum: Int, time: String, db3: User3Database?, db4: User4Database?) {
        binding.alertButton1.setOnClickListener {
            var title : String = ""
            totalItemCountCur()
            // 약, 혈압, 음식, 운동 => 1,2,3,4번 순
            if (titleNum == 1) {
                approximatelyCountCur()
                title = "오늘의 약 복용"
            } else if (titleNum == 2) {
                bloodPressureCountCur()
                title = "오늘의 혈압측정"
            } else if (titleNum == 3) {
                foodCountCur()
                title = "오늘의 식사시간"
            } else if (titleNum == 4) {
                exerciseCountCur()
                title = "오늘의 운동"
            }
            // db에 완료한 데이터 기록
            db3?.user3Dao()?.insert(User3(title, time))
            // 여기서 캘린더를 위해 room에 하루기준으로 데이터 계산해서 저장해야함!! => 이걸로 이달의 미션달성 등등 퍼센트 캘린더에 나타내기~!!!
            initDatabase4(db4)
            finish()
        }
    }
    
    // 버튼 2 초기화
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buttonClick2() {
        binding.alertButton2.setOnClickListener {
            Toast.makeText(this, "꼭 하시고 오늘의 미션에 체크하기!!", Toast.LENGTH_SHORT)
            Thread.sleep(500)
            ActivityCompat.finishAffinity(this) // 액티비티 종료
            System.exit(0) // 프로세스 종료
        }
    }

    
    // 버튼 3 초기화 => 아무것도 안하고 종료
    private fun buttonClick3() {
        binding.alertButton3.setOnClickListener {
            ActivityCompat.finishAffinity(this) // 액티비티 종료
            System.exit(0) // 프로세스 종료
        }
    }


    // 뷰 초기화
    // 약, 혈압, 음식, 운동 => 1,2,3,4번 순
    fun initView(titleNum : Int, time : String) {
        if (titleNum == 0) { // 널이라면
            // 아무것도 안해요
            binding.idTextView.text = " 오류다 널 ${titleNum}"
        } else if (titleNum == 1) { // 약이라면,
            binding.idTextView.text = "오늘의 약 복용"
            binding.detailTextView.text = time
            binding.alertButton1.text = "지금 먹었어요"
            binding.alertButton2.text = "잠시 후에 먹을게요"
            binding.alertButton3.text = "오늘은 힘들어요"
        } else if (titleNum == 2) { // 혈압이라면,
            binding.idTextView.text = "오늘의 혈압측정"
            binding.detailTextView.text = "오늘의 혈압은?"
            binding.alertButton1.text = "지금 했어요"
            binding.alertButton2.text = "잠시 후에 할게요"
            binding.alertButton3.text = "오늘은 힘들어요"
        } else if (titleNum == 3) { // 음식이라면,
            binding.idTextView.text = "오늘의 식사시간"
            binding.detailTextView.text = "오늘의 음식은?"
            binding.alertButton1.text = "지금 먹었어요"
            binding.alertButton2.text = "잠시 후에 먹을게요"
            binding.alertButton3.text = "오늘은 힘들어요"
        } else if (titleNum == 4) { // 운동이라면,
            binding.idTextView.text = "오늘의 운동"
            binding.detailTextView.text = "오늘의 운동은?"
            binding.alertButton1.text = "미션 완료!"
            binding.alertButton2.text = "잠시 후에 할게요"
            binding.alertButton3.text = "오늘은 힘들어요"
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun initDatabase4(db4: User4Database?) {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
        val mFormatYear = SimpleDateFormat("yyyy",Locale.KOREAN)
        mFormatYear.setTimeZone(tz) //DateFormat에 TimeZone 설정
        val mFormatMonth = SimpleDateFormat("MM",Locale.KOREAN)
        mFormatMonth.setTimeZone(tz) //DateFormat에 TimeZone 설정
        val mFormatDay = SimpleDateFormat("dd",Locale.KOREAN)
        mFormatDay.setTimeZone(tz) //DateFormat에 TimeZone 설정
        var currentYear = mFormatYear.format(date).toInt()
        var currentMonth = mFormatYear.format(date).toInt()
        var currentDay = mFormatYear.format(date).toInt()

        var dayList = db4?.user4Dao()?.getAll()
        // 중복 데이터면 삭제
        for (i in 0.. dayList!!.size-1) {
            // 현재날짜와 db에 저장된 날짜가 같고, title까지 같으면? 해당데이터 전부삭제!! 그리고 아래에서 다시 업데이트 하는방식
            if(dayList[i].year == currentYear && dayList[i].month == currentMonth && dayList[i].day == currentDay && dayList[i].title.equals("todayItemRate"))
                db4?.user4Dao()?.delete(dayList[i]) // 해당 데이터 삭제 ! !
            else if(dayList[i].year == currentYear && dayList[i].month == currentMonth && dayList[i].day == currentDay && dayList[i].title.equals("exerciseRate"))
                db4?.user4Dao()?.delete(dayList[i]) // 해당 데이터 삭제 ! !
            else if(dayList[i].year == currentYear && dayList[i].month == currentMonth && dayList[i].day == currentDay && dayList[i].title.equals("approximatelyRate"))
                db4?.user4Dao()?.delete(dayList[i]) // 해당 데이터 삭제 ! !
            else if(dayList[i].year == currentYear && dayList[i].month == currentMonth && dayList[i].day == currentDay && dayList[i].title.equals("bloodPressureRate"))
                db4?.user4Dao()?.delete(dayList[i]) // 해당 데이터 삭제 ! !
            else if(dayList[i].year == currentYear && dayList[i].month == currentMonth && dayList[i].day == currentDay && dayList[i].title.equals("foodRate"))
                db4?.user4Dao()?.delete(dayList[i]) // 해당 데이터 삭제 ! !
        }
        // 데이터 저장하기
        var calendarTodayMissionRate = calTodayMissionRate()
        if (calendarTodayMissionRate == 100) db4?.user4Dao()?.insert(User4("todayItemRate", 1, currentYear, currentMonth,currentDay))
        else db4?.user4Dao()?.insert(User4("todayItemRate", 0, currentYear, currentMonth, currentDay))
        var calendarExerciseRate = calExerciseRate()
        if (calendarExerciseRate == 100) db4?.user4Dao()?.insert(User4("exerciseRate", 1, currentYear, currentMonth, currentDay))
        else db4?.user4Dao()?.insert(User4("exerciseRate", 0, currentYear, currentMonth, currentDay))
        var calendarApproximatelyRate = calApproximatelyRate()
        if (calendarApproximatelyRate == 100) db4?.user4Dao()?.insert(User4("approximatelyRate", 1, currentYear, currentMonth, currentDay))
        else db4?.user4Dao()?.insert(User4("approximatelyRate", 0, currentYear, currentMonth, currentDay))
        var calendarBloodPressureRate = calBloodPressureRate()
        if (calendarBloodPressureRate == 100) db4?.user4Dao()?.insert(User4("bloodPressureRate", 1, currentYear, currentMonth, currentDay))
        else db4?.user4Dao()?.insert(User4("bloodPressureRate", 0, currentYear, currentMonth, currentDay))
        var calendarFoodRate = calFoodRate()
        if (calendarFoodRate == 100) db4?.user4Dao()?.insert(User4("foodRate", 1, currentYear, currentMonth, currentDay))
    }


    // 캘린더를 위한 데이터 가공 및 저장 함수들
    fun calTodayMissionRate() : Int {
        val calendarTodayItemCount = MyApplication.prefs.getString("todayItemCount", "0.0").toDouble() // 오늘의 미션 총 개수
        val calendarTodayItemCountCur = MyApplication.prefs.getString("todayItemCountCur", "0.0").toDouble() // 오늘의 미션 완료한 현재 개수
        return ((calendarTodayItemCountCur / calendarTodayItemCount)*100.0).toInt() // 저장할때 NaN으로 저장될경우를 위해 int형으로 저장하고, 나타낼때 더블형으로 나타내기
    }

    fun calExerciseRate() : Int {
        val calendarExerciseCount = MyApplication.prefs.getString("exerciseCount", "0.0").toDouble() // 오늘의 운동 미션 총 개수
        val calendarExerciseCountCur = MyApplication.prefs.getString("exerciseCountCur", "0.0").toDouble() // 오늘의 운동 미션 완료한 현재 개수
        return ((calendarExerciseCountCur / calendarExerciseCount)*100.0).toInt() // 저장할때 NaN으로 저장될경우를 위해 int형으로 저장하고, 나타낼때 더블형으로 나타내기
    }

    fun calApproximatelyRate() : Int {
        val calendarApproximatelyCount = MyApplication.prefs.getString("approximatelyCount", "0.0").toDouble() // 오늘의 운동 미션 총 개수
        val calendarApproximatelyCountCur = MyApplication.prefs.getString("approximatelyCountCur", "0.0").toDouble() // 오늘의 운동 미션 완료한 현재 개수
        return ((calendarApproximatelyCountCur / calendarApproximatelyCount)*100.0).toInt() // 저장할때 NaN으로 저장될경우를 위해 int형으로 저장하고, 나타낼때 더블형으로 나타내기
    }

    fun calBloodPressureRate() : Int {
        val calendarBloodPressureCount = MyApplication.prefs.getString("bloodPressureCount", "0.0").toDouble() // 오늘의 운동 미션 총 개수
        val calendarBloodPressureCountCur = MyApplication.prefs.getString("bloodPressureCountCur", "0.0").toDouble() // 오늘의 운동 미션 완료한 현재 개수
        return ((calendarBloodPressureCountCur / calendarBloodPressureCount)*100.0).toInt() // 저장할때 NaN으로 저장될경우를 위해 int형으로 저장하고, 나타낼때 더블형으로 나타내기
    }

    fun calFoodRate() : Int {
        val calendarFoodCount = MyApplication.prefs.getString("foodCount", "0.0").toDouble() // 오늘의 운동 미션 총 개수
        val calendarFoodCountCur = MyApplication.prefs.getString("foodCountCur", "0.0").toDouble() // 오늘의 운동 미션 완료한 현재 개수
        return ((calendarFoodCountCur / calendarFoodCount)*100.0).toInt() // 저장할때 NaN으로 저장될경우를 위해 int형으로 저장하고, 나타낼때 더블형으로 나타내기
    }



    // 데이터 현재 아이템 개수 저장 함수들
    fun totalItemCountCur() {
        var todayItemCountCur = MyApplication.prefs.getString("todayItemCountCur", "0").toInt()
        todayItemCountCur++
        var completeTotalItemCount = todayItemCountCur.toString()
        MyApplication.prefs.setString("todayItemCountCur", "${completeTotalItemCount}") // 미션 완료한 현재 아이템 개수 저장.
    }

    fun exerciseCountCur() {
        var exerciseCountCur = MyApplication.prefs.getString("exerciseCountCur", "0").toInt()
        exerciseCountCur++
        var completeExerciseCount = exerciseCountCur.toString()
        MyApplication.prefs.setString("exerciseCountCur", "${completeExerciseCount}") // 미션 완료한 현재 아이템 개수 저장.
    }

    fun approximatelyCountCur() {
        var approximatelyCountCur = MyApplication.prefs.getString("approximatelyCountCur", "0").toInt()
        approximatelyCountCur++
        var completeApproximatelyCount = approximatelyCountCur.toString()
        MyApplication.prefs.setString("approximatelyCountCur", "${completeApproximatelyCount}") // 미션 완료한 현재 아이템 개수 저장.
    }

    fun bloodPressureCountCur() {
        var bloodPressureCountCur = MyApplication.prefs.getString("bloodPressureCountCur", "0").toInt()
        bloodPressureCountCur++
        var completeBloodPressureCount = bloodPressureCountCur.toString()
        MyApplication.prefs.setString("bloodPressureCountCur", "${completeBloodPressureCount}") // 미션 완료한 현재 아이템 개수 저장.
    }

    fun foodCountCur() {
        var foodCountCur = MyApplication.prefs.getString("foodCountCur", "0").toInt()
        foodCountCur++
        var completeFoodCount = foodCountCur.toString()
        MyApplication.prefs.setString("foodCountCur", "${completeFoodCount}") // 미션 완료한 현재 아이템 개수 저장.
    }
}
package mica.part1.checkMate

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import mica.part1.checkMate.HealthReport.HealthReportFragment
import mica.part1.checkMate.Home.HomeFragment
import mica.part1.checkMate.SharedPreference.MyApplication
import mica.part1.checkMate.TodayMission.Room.User3Database
import mica.part1.checkMate.TodayMission.TodayMissionFragment
import mica.part1.checkMate.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    val binding by lazy{ActivityMainBinding.inflate(layoutInflater)}

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 1. 날짜 비교후 데이터 초기화 => 00시마다 하게 되어있지만 놔둬 두겠음.
        initDataReset()

        // Fragment생성자(클래스)
        val homeFragment = HomeFragment()
        val todayMissionFragment = TodayMissionFragment()
        val healthReportFragment = HealthReportFragment()

        // replaceFragment함수를 이용해서 homeFragment클래스로 홈탭에 미리 초기화
        replaceFragment(homeFragment)
        // replaceFragment함수를 이용해서 bottomNavigationView를 구성
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment)
                R.id.todayMission -> replaceFragment(todayMissionFragment)
                R.id.healthReprot -> replaceFragment(healthReportFragment)
            }
            true
        }

    }


    // 날짜 비교후 데이터 초기화 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initDataReset() {
        // 1. 현재 날짜와 날짜 저장 초기화 하는 부분 처음실행시 saveDate의 파일은 없기때문에 기본값 0을 이용해 조건문 사용
        val now = System.currentTimeMillis()
        val date = Date(now)
        val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
        val mFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        mFormat.setTimeZone(tz) //DateFormat에 TimeZone 설정
        var currentDate = mFormat.format(date).toString()

        var db3 : User3Database? = User3Database.getInstance(this)
        if(MyApplication.prefs.getString("saveDate", "0").equals("0")) { // 처음 실행해서 데이터가 없는 상황이라면
            MyApplication.prefs.setString("saveDate", currentDate) // 현재 날짜로 저장
        } else { // 데이터가 있는 상황이라면
            var saveDateYear = MyApplication.prefs.getString("saveDate", "0").slice(IntRange(0, 3)).toInt()
            var saveDateMonth = MyApplication.prefs.getString("saveDate", "0").slice(IntRange(5, 6)).toInt()
            var saveDateDay = MyApplication.prefs.getString("saveDate", "0").slice(IntRange(8, 9)).toInt()
            var currentYear = currentDate.slice(IntRange(0, 3)).toInt()
            var currentMonth = currentDate.slice(IntRange(5, 6)).toInt()
            var currentDay = currentDate.slice(IntRange(8, 9)).toInt()
            if (saveDateYear < currentYear || saveDateMonth < currentMonth ||
                (saveDateYear == currentYear && saveDateMonth == currentMonth && saveDateDay < currentDay)) {
                // 날짜 지날시 데이터 초기화 해야하는 것들
                MyApplication.prefs.setString("todayItemCountCur", "0") // 미션 완료한 현재 총 아이템 개수 초기화
                MyApplication.prefs.setString("exerciseCountCur", "0") // 미션 완료한 현재 운동 아이템 개수 초기화
                MyApplication.prefs.setString("approximatelyCountCur", "0") // 미션 완료한 현재 약 복용 아이템 개수 초기화
                MyApplication.prefs.setString("bloodPressureCountCur", "0") // 미션 완료한 현재 혈압측정 아이템 개수 초기화
                MyApplication.prefs.setString("foodCountCur", "0") // 미션 완료한 현재 식사 아이템 개수 초기화
                db3?.user3Dao()?.deleteAll() // 디비 데이터 초기화
            }
        }
        MyApplication.prefs.setString("saveDate", currentDate) // 담날짜랑 비교하게끔 현재날짜 저장하는 부분
//        Log.d("home22", "db3 : ${db3?.user3Dao()?.getAll()}")
    }

    // void형식 함수(인자로 fragment를 받음)
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction() // support를 통해 Fragment교체
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
    }
}
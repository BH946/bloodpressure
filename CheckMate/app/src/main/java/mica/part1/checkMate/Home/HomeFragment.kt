package mica.part1.checkMate.Home

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import mica.part1.checkMate.HealthReport.Room.User5
import mica.part1.checkMate.HealthReport.Room.User5Database
import mica.part1.checkMate.Home.BloodPressure.Room.UserDatabase
import mica.part1.checkMate.Home.BloodPressure.BloodPressureHomeActivity
import mica.part1.checkMate.Home.BloodPressure.Room.User
import mica.part1.checkMate.SharedPreference.MyApplication
import mica.part1.checkMate.databinding.FragmentHomeBinding
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class HomeFragment : Fragment() {
    // 전역 바인딩
    lateinit var binding:FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db : UserDatabase? = UserDatabase.getInstance(requireContext()) // 혈압 db
        val dataList = db?.userDao()?.getAll() // 혈압리스트

        // 상태바 설정
        activity?.window?.statusBarColor = Color.parseColor("#FFFFFFFF") // 흰색
        activity?.window?.decorView?.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) // 검정색

        // 홈 UI 설정 윗부분
        // 1. 금일 날짜 적용
        dateInsert()

        // 홈 UI 설정 아래부분
        // 1. 오늘의 미션달성
        initTodayMission()
        
        // 2. 오늘의 혈압수치 - 최근 혈압수치 가져옴
        todayBloodPressureGoodAndBad(dataList)
        
        // 3. 현재 혈압수치 (120/80기준) - 최근 혈압수치 가져옴
        currentBloodPressureGoodAndBad(dataList)

        // 4. 오늘의 운동강도
        initExerciseMission()

        // 버튼 init
//        deleteButton(db) // 일단 ui도 클릭x 안보이게 해놨음.
        bloodPressureButton()
    }

    // 데이터 저장되어있는 부분 초기화 버튼
    private fun deleteButton(db : UserDatabase?) {
        binding.deleteButton.setOnClickListener {
            context?.let{
                db?.userDao()?.deleteAll()
                Thread.sleep(500) // 적절한 sleep이 있어야 데이터변동이 UI를 따라감.
            }
        }
    }

    // 혈압측정 하는 버튼
    private fun bloodPressureButton() {
        binding.currentBloodPressureButton.setOnClickListener {
            context?.let {
                val intent = Intent(it, BloodPressureHomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // 금일 날짜 적용
    @RequiresApi(Build.VERSION_CODES.O)
    private fun dateInsert() {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
        val mFormat = SimpleDateFormat("MM월dd일", Locale.KOREAN)
        mFormat.setTimeZone(tz) //DateFormat에 TimeZone 설정
        var currentDate = mFormat.format(date).toString()

        binding.dateTextView.text = currentDate
    }

    
    // 최근 혈압값에 따른 출력 함수
    private fun currentBloodPressureGoodAndBad(dataList : List<User>?) {
        var currentBloodPressure : String // 최근 혈압 선언
        var sys : Double
        var dia : Double
        var maxBP : Int = 0 // 수축기 혈압(120기준) =>  100/60 mmHg 이하는 저혈압
        var minBP : Int = 0 // 이완기 혈압(80기준)
        try {
            currentBloodPressure = dataList!![dataList!!.size-1].bloodPressure // 최근 혈압 구함
        } catch (e : IndexOutOfBoundsException){
            currentBloodPressure = "" // 혈압 없는경우 이다.
        }
        val bP = currentBloodPressure
        if (bP != "") { // null인지 확인
            // 데이터 가공..
            val arr = bP.split("\n") // EX : [116, 78, 80]
            sys = arr[0].toDouble() // 수축기
            dia = arr[1].toDouble() // 이완기
            maxBP = sys.toInt()
            minBP = dia.toInt()
            if((maxBP > 120 || minBP > 80) || (maxBP < 100 || minBP < 60)) { // 고혈압이거나 저혈압인경우
                binding.currentBloodPressureTextView.text = "Bad"
                binding.currentBloodPressureTextView.setTextColor(Color.RED)
            }
            else { // 정상인 경우
                binding.currentBloodPressureTextView.text = "Good"
                binding.currentBloodPressureTextView.setTextColor(Color.parseColor("#2BD1FD"))
            }
        }
        else { // null이라면(혈압데이터 없다면)
            binding.currentBloodPressureTextView.text = ""
        }
    }
    

    // 운동미션 달성률에 따른 출력 함수
    private fun exerciseGoodAndBad(i : Int) {
        // 오늘의 운동강도는 오늘의 미션에서 운동 하는날이 아닌날은 비워두거나 오늘은 휴식을 취해보요 등 나타내기.
        if (i==1){ // Good
            binding.todayExerciseTextView1.text="Good"
            binding.todayExerciseTextView1.setTextColor(Color.parseColor("#2BD1FD"))
            binding.todayExerciseTextView2.text="좋아요! 우리함께\n건강을 찾아볼까요?"
        }
        else if (i==2) { // 아예 운동이 없는 경우!
            binding.todayExerciseTextView1.text="Not"
            binding.todayExerciseTextView1.setTextColor(Color.BLACK)
            binding.todayExerciseTextView2.text="오늘은\n휴식을 취해볼까요?"
        }
        else { // Bad
            binding.todayExerciseTextView1.text="Bad"
            binding.todayExerciseTextView1.setTextColor(Color.RED)
            binding.todayExerciseTextView2.text="오늘의 운동\n잊지마세요!"
        }
    }

    // 최근 혈압값에 따른 출력 함수
    private fun todayBloodPressureGoodAndBad(dataList : List<User>?) {
        var currentBloodPressure : String // 최근 혈압 선언
        var sys : Double
        var dia : Double
        var maxBP : Int = 0 // 수축기 혈압(120기준) =>  100/60 mmHg 이하는 저혈압
        var minBP : Int = 0 // 이완기 혈압(80기준)
        try {
            currentBloodPressure = dataList!![dataList!!.size-1].bloodPressure // 최근 혈압 구함
        } catch (e : IndexOutOfBoundsException){
            currentBloodPressure = "" // 혈압 없는경우 이다.
        }
        val bP = currentBloodPressure
        if (bP != "") { // null인지 확인
            // 데이터 가공..
            val arr = bP.split("\n") // EX : [116, 78, 80]
            sys = arr[0].toDouble() // 수축기
            dia = arr[1].toDouble() // 이완기
            maxBP = sys.toInt()
            minBP = dia.toInt()

            if((maxBP > 120 || minBP > 80) || (maxBP < 100 || minBP < 60)) { // 고혈압이거나 저혈압인경우
                binding.todayBloodPressureTextView1.text = "비정상"
                binding.todayBloodPressureTextView1.setTextColor(Color.RED)
                binding.todayBloodPressureTextView2.text = "${maxBP}/${minBP}"
                binding.todayBloodPressureTextView2.setTextColor(Color.RED)
            }
            else { // 정상인 경우
                binding.todayBloodPressureTextView1.text = "정상"
                binding.todayBloodPressureTextView1.setTextColor(Color.parseColor("#2BD1FD"))
                binding.todayBloodPressureTextView2.text = "${maxBP}/${minBP}"
                binding.todayBloodPressureTextView2.setTextColor(Color.parseColor("#67DCFD"))
            }
        }
        else { // null이라면(혈압데이터 없다면)
            binding.currentBloodPressureTextView.text = ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initTodayMission() {
        var totalMissionCount = MyApplication.prefs.getString("todayItemCount","0.0").toDouble()
        var currentMissionCount = MyApplication.prefs.getString("todayItemCountCur", "0.0").toDouble()
        var missionRate = (currentMissionCount / totalMissionCount)*100.0
        if(missionRate > 100)  missionRate = 100.0 // 100프로 이상나오는걸 방지하기 위한 예외처리
        if(missionRate.toInt() == 0) { // NaN 예외처리를 위해
            binding.rateTextView.text = "${missionRate.toInt()}"
            binding.rateProgressBar.setProgressPercentage(0.0)
        } else {
            binding.rateTextView.text = "${missionRate.toInt()}"
            binding.rateProgressBar.setProgressPercentage(missionRate!!)
        }
        // user5에 missionRate.toInt()데이터를 넣자, 이때의 날짜와 같이
        // 여기서 날짜를 넣을때는 room데이터에 같은 날짜가 있다면 삭제해주고 넣게끔 해주자.
        var db5 : User5Database? = User5Database.getInstance(binding.root.context)
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

        var dayList = db5?.user5Dao()?.getAll()
        for (i in 0.. dayList!!.size-1) {
            if (dayList[i].year == currentYear && dayList[i].month == currentMonth && dayList[i].day == currentDay)
                db5?.user5Dao()?.delete(dayList[i]) // 해당 데이터 삭제 ! !
        }
        db5?.user5Dao()?.insert(User5(missionRate.toInt(), currentYear,currentMonth,currentDay)) // 데이터 저장 ! !
    }

    private fun initExerciseMission() {
        var exerciseCount = MyApplication.prefs.getString("exerciseCount","0").toDouble()
        var exerciseCountCur = MyApplication.prefs.getString("exerciseCountCur", "0").toDouble()
        var missionRate = (exerciseCountCur / exerciseCount)*100.0
        if(exerciseCount.toInt() == 0) { // 운동 아이템이 없다는 말
            exerciseGoodAndBad(2)
        } else {
            if (missionRate.toInt() < 100) {
                exerciseGoodAndBad(0)
            } else if (missionRate.toInt() >= 100) {
                exerciseGoodAndBad(1)
            }
        }
    }

}
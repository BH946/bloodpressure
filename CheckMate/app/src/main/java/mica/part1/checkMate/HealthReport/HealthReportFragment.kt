package mica.part1.checkMate.HealthReport

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.prolificinteractive.materialcalendarview.CalendarDay
import mica.part1.checkMate.HealthReport.Adapter.CalendarRateAdapter
import mica.part1.checkMate.HealthReport.CalendarDecorator.*
import mica.part1.checkMate.HealthReport.DataClass.CalendarRate
import mica.part1.checkMate.HealthReport.Room.User4Database
import mica.part1.checkMate.HealthReport.Room.User5Database
import mica.part1.checkMate.databinding.FragmentHealthreportBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class HealthReportFragment : Fragment() {

    lateinit var binding:FragmentHealthreportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHealthreportBinding.inflate(inflater, container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O) // 현재시간 가져오기위해 사용(한국)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상태바 설정
        activity?.window?.statusBarColor = Color.parseColor("#DEF8FF") // bluelayout
        activity?.window?.decorView?.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) // 검정색


        // MaterialCalendar 설정입니다.
        val now = System.currentTimeMillis()
        val date = Date(now)
        val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
        val mFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        mFormat.setTimeZone(tz) //DateFormat에 TimeZone 설정

        var startTimeCalendar = Calendar.getInstance() // 시작날짜 캘린더 객체 생성
        var currentTimeCalendar = mFormat.format(date).toString()
//        var currentTimeCalendar = LocalDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate().toString() // 현재날짜 한국기준으로 설정

        // 현재 날짜 설정 (현재시간 받고 있는중)
        val crCalendarDay = CalendarDay(
            currentTimeCalendar.slice(IntRange(0,3)).toInt(),
            currentTimeCalendar.slice(IntRange(5,6)).toInt(),
            currentTimeCalendar.slice(IntRange(8,9)).toInt()
        )

        // 달력 설정
        binding.calendar.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY) // 일 월 화 수 목 금 토
            .setMaximumDate(CalendarDay.from(crCalendarDay.year, crCalendarDay.month, 31))
            .commit()
        binding.calendar.isDynamicHeightEnabled = true // 다이나믹하게 높이지정

        // 처음 달력 title format
        binding.calendar.setTitleFormatter({
            val simpleDateFormat = SimpleDateFormat("MM월", Locale.KOREA)
            simpleDateFormat.format(startTimeCalendar.time)
        })
        // 달력 넘길때 format
        binding.calendar.setOnMonthChangedListener { widget, date ->
            binding.calendar.setTitleFormatter({
                val simpleDateFormat = SimpleDateFormat("MM월", Locale.KOREA)
                simpleDateFormat.format(date.date)
            })
            initChangeMonthCalendar(date) // 캘린더 달 변경때마다 리사이클러뷰 재설정
        }

        val defaultDecorator = DefaultDecorator(requireActivity()) // 배경색 default

        binding.calendar.addDecorator( // Decorator에 s를 붙여서 여러 Decorator를 추가함.
            defaultDecorator
        )
        
        // 자 이부분에 한번 미션달성률을 적용
        var db5 : User5Database? = User5Database.getInstance(binding.root.context)
        var calendarDataList = db5?.user5Dao()?.getAll()
        for (i in 0..calendarDataList!!.size-1) {
            val evCalendarDay = CalendarDay( // 객체 생성
                calendarDataList[i].year,
                calendarDataList[i].month,
                calendarDataList[i].day
            )
           binding.calendar.addDecorator(EventDecorator(calendarDataList[i].rate, requireActivity(), evCalendarDay))
        }

        // 리사이클러뷰 설정
        // 1. 데이터 로딩 -> crCalendarDay는 그냥 넣은거임 상관 x
        var data = loadData(0,crCalendarDay) // 달력변경에서 데이터로딩인지, 처음 초기화할때 데이터로딩인지 알아야 하기때문에 0, 1로 구분하겠음!
        // 2. 어댑터 생성
        var calendarRateAdapter = CalendarRateAdapter(data) // 생성자 전역 만들었으니 data넘기기
        // 4. 화면에 있는 리사이클러뷰에 어댑터 연결
        binding.recyclerview.adapter = calendarRateAdapter
        // 5. 레이아웃 매니저 연결
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

    }

    // 캘린더 달 변경시
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initChangeMonthCalendar(date : CalendarDay) {
        // 리사이클러뷰
        // 1. 데이터 로딩 -> date는 꼭 필요!! 최대날짜 구할때 필요
        var data = loadData(1, date)
        // 2. 어댑터 생성
        var calendarRateAdapter = CalendarRateAdapter(data) // 생성자 전역 만들었으니 data넘기기
        // 4. 화면에 있는 리사이클러뷰에 어댑터 연결
        binding.recyclerview.adapter = calendarRateAdapter
        // 5. 레이아웃 매니저 연결
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadData(calendarTitleChange : Int, calendarDay : CalendarDay): MutableList<CalendarRate> {
        // 데이터 설정 시작
        var data:MutableList<CalendarRate> = mutableListOf() // return할 data
        var db4 : User4Database? = User4Database.getInstance(binding.root.context)
        var calendarDataList = db4?.user4Dao()?.getAll()
        
        // 캘린더의 하루마다 complete한 개수 구하기위한 변수 지정
        var calendarTodayItemCountCur : Int = 0
        var calendarExerciseCountCur : Int = 0
        var calendarApproximatelyCountCur : Int = 0
        var calendarBloodPressureCountCur : Int = 0
        var calendarFoodCountCur : Int = 0
        // 캘린더 이달의 미션달성률에 적용할 변수 지정
        var calendarTodayMissionRate : Int = 0
        var calendarExerciseRate : Int = 0
        var calendarApproximatelyRate : Int = 0
        var calendarBloodPressureRate : Int = 0
        var calendarFoodRate : Int = 0

        // 0이면 처음 캘린더 화면으로 클릭했을때
        if (calendarTitleChange == 0) {
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

            var cal = Calendar.getInstance()
            cal.set(currentYear,currentMonth-1,currentDay)
            var maxDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) // 최대날짜 설정

            for (i in 0..calendarDataList!!.size-1) {
                // 1. 현재 총 미션달성
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("todayItemRate")) {
                    calendarTodayItemCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarTodayMissionRate = ((calendarTodayItemCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }
                // 2. 약
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("approximatelyRate")) {
                    calendarApproximatelyCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarApproximatelyRate = ((calendarApproximatelyCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }
                // 3. 혈압
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("bloodPressureRate")) {
                    calendarBloodPressureCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarBloodPressureRate = ((calendarBloodPressureCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }
                // 4. 운동
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("exerciseRate")) {
                    calendarExerciseCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarExerciseRate = ((calendarExerciseCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }
                // 5. 식사
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("foodRate")) {
                    calendarFoodCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarFoodRate = ((calendarFoodCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }

            } // 계산 끝

        } else if (calendarTitleChange == 1) { // 달력 월 변경때 초기화
            // 캘린더 달력 변경때마다 최대날짜가 당연히 다르기 때문에 최대날짜를 동적으로 구현해야함.
            var currentYear = calendarDay.year
            var currentMonth = calendarDay.month+1
            var currentDay = calendarDay.day
            var cal = Calendar.getInstance()
            cal.set(currentYear,currentMonth-1,currentDay)
            var maxDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) // 최대날짜 설정
            currentDay = calendarDay.day+maxDayOfMonth-1 // 1일 나오기 때문!!!!

            for (i in 0..calendarDataList!!.size-1) {
                // 1. 현재 총 미션달성
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("todayItemRate")) {
                    calendarTodayItemCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarTodayMissionRate = ((calendarTodayItemCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }
                // 2. 약
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("approximatelyRate")) {
                    calendarApproximatelyCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarApproximatelyRate = ((calendarApproximatelyCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }
                // 3. 혈압
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("bloodPressureRate")) {
                    calendarBloodPressureCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarBloodPressureRate = ((calendarBloodPressureCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }
                // 4. 운동
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("exerciseRate")) {
                    calendarExerciseCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarExerciseRate = ((calendarExerciseCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }
                // 5. 식사
                if(calendarDataList[i].day <= currentDay && calendarDataList[i].month == currentMonth && calendarDataList[i].title.equals("foodRate")) {
                    calendarFoodCountCur += calendarDataList[i].complete // 현재개수로 보면됨
                    calendarFoodRate = ((calendarFoodCountCur.toDouble() / maxDayOfMonth.toDouble())*100.0).toInt()
                }
            } // 계산 끝
        }
        // 2. 적용
        var titleList:MutableList<String> = mutableListOf("미션 달성", "약 복용", "혈압측정", "운동강도", "식사시간")
        for(i in 0..titleList.size-1) {
            var title = titleList[i]
            var rate = 0 // 기본 값은 0으로, 아래는 한달전체중 미션성공한 개수들로 계산해서 rate에 넣은부분임.
            if (i == 0 && calendarTodayItemCountCur > 0) rate =  (calendarTodayMissionRate) // "미션 달성"
            if (i == 1 && calendarApproximatelyCountCur > 0) rate = calendarApproximatelyRate // "약 복용"
            if (i == 2 && calendarBloodPressureCountCur > 0) rate = calendarBloodPressureRate // "혈압측정"
            if (i == 3 && calendarExerciseCountCur > 0) rate = calendarExerciseRate // "운동강도"
            if (i == 4 && calendarFoodCountCur > 0) rate = calendarFoodRate // "식사시간"
            var calendarRate = CalendarRate(title, rate)
            data.add(calendarRate) // data 리스트에 추가부분
        }
//        Log.d("home33", "user4 : ${calendarDataList}")
//        Log.d("home33", "calendarTodayItemCountCur : ${calendarTodayItemCountCur}")
        return data
    }



}













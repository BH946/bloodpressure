package mica.part1.checkMate.TodayMission

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import mica.part1.checkMate.SharedPreference.MyApplication
import mica.part1.checkMate.TodayMission.Adapter.TodayMissionTitleAdapter
import mica.part1.checkMate.TodayMission.DataClass.TodayMissionTitle
import mica.part1.checkMate.TodayMission.Room.User2Database
import mica.part1.checkMate.TodayMission.TodayMissionOption.TodayMissionOptionActivity
import mica.part1.checkMate.databinding.FragmentTodayMissionBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class TodayMissionFragment : Fragment() {
    // 전역 바인딩
    lateinit var binding: FragmentTodayMissionBinding
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodayMissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 상태바 색상 변경
        activity?.window?.statusBarColor = Color.parseColor("#DEF8FF") // bluelayout
        activity?.window?.decorView?.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) // 검정색


        // 설정 버튼 클릭시
        binding.optionButton.setOnClickListener {
            context?.let{
                val intent = Intent(it, TodayMissionOptionActivity::class.java)
                startActivity(intent)
            }
        }

        // 리사이클러뷰
        // 1. 데이터 로딩
        var data = loadData()
        // 2. 어댑터 생성
        var adapter = TodayMissionTitleAdapter(data) // 생성자 전역 만들었으니 data넘기기
        // 4. 화면에 있는 리사이클러뷰에 어댑터 연결
        binding.recyclerview.adapter = adapter
        // 5. 레이아웃 매니저 연결
        binding.recyclerview.layoutManager = LinearLayoutManager(binding.root.context)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadData() : MutableList<TodayMissionTitle> {
        var data : MutableList<TodayMissionTitle> = mutableListOf()
        var db2 : User2Database? = User2Database.getInstance(binding.root.context)
        var dataList = db2?.user2Dao()?.getAll() // db데이터
        val now = System.currentTimeMillis()
        val date = Date(now)
        val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
        val mFormat = SimpleDateFormat("E요일", Locale.KOREAN)
        mFormat.setTimeZone(tz) //DateFormat에 TimeZone 설정
        var currentDate = mFormat.format(date).toString()

        var todayItemCount : Int = 0 // 총 아이템 개수
        var exerciseCount : Int = 0 // 총 운동아이템 개수
        var approximatelyCount : Int = 0 // 총 약아이템 개수
        var bloodPressureCount : Int = 0 // 총 혈압아이템 개수
        var foodCount : Int = 0 // 총 식사아이템 개수

        // 약 복용시간, 혈압 측정시간, 식사시간, 운동시간 이 DB데이터에 있는지 확인 (제목, 요일 비교)
        try {
            for (i in 0..dataList!!.size) {
                if((dataList[i].everydayCheck == 1 && dataList[i].title.equals("약 복용시간")) ||
                    (dataList[i].title.equals("약 복용시간") && dataList[i].dayOfWeek.equals(currentDate))) {
                    data.add(TodayMissionTitle("오늘의 약 복용", dataList[i].time))
                    todayItemCount++
                    approximatelyCount++
                }
                    
                else if ((dataList[i].everydayCheck == 1 && dataList[i].title.equals("혈압 측정시간")) ||
                        (dataList[i].title.equals("혈압 측정시간") && dataList[i].dayOfWeek.equals(currentDate))) {
                    data.add(TodayMissionTitle("오늘의 혈압측정", dataList[i].time))
                    todayItemCount++
                    bloodPressureCount++
                }
                    
                else if ((dataList[i].everydayCheck == 1 && dataList[i].title.equals("식사시간")) ||
                        (dataList[i].title.equals("식사시간") && dataList[i].dayOfWeek.equals(currentDate))){
                    data.add(TodayMissionTitle("오늘의 식사시간", dataList[i].time))
                    todayItemCount++
                    foodCount++
                }
                else if ((dataList[i].everydayCheck == 1 && dataList[i].title.equals("운동시간")) ||
                        (dataList[i].title.equals("운동시간") && dataList[i].dayOfWeek.equals(currentDate))) {
                    data.add(TodayMissionTitle("오늘의 운동", dataList[i].time))
                    todayItemCount++
                    exerciseCount++
                }
                }
            } catch ( e: IndexOutOfBoundsException){}
        MyApplication.prefs.setString("todayItemCount", "${todayItemCount}") // Item총개수 저장
        // 4가지 경우로 전부 만들어 두기.
        MyApplication.prefs.setString("exerciseCount", "${exerciseCount}") // 운동 아이템 총개수 저장
        MyApplication.prefs.setString("approximatelyCount", "${approximatelyCount}") // 약 복용 아이템 총개수 저장
        MyApplication.prefs.setString("bloodPressureCount", "${bloodPressureCount}") // 혈압 아이템 총개수 저장
        MyApplication.prefs.setString("foodCount", "${foodCount}") // 식사 아이템 총개수 저장


        return data
    }

}
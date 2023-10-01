package mica.part1.checkMate.TodayMission.TodayMissionOption

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import mica.part1.checkMate.MainActivity
import mica.part1.checkMate.TodayMission.TodayMissionOption.Adapter.ExpandableAdapter
import mica.part1.checkMate.TodayMission.TodayMissionOption.DataClass.TodayMissionOption
import mica.part1.checkMate.databinding.ActivityTodayMissionOptionBinding

// https://android-dev.tistory.com/59 => UI형성
// https://kumgo1d.tistory.com/44 => RecyclerView에서 Intent사용법
// 이중(중첩) recyclerVeiw 만드는 법
// 1. recyclerViewA를 만든다
// 2. recyclerViewA 어답터에서 ViewHolder에 recyclerViewB를 정의해준다.

class TodayMissionOptionActivity : AppCompatActivity() {
    val binding by lazy { ActivityTodayMissionOptionBinding.inflate(layoutInflater) }
    private lateinit var todayMissionList: MutableList<TodayMissionOption> // 1. 데이터 생성
    private lateinit var adapter: ExpandableAdapter // 2. 어댑터 생성

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        resultButtonClick() // 버튼클릭 함수
        // 상태바 색상 변경
        window.statusBarColor = Color.parseColor("#DEF8FF") // bluelayout
        window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) // 검정색

        val recyclerView = binding.todayMissionRecyclerView

        // 1. 데이터 로딩(생성)
        todayMissionList = loadData()
        // 2. 어댑터 생성
        adapter = ExpandableAdapter(getApplicationContext(), todayMissionList)
        // 3. 화면에 있는 리사이클러뷰에 어댑터 연결
        recyclerView.adapter = adapter
        // 4. 레이아웃 매니저 연결
        recyclerView.setHasFixedSize(true) // ????이건뭐야
        recyclerView.layoutManager = LinearLayoutManager(this)


    }
    // TodayMissionOption클래스가 담겨있는 목록을 리턴해주는 함수를 만들기. => room으로 바꿀시 데이터 수정!
    private fun loadData(): MutableList<TodayMissionOption> {
        var data:MutableList<TodayMissionOption> = mutableListOf()
        var titleList:MutableList<String> = mutableListOf("약 복용시간", "혈압 측정시간", "식사시간", "운동시간") // 제목 초기화
        for(i in 0..titleList.size-1){ // 4번반복
            val todayMissionOption = TodayMissionOption(titleList[i])
            data.add(todayMissionOption) // data 리스트에 추가부분
        }
        return data
    }

    private fun resultButtonClick() {
        binding.resultButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}


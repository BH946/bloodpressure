package mica.part1.checkMate.TodayMission.TodayMissionOption.Adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mica.part1.checkMate.R
import mica.part1.checkMate.TodayMission.*
import mica.part1.checkMate.TodayMission.Room.User2Database
import mica.part1.checkMate.TodayMission.TodayMissionOption.DataClass.TodayMissionOptionInner
import mica.part1.checkMate.TodayMission.TodayMissionOption.DataClass.TodayMissionOption
import mica.part1.checkMate.TodayMission.TodayMissionOption.ToggleAnimation
import mica.part1.checkMate.databinding.ItemTimeBinding


// onBindViewHolder에서 item구성한 xml의 id들을 이용해서 버튼클릭이라던지 등등 여기서 하면됨.
class ExpandableAdapter(private val context: Context, val listData : MutableList<TodayMissionOption>) : RecyclerView.Adapter<ExpandableAdapter.Holder>() {

    class Holder(val binding: ItemTimeBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var missionTime: MutableList<TodayMissionOptionInner> // 1. 데이터 생성
        var db2 : User2Database? = User2Database.getInstance(binding.root.context)


        fun bind(todayMission : TodayMissionOption) {
            val textTitle = binding.itemMissionTextView // 제목
            val imgMoreButton = binding.itemMissionMoreImageButton // 레아이웃 더보기 버튼
            val layoutExpand = binding.itemMissionMoreLayout // 숨겨진 레이아웃

            textTitle.text = todayMission.title

            // 숨겨진 레이아웃 보는 기능 1
            imgMoreButton.setOnClickListener {
                val show = toggleLayout(!todayMission.isExpanded, it, layoutExpand)
                todayMission.isExpanded = show

            }

            // inner리사이클러뷰
            missionTime = loadData()
            val s = binding.itemTimeRecyclerView
            val mAdapter = InnerExpandableAdapter(binding.root.context, missionTime)
            s.adapter = mAdapter
            s.setHasFixedSize(true)
            s.layoutManager = LinearLayoutManager(binding.root.context)

        }
        // MissionTime클래스의 형식으로 데이터 로드하는 함수 만들어 주기 - inner리사이클러뷰 => room형식 데이터 수정
        // 해당 리사이클러뷰 아이템에서 해당 체크를 클릭할시 db2에 해당 데이터를 저장,, title, day, time 전부 비교해서 인덱스만구해서 해당 인덱스에 everydayCheck값만 변경
        private fun loadData(): MutableList<TodayMissionOptionInner> {
            val data:MutableList<TodayMissionOptionInner> = mutableListOf() // return할 data
            var dataList = db2?.user2Dao()?.getAll() // db데이터
            val itemTimeTitle = binding.itemMissionTextView.text // 비교할 title
            try {
                for (i in 0..dataList!!.size) {
                    if(itemTimeTitle == dataList[i].title) {
                        val title = dataList[i].title
                        val dayOfTheWeek = dataList[i].dayOfWeek
                        val time = dataList[i].time
                        val everydayCheck = dataList[i].everydayCheck
                        val todyMissionOptionInner = TodayMissionOptionInner(title, dayOfTheWeek, time, everydayCheck)
                        data.add(todyMissionOptionInner) // data 리스트에 추가부분
                    }
                }
            }catch ( e: IndexOutOfBoundsException){}
            return data
        }


        // 숨겨진 레이아웃 보는 기능 2
        // 1. 처음에 화살표 이미지를 클릭하면 toggleLayout() 함수를 호출하고, togglerLayout() 함수를 통해 expand와 collpase를 구현합니다.
        // 2. ToggleAnimation의 toggleArrow를 호출합니다. 해당 메소드는 isExpaned의 여부에 따라 화살표를 회전합니다.
        // 이부분은 그냥 인터넷 참고함.
        private fun toggleLayout(isExpanded: Boolean, view: View, layoutExpand: ConstraintLayout): Boolean{
            ToggleAnimation.toggleArrow(view, isExpanded)
            if (isExpanded) {
                ToggleAnimation.expand(layoutExpand) // 확장 부분
                binding.constraint.background = getDrawable(binding.root.context, R.drawable.ic_corners_button2)
                binding.itemMissionTextView.setTextColor(Color.parseColor("#FFFFFFFF")) // title텍스트 색상변경(흰색)
                binding.itemMissionMoreImageButton.setBackgroundColor(Color.parseColor("#F2571D")) // 드롭단추 배경 색상변경(오렌지)
                binding.itemMissionMoreImageButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFFFF")) // 드롭단추 색상변경(흰색)

            } else {
                ToggleAnimation.collapse(layoutExpand) // 확장반대 부분
                binding.constraint.background = getDrawable(binding.root.context, R.drawable.ic_corners_layout)
                binding.itemMissionTextView.setTextColor(Color.parseColor("#5A5A5A")) // title텍스트 색상변경(원래색)
                binding.itemMissionMoreImageButton.setBackgroundColor(Color.parseColor("#FFFFFFFF")) // 드롭단추 배경 색상변경(흰색)
                binding.itemMissionMoreImageButton.imageTintList = ColorStateList.valueOf(Color.parseColor("#757575")) // 드롭단추 색상변경(원래색)
            }
            return isExpanded
        }
    }

    // 기본 recyclerView 오버라이드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // 홀더를 만드는 녀석이다. (아이템을 클래스화 시켜서 홀더에 던져주는 녀석)
        return Holder(ItemTimeBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        // 실제로 create된곳에 값을 세팅하기위해 사용되는 것
        // 1. 사용할 데이터를 꺼내고
        val data = listData.get(position)
        // 2. 홀더에 데이터를 전달
        holder.bind(data) // 위에서 만든 bind함수에 전달
        // + 버튼 클릭
        holder.binding.TimeOptionImageButton.setOnClickListener {
            val intent = Intent(holder.binding.TimeOptionImageButton?.context, TodayMissionTimeOptionActivity::class.java)
            intent.putExtra("title", holder.binding.itemMissionTextView.text) // "title(예: 약 복용시간)
            startActivity(holder.binding.TimeOptionImageButton.context, intent, null)
        }
    }
    override fun getItemCount(): Int {
        // 안드로이드한테 니가 사용할 아이템 개수가 몇개냐 묻는것
        return listData.size
    }
}

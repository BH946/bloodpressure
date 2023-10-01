package mica.part1.checkMate.TodayMission.Adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import mica.part1.checkMate.HealthReport.Room.User4
import mica.part1.checkMate.HealthReport.Room.User4Database
import mica.part1.checkMate.SharedPreference.MyApplication
import mica.part1.checkMate.R
import mica.part1.checkMate.TodayMission.DataClass.TodayMissionTitle
import mica.part1.checkMate.TodayMission.Room.User3
import mica.part1.checkMate.TodayMission.Room.User3Database
import mica.part1.checkMate.databinding.ItemMissionBinding
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class TodayMissionTitleAdapter(val listData : MutableList<TodayMissionTitle>) : RecyclerView.Adapter<TodayMissionTitleAdapter.Holder>() {

    class Holder(val binding : ItemMissionBinding) : RecyclerView.ViewHolder(binding.root){
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(todayMissionTitle : TodayMissionTitle) {
            binding.titleTextView.text = todayMissionTitle.title
            var db3 : User3Database? =  User3Database.getInstance(binding.root.context) // 아이템 위한 db
            var db4 : User4Database? = User4Database.getInstance(binding.root.context) // 캘린더 위한 db
            var successItemList = db3?.user3Dao()?.getAll()

            // 먼저 시간 초기화
            var hour = todayMissionTitle.time.slice(IntRange(0,1)).toInt() // int로 바꿔주면 01->1로 자동 변함
            var minute = todayMissionTitle.time.slice(IntRange(3,4)).toInt()
            val now = System.currentTimeMillis()
            val date = Date(now)
            val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
            val mFormatHour = SimpleDateFormat("HH", Locale.KOREAN)
            mFormatHour.setTimeZone(tz) //DateFormat에 TimeZone 설정
            val mFormatMinute = SimpleDateFormat("mm", Locale.KOREAN)
            mFormatMinute.setTimeZone(tz) //DateFormat에 TimeZone 설정
            var currentHour = mFormatHour.format(date).toInt()
            var currentMinute = mFormatMinute.format(date).toInt()

            // 조건문 해서 조건문 충족시 버튼보이게 하기.
            if( (currentHour > hour) || (currentHour == hour && currentMinute >= minute)) {
                binding.checkImageButton.isClickable = true // 클릭가능
                binding.checkImageButton.isInvisible = false // 보이게 하기 (visible이 보이는거일거임)
            }
            // 저장한 데이터가지고, 현재 title과 time을 비교하는데 아래 조건문에 넣을려면 둘다 != 여야함.
            try {
                for (i in 0..successItemList!!.size-1) { // size가 여긴 0이 아니여야 실행될것이다.
                    if(successItemList!![i].title == todayMissionTitle.title && successItemList!![i].time == todayMissionTitle.time) {
                        changeView() // 색 변경
                    }
                }
            }catch (e : IndexOutOfBoundsException) { } // db가 index 넘어간 경우 여기선 넘어갈 경우가 없긴 하다.

            // 완료 체크 버튼 클릭 기능
            checkImageButton(todayMissionTitle,db3,db4)
            // db4를 리프레쉬하는 부분
            initDatabase4(db4)

        }


        @RequiresApi(Build.VERSION_CODES.O)
        fun checkImageButton(todayMissionTitle: TodayMissionTitle, db3 : User3Database?, db4 : User4Database?) {
            binding.checkImageButton.setOnClickListener {
                changeView() // 색 변경

                // 데이터 기록 현재 총 아이템
                totalItemCountCur()
                // 조건문으로 각 타이틀 마다 현재 완료한 아이템 개수 따로 저장.
                if (todayMissionTitle.title.equals("오늘의 운동")) {
                    // 데이터 기록 현재 운동 아이템
                    exerciseCountCur()
                } else if (todayMissionTitle.title.equals("오늘의 약 복용")){
                    // 데이터 기록 현재 약 복용 아이템
                    approximatelyCountCur()
                } else if (todayMissionTitle.title.equals("오늘의 혈압측정")) {
                    // 데이터 기록 현재 혈압 아이템
                    bloodPressureCountCur()
                } else if (todayMissionTitle.title.equals("오늘의 식사시간")) {
                    // 데이터 기록 현재 식사 아이템
                    foodCountCur()
                }

                // db에 완료한 데이터 기록
                db3?.user3Dao()?.insert(User3(todayMissionTitle.title, todayMissionTitle.time))

                // 여기서 캘린더를 위해 room에 하루기준으로 데이터 계산해서 저장해야함!! => 이걸로 이달의 미션달성 등등 퍼센트 캘린더에 나타내기~!!!
                initDatabase4(db4)
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

        // 뷰체인지
        fun changeView() {
            binding.checkImageButton.isClickable = false // 클릭불가
            binding.checkImageButton.isInvisible = true // 안보이게 하기
            binding.checkImageView.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFFFF")) // 체크표시 흰색
            binding.checkImageView.background = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_corners_check_circle2) // 배경 변경
            binding.titleTextView.setTextColor(Color.parseColor("#FFFFFFFF")) // 글자 흰색
            binding.constraint.background = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_corners_layout_red_cc) // 배경 변경
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder {
        return Holder(ItemMissionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        // 1. 사용할 데이터를 꺼내고
        val data = listData.get(position)
        // 2. 홀더에 데이터를 전달
        holder.bind(data) // 위에서 만든 bind함수에 전달
    }

    override fun getItemCount(): Int {
        return listData.size
    }
}
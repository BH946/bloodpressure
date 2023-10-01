package mica.part1.checkMate.TodayMission

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mica.part1.checkMate.*
import mica.part1.checkMate.SharedPreference.MyApplication
import mica.part1.checkMate.TodayMission.Room.User2
import mica.part1.checkMate.TodayMission.Room.User2Database
import mica.part1.checkMate.TodayMission.Room.User3Database
import mica.part1.checkMate.TodayMission.TodayMissionOption.TodayMissionOptionActivity
import mica.part1.checkMate.TodayMission.TodayMissionOption.TodayMissionTimeOption.DataClass.MissionTimeOption
import mica.part1.checkMate.databinding.ActivityTodayMissionTimeOptionBinding
import mica.part1.checkMate.databinding.ItemTimeOptionBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class TodayMissionTimeOptionActivity : AppCompatActivity() {

    val binding by lazy {ActivityTodayMissionTimeOptionBinding.inflate(layoutInflater)}
    val spinnerItemList = listOf("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일")
    var positions : Int = 0
    var dataList:MutableList<MissionTimeOption> = mutableListOf() // 추가하는 dataList임
    // 알람설정에 사용할 변수 전역 초기화
    var codeOne : Int = MyApplication.prefs.getString("codeOne", "0").toInt()
    var codeTwo : Int = MyApplication.prefs.getString("codeTwo", "0").toInt()
    var codeThree : Int = MyApplication.prefs.getString("codeThree", "0").toInt()
    var codeFour : Int = MyApplication.prefs.getString("codeFour", "0").toInt()

        @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // 상태바 색상 변경
        window.statusBarColor = Color.parseColor("#EA5946") // redAlarm
        window.decorView.setSystemUiVisibility(0) // 흰색

        var db2 : User2Database? = User2Database.getInstance(this)
        // 들어간 아이템 타이틀 설정
            var completeItemTitle = intent.getStringExtra("title")!!
            if (completeItemTitle.equals("약 복용시간")) 
                completeItemTitle = "오늘의 약 복용"
            else if (completeItemTitle.equals("혈압 측정시간"))
                completeItemTitle = "오늘의 혈압측정"
            else if (completeItemTitle.equals("식사시간"))
                completeItemTitle = "오늘의 식사시간"
            else if (completeItemTitle.equals("운동시간"))
                completeItemTitle = "오늘의 운동"
            MyApplication.prefs.setString("completeItemTitle", completeItemTitle)
            
        // recyclerView 부분 설정 시작
        // 1. 데이터 로딩
        var data = loadData(db2) // db2도 같이 보내줌으로써 데이터 적용
        // 2. 어댑터 생성
        var customadapter = CustomAdapter(data) // 생성자 전역 만들었으니 data넘기기
        // 4. 화면에 있는 리사이클러뷰에 어댑터 연결
        binding.recyclerview.adapter = customadapter
        // 5. 레이아웃 매니저 연결
        binding.recyclerview.layoutManager = LinearLayoutManager(this)


        // 뒤로가기 버튼 클릭시 뒤로가게 하기.
        binding.backButton.setOnClickListener {
            val intent = Intent(this, TodayMissionOptionActivity::class.java)
            startActivity(intent)
        }
        // 아이템 클릭시 삭제 부분 -> 이부분과 맨아래에 코드 필요 https://mechacat.tistory.com/7
        customadapter.setItemClickListener(object : CustomAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                dataList.removeAt(position)
                customadapter.notifyDataSetChanged()
            }
        })

        // spinner 부분 설정
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,spinnerItemList)
        binding.spinner.adapter = adapter // spinner 어뎁터를 연결
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected( // 선택된값
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                positions = position
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { // 아무것도 선택안했을시 보여주는 값? 비워두기
            }
        }

        // 추가 버튼 클릭시
        // spinner의 값과, timePicker의 값을 받아와서 저장하기 => room으로 고고 => 이러면 총 2개의 room이 있는것(혈압, 시간)
        binding.adderButton.setOnClickListener {
            adderButtonClick()
            // 추가버튼시 recyclerView에 추가되는 기능 구현
            var dayOfWeek = spinnerItemList[positions]
            var hour : String
            if(binding.timePicker.hour <10) hour = "0${binding.timePicker.hour}" else hour = binding.timePicker.hour.toString()
            var minute : String
            if(binding.timePicker.minute < 10) minute = "0${binding.timePicker.minute}" else minute = binding.timePicker.minute.toString()
            var everydayCheck : Int = 0 // 그냥 기본값 설정해서 추가 (적용버튼에서 수정될거임)

            var missionTimeOption = MissionTimeOption(dayOfWeek, hour, minute, everydayCheck)
            var count : Int = 0
            try {
                for(i in 0..dataList.size-1){
                    if(missionTimeOption.dayOfWeek == dataList[i].dayOfWeek && missionTimeOption.hour == dataList[i].hour && missionTimeOption.minute == dataList[i].minute){
                        count = 1 // 동일한 시간 설정시 추가 x
                        break
                    }
                }
            }catch (e: IndexOutOfBoundsException){}
            if (count == 0)
                dataList.add(missionTimeOption)
            customadapter.notifyDataSetChanged() // recyclerView 화면 갱신

        }

        // 적용 버튼 클릭시
        binding.insertButton.setOnClickListener {
            // db 와 같은 title 지워주는것!
            var title = intent.getStringExtra("title")!!
            var titleList = db2?.user2Dao()?.getAll()
            try {
                for(i in 0..titleList!!.size-1) {
                    if(title == titleList!![i].title) { // db2와 같은 title이며,
                        for ( j in 0..dataList.size-1) {
                            if(titleList[i].time.equals("${dataList[j].hour}:${dataList[j].minute}")
                                && titleList[i].dayOfWeek.equals(dataList[j].dayOfWeek)
                                && titleList[i].everydayCheck == 1) // 매주 체크되어있는것들이라면 + 요일, 시간 같다면 => dataList[i].everydayCheck수정
                                dataList[j].everydayCheck == 1 // 추가버튼에서 그냥 체크 0으로 초기화 했던것을 여기서 수정한것
                        }
                        db2?.user2Dao()?.delete(titleList[i]) // db2에 데이터는 전부 삭제 할것임.(같은 title만)
                    }
                }
            }catch (e:IndexOutOfBoundsException){}
            // db에 데이터 저장해주는것~!
            for(i in 0..dataList.size-1) {
                // title을 item에서 intent로 값넘겨서 받아온다.
                // 그다음 +시간 적용 하는것을 각 리사이클러뷰 아이템마다 title마다 따로 적용하는 부분 필요하다.
                var title = intent.getStringExtra("title")!!
//                var title = bindingPrevViewItem.itemMissionTextView.text.toString()
                var dayOfWeek = dataList[i].dayOfWeek
                var time = "${dataList[i].hour}:${dataList[i].minute}"
                var everydayCheck = dataList[i].everydayCheck
                db2?.user2Dao()?.insert(User2(title, dayOfWeek, time, everydayCheck))
            }

            // 알람 삭제 후 초기화
            deleteAlarm()
            initOnAlarm()

            val intent = Intent(this, TodayMissionOptionActivity::class.java)
            startActivity(intent)
        }
    }


    // MissionTimeOption클래스가 담겨있는 목록을 리턴해주는 함수를 만들기.
    //         이부분에 room에서 데이터 가져와서 수정하게끔 수정!====================!!!!!!!!!!
    fun loadData(db2 : User2Database?) : MutableList<MissionTimeOption> {

        // db와 같은 title인 경우들의 데이터를 로딩시키자.
        var title = intent.getStringExtra("title")!!
        var titleList = db2?.user2Dao()?.getAll()
        try {
            for(i in 0..titleList!!.size-1) {
                if(title == titleList!![i].title) {
                    var findIndex = titleList[i].time.indexOf(':')
                    var dayOfWeek = titleList[i].dayOfWeek
                    var hour : String = titleList[i].time.slice(IntRange(0,findIndex-1))
                    var minute : String = titleList[i].time.slice(IntRange(findIndex+1, titleList[i].time.length-1))
                    var everydayCheck : Int = titleList[i].everydayCheck
                    var missionTimeOption = MissionTimeOption(dayOfWeek, hour, minute, everydayCheck)
                    dataList.add(missionTimeOption)
                }
            }
        }catch (e:IndexOutOfBoundsException){}

        return dataList
        }

    private fun adderButtonClick() {
        // spinner의 값과, timePicker의 값을 받아와서 저장하기 => room으로 고고 => 이러면 총 2개의 room이 있는것(혈압, 시간)
        val dayOfWeek = spinnerItemList[positions]
        var hour : String
        if(binding.timePicker.hour <10) hour = "0${binding.timePicker.hour}" else hour = binding.timePicker.hour.toString()
        var minute : String
        if(binding.timePicker.minute < 10) minute = "0" + binding.timePicker.minute.toString() else minute = binding.timePicker.minute.toString()
    }



    // 알람 설정
    private fun deleteAlarm() {
        for (code in 0..codeOne)
            cancelAlarm(code)
        for (code in 0..codeTwo)
            cancelAlarm(code)
        for (code in 0..codeThree)
            cancelAlarm(code)
        for (code in 0..codeFour)
            cancelAlarm(code)
//        Log.d("home22","deleteAlarm ${codeOne}, ${codeTwo}, ${codeThree}, ${codeFour}")
    }


    // 알람 켜기 끄기 버튼. as?는 형변환 시키는 것임(?는 널처리)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initOnAlarm() {

        // 저장한 데이터를 확인한다
        val db : User2Database? = User2Database.getInstance(this) // 시간데이터 가져오기
        val dataList = db?.user2Dao()?.getAll() // 시간리스트

        // 금일 데이터로 변경 => 온 => 알람을 등록
        // 시간 추가하는곳에서 적용 버튼 눌렀을시 알람 지금 이부분 실행시키기 (그전에 알람기존 삭제해주고)
        // 매일 알람반복 체크되어있는 시간 있다면, 그녀석도 추가해주기 여기서.
        // => everydayCheck 가 1인 경우도 or로 if문에 추가
        // 매일 반복에 체크 했을 때도 알람 이부분 실행해야 할 듯. => 상관없음. 그런데 다음날 어떻게 이소스부분 시행하는거지?
        // 애초에 일주일치로 짜야할거 같음.. 금일 알람만 울리는게 아니라..

        // 아니면 일주일치 알람 집어넣고. 해당시간이 되면 리시버로 넘어가므로 리시버에서 요일비교하고 금일 요일이면 알람울리게하고 아니면 안울리게하기. ok
        // => 메인의 00시또한 그냥 시간 알람에 추가해서 00시에 리시버로가면 해당요일이든 아니든 일단 데이터초기화부분 발동하게 작성하면 될듯..!? ok
        // => + 이 00시에 초기화 뿐만아니라 알람재설정 하는것도 좋을듯..!! ok
        codeOne = 100
        codeTwo = 200
        codeThree = 300
        codeFour = 400
        try {
            for (i in 0..dataList!!.size) { // db2데이터
                if(dataList[i].title.equals("약 복용시간") || (dataList[i].title.equals("약 복용시간") && dataList[i].everydayCheck==1)) {
//                    Log.d("home555", "first : ${dataList[i].time} ${dataList[i].dayOfWeek}")
                    val time = dataList[i].time
                    val hour = dataList[i].time.slice(IntRange(0,1)).toInt() // int로 바꿔주면 01->1로 자동 변함
                    val minute = dataList[i].time.slice(IntRange(3,4)).toInt()
                    val dayOfWeek = dataList[i].dayOfWeek
                    val calender = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour) // 시간 설정
                        set(Calendar.MINUTE, minute) // 분 설정
                        // 지나간 시간의 경우 다음날 알람으로 울리도록 함으로써 그냥 안울리게 하기.
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DATE, 1) // 하루 더하기
                        }
                    }
                    //알람 매니저 가져오기.
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val intent = Intent(this, AlarmReceiver::class.java)
                    intent.putExtra("key", "approximately")
                    intent.putExtra("code", codeOne)
                    intent.putExtra("alertTime", time)
                    intent.putExtra("alertDayOfWeek", dayOfWeek)
                    intent.putExtra("alertEverydayCheck", dataList[i].everydayCheck)
                    val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        codeOne, // id값이라 생각하고 이걸로 다수의 알람 등록
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT // 있으면 새로 만든거로 업데이트 ( 아마 해당시간 되면 이부분 발동 )
                    )
                    alarmManager.setAndAllowWhileIdle( // 정시에 반복 ( 잠자기 모드에서도 허용 )
                        AlarmManager.RTC_WAKEUP, // RTC_WAKEUP : 실제 시간 기준으로 wakeup , ELAPSED_REALTIME_WAKEUP : 부팅 시간 기준으로 wakeup
                        calender.timeInMillis, // 언제 알람이 발동할지.
                        pendingIntent
                    )
                    codeOne++
                }
                else if (dataList[i].title.equals("혈압 측정시간") || (dataList[i].title.equals("혈압 측정시간") && dataList[i].everydayCheck==1)) {
                    val time = dataList[i].time
                    val hour = dataList[i].time.slice(IntRange(0,1)).toInt()
                    val minute = dataList[i].time.slice(IntRange(3,4)).toInt()
                    val dayOfWeek = dataList[i].dayOfWeek
                    val calender = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour) // 시간 설정
                        set(Calendar.MINUTE, minute) // 분 설정
                        // 지나간 시간의 경우 다음날 알람으로 울리도록 함으로써 그냥 안울리게 하기.
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DATE, 1) // 하루 더하기
                        }
                    }
                    //알람 매니저 가져오기.
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val intent = Intent(this, AlarmReceiver::class.java)
                    intent.putExtra("key", "bloodPressure")
                    intent.putExtra("code", codeTwo)
                    intent.putExtra("alertTime", time)
                    intent.putExtra("alertDayOfWeek", dayOfWeek)
                    intent.putExtra("alertEverydayCheck", dataList[i].everydayCheck)
                    val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        codeTwo, // id값이라 생각하고 이걸로 다수의 알람 등록
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT // 있으면 새로 만든거로 업데이트
                    )
                    alarmManager.setAndAllowWhileIdle( // 정시에 반복 ( 잠자기 모드에서도 허용 )
                        AlarmManager.RTC_WAKEUP, // RTC_WAKEUP : 실제 시간 기준으로 wakeup , ELAPSED_REALTIME_WAKEUP : 부팅 시간 기준으로 wakeup
                        calender.timeInMillis, // 언제 알람이 발동할지.
                        pendingIntent
                    )
                    codeTwo++
                }
                else if (dataList[i].title.equals("식사시간") || (dataList[i].title.equals("식사시간") && dataList[i].everydayCheck==1)) {
                    val time = dataList[i].time
                    val hour = dataList[i].time.slice(IntRange(0,1)).toInt()
                    val minute = dataList[i].time.slice(IntRange(3,4)).toInt()
                    val dayOfWeek = dataList[i].dayOfWeek
                    val calender = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour) // 시간 설정
                        set(Calendar.MINUTE, minute) // 분 설정
                        // 지나간 시간의 경우 다음날 알람으로 울리도록 함으로써 그냥 안울리게 하기.
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DATE, 1) // 하루 더하기
                        }
                    }
                    //알람 매니저 가져오기.
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val intent = Intent(this, AlarmReceiver::class.java)
                    intent.putExtra("key", "food")
                    intent.putExtra("code", codeThree)
                    intent.putExtra("alertTime", time)
                    intent.putExtra("alertDayOfWeek", dayOfWeek)
                    intent.putExtra("alertEverydayCheck", dataList[i].everydayCheck)
                    val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        codeThree, // id값이라 생각하고 이걸로 다수의 알람 등록
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT // 있으면 새로 만든거로 업데이트
                    )
                    alarmManager.setAndAllowWhileIdle( // 정시에 반복 ( 잠자기 모드에서도 허용 )
                        AlarmManager.RTC_WAKEUP, // RTC_WAKEUP : 실제 시간 기준으로 wakeup , ELAPSED_REALTIME_WAKEUP : 부팅 시간 기준으로 wakeup
                        calender.timeInMillis, // 언제 알람이 발동할지.
                        pendingIntent
                    )
                    codeThree++
                }
                else if (dataList[i].title.equals("운동시간") || (dataList[i].title.equals("운동시간") && dataList[i].everydayCheck==1)) {
                    val time = dataList[i].time
                    val hour = dataList[i].time.slice(IntRange(0,1)).toInt()
                    val minute = dataList[i].time.slice(IntRange(3,4)).toInt()
                    val dayOfWeek = dataList[i].dayOfWeek
                    val calender = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour) // 시간 설정
                        set(Calendar.MINUTE, minute) // 분 설정
                        // 지나간 시간의 경우 다음날 알람으로 울리도록 함으로써 그냥 안울리게 하기.
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DATE, 1) // 하루 더하기
                        }
                    }
                    //알람 매니저 가져오기.
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val intent = Intent(this, AlarmReceiver::class.java)
                    intent.putExtra("key", "exercise")
                    intent.putExtra("code", codeFour)
                    intent.putExtra("alertTime", time)
                    intent.putExtra("alertDayOfWeek", dayOfWeek)
                    intent.putExtra("alertEverydayCheck", dataList[i].everydayCheck)
                    val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        codeFour, // id값이라 생각하고 이걸로 다수의 알람 등록
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT // 있으면 새로 만든거로 업데이트
                    )
                    alarmManager.setAndAllowWhileIdle( // 정시에 반복 ( 잠자기 모드에서도 허용 )
                        AlarmManager.RTC_WAKEUP, // RTC_WAKEUP : 실제 시간 기준으로 wakeup , ELAPSED_REALTIME_WAKEUP : 부팅 시간 기준으로 wakeup
                        calender.timeInMillis, // 언제 알람이 발동할지.
                        pendingIntent
                    )
                    codeFour++
                }
            }
        } catch ( e: IndexOutOfBoundsException){} // 데이터 없는 경우니까 아무것도 안하면됨. 알람도 실행x
        // shaerd이용해서 저장
        MyApplication.prefs.setString("codeOne", "${codeOne}")
        MyApplication.prefs.setString("codeTwo", "${codeTwo}")
        MyApplication.prefs.setString("codeThree", "${codeThree}")
        MyApplication.prefs.setString("codeFour", "${codeFour}")

//        Log.d("home22","deleteAlarmIntent ${codeOne}, ${codeTwo}, ${codeThree}, ${codeFour}")

        // ++ 00시 코드 실행을 위해!! -> 폰 끈경우 등등도 생각한다면 00시가 아니라 휴대폰 부팅후 시간으로 변경해야 하기도 함.
        val hour = 2
        val minute = 46
        val calender = Calendar.getInstance().apply { // 00:00시간으로 설정
            set(Calendar.HOUR_OF_DAY, hour) // 시간 설정
            set(Calendar.MINUTE, minute) // 분 설정
            // 지나간 시간의 경우 다음날 알람으로 울리도록 함으로써 그냥 안울리게 하기.
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1) // 하루 더하기
            }
        }
        //알람 매니저 가져오기.
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("key", "newDay")
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1, // 코드값 1로 정하겠음.
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT // 있으면 새로 만든거로 업데이트 ( 아마 해당시간 되면 이부분 발동 )
        )
        alarmManager.setAndAllowWhileIdle( // 정시에 반복 ( 잠자기 모드에서도 허용 )
            AlarmManager.RTC_WAKEUP, // RTC_WAKEUP : 실제 시간 기준으로 wakeup , ELAPSED_REALTIME_WAKEUP : 부팅 시간 기준으로 wakeup
            calender.timeInMillis, // 언제 알람이 발동할지.
            pendingIntent
        )
    }

    private fun cancelAlarm(code : Int) {
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            code, // code
            Intent(this, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE) // 있으면 가져오고 없으면 안만든다 (null)
        pendingIntent?.cancel() // 기존 알람 삭제
    }


}


class CustomAdapter(val listData:MutableList<MissionTimeOption>) : RecyclerView.Adapter<CustomAdapter.Holder>() {

    class Holder(val binding: ItemTimeOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var currentData: MissionTimeOption
        // 클릭처리는 init에서만 한다. (왜냐? 드래그할때도 클릭으로 볼수 있기때문)
        init {
            binding.root.setOnClickListener{
                Toast.makeText(binding.root.context, "클릭된 아이템 : $currentData", Toast.LENGTH_SHORT).show()
            }
        }
        // 3. 받은 데이터를 화면에 출력한다.
        @RequiresApi(Build.VERSION_CODES.O)
        fun setData(data: MissionTimeOption){
            currentData = data
            // binding.하면서 사용하기위해선 전역으로 생성자 만들어줘야죠(따라서 val붙여주기)
            // with스코프 함수를 사용해서 코드를 줄여서 바인딩 사용가능@@!!
            with(binding){
                if ( data.hour.toInt() > 12 ) { // 시간이 12보다 크다면
                    // 토요일 오후 1시35분 이런식으로 넣기
                    var hour = data.hour.toInt()-12
                    var amPm = "오후"
                    TimeOptionTextView.text = "${data.dayOfWeek} ${amPm} ${hour}:${data.minute}"
                } else {
                    var hour = data.hour.toInt()
                    var amPm = "오전"
                    TimeOptionTextView.text = "${data.dayOfWeek} ${amPm} ${hour}:${data.minute}"
                }
            }
            // 아이템의 time들을 보고, db3와 같은 time이 있는것들은 -버튼 클릭 불가와 안보이게 하기.
            val now = System.currentTimeMillis()
            val date = Date(now)
            val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
            val mFormat = SimpleDateFormat("E요일", Locale.KOREAN)
            mFormat.setTimeZone(tz) //DateFormat에 TimeZone 설정
            var currentDate = mFormat.format(date).toString()

            var db3 : User3Database? = User3Database.getInstance(binding.root.context) // 이하 생략
            var db3List = db3?.user3Dao()?.getAll()



            for (i in 0..db3List!!.size-1) {
//                Log.d("home22", "${db3List[i].title}")
                if (currentData.hour.toInt() == db3List[i].time.slice(IntRange(0,1)).toInt() && currentData.minute.toInt() == db3List[i].time.slice(IntRange(3,4)).toInt()
                    && currentData.dayOfWeek == currentDate && db3List[i].title.equals(MyApplication.prefs.getString("completeItemTitle", "0"))) {
                    binding.TimeOptionImageButton.isClickable = false // 클릭불가능
                    binding.TimeOptionImageButton.isInvisible = true // 안보이게 하기 (visible이 보이는거일거임)
                }
            }
//            Log.d("home22", "${MyApplication.prefs.getString("completeItemTitle", "0")}")

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // 홀더를 만드는 녀석이다. (아이템을 클래스화 시켜서 홀더에 던져주는 녀석)
        val binding = ItemTimeOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }

    override fun getItemCount(): Int {
        // 안드로이드한테 니가 사용할 아이템 개수가 몇개냐 묻는것
        return listData.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        // 실제로 create된곳에 값을 세팅하기위해 사용되는 것, View에 내용입력
        // 1. 사용할 데이터를 꺼내고
        val data = listData.get(position)
        // 2. 홀더에 데이터를 전달
        holder.setData(data)
        
        // 클릭시 삭제를 위한 로직
        // (1) 리스트 내 항목 클릭 시 onClick() 호출 (itemView.setOnCli~~하면 뷰클릭시임
        holder.binding.TimeOptionImageButton.setOnClickListener {
            itemClickListener.onClick(it, position)
        }

    }
        // (2) 리스너 인터페이스
        interface OnItemClickListener {
            fun onClick(v: View, position: Int)
        }
        // (3) 외부에서 클릭 시 이벤트 설정
        fun setItemClickListener(onItemClickListener: OnItemClickListener) {
            this.itemClickListener = onItemClickListener
        }
        // (4) setItemClickListener로 설정한 함수 실행
        private lateinit var itemClickListener : OnItemClickListener
}

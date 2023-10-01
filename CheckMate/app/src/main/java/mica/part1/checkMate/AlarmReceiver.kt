package mica.part1.checkMate

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import mica.part1.checkMate.SharedPreference.MyApplication
import mica.part1.checkMate.TodayMission.Room.User2Database
import mica.part1.checkMate.TodayMission.Room.User3Database
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    var codeOne : Int = MyApplication.prefs.getString("codeOne", "0").toInt()
    var codeTwo : Int = MyApplication.prefs.getString("codeTwo", "0").toInt()
    var codeThree : Int = MyApplication.prefs.getString("codeThree", "0").toInt()
    var codeFour : Int = MyApplication.prefs.getString("codeFour", "0").toInt()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
        val mFormat = SimpleDateFormat("E요일", Locale.KOREAN)
        mFormat.setTimeZone(tz) //DateFormat에 TimeZone 설정
        var currentDate = mFormat.format(date).toString()

        val alertIntent = Intent(context, AlertActivity::class.java) // alert로 가는 intent 설정
        var name : String = intent.getStringExtra("key").toString() // 키 값
        var time : String = intent.getStringExtra("alertTime").toString()
        var dayOfWeek : String = intent.getStringExtra("alertDayOfWeek").toString() // 해당 알람의 요일값
        var everydayCheck : Int = intent.getIntExtra("alertEverydayCheck", 0) // 기본값은 0으로.

//        Log.d("home555", "second : ${name} ${time} ${dayOfWeek}")
        if (everydayCheck == 1 || (name.equals("approximately") && dayOfWeek.equals(currentDate))) { // 금일 요일인 알람만 실행
//            Log.d("home555", "third : ${name} ${dayOfWeek}")
            var codeOne : Int = intent.getIntExtra("code", 0)
            createNotificationChannelApproximately(context, codeOne) // 채널 생성
            notifyNotificationApproximately(context, codeOne, alertIntent, time) // 알림
        }
        else if (everydayCheck == 1 || (name.equals("bloodPressure") && dayOfWeek.equals(currentDate))) {
            var codeTwo : Int = intent.getIntExtra("code", 0)
            createNotificationChannelBloodPressure(context, codeTwo) // 채널 생성
            notifyNotificationBloodPressure(context, codeTwo, alertIntent, time) // 알림
        }
        else if (everydayCheck == 1 || (name.equals("food") && dayOfWeek.equals(currentDate))) {
            var codeThree : Int = intent.getIntExtra("code", 0)
            createNotificationChannelFood(context, codeThree) // 채널 생성
            notifyNotificationFood(context, codeThree, alertIntent, time) // 알림
        }
        else if (everydayCheck == 1 || (name.equals("exercise") && dayOfWeek.equals(currentDate))) {
            var codeFour : Int = intent.getIntExtra("code", 0)
            createNotificationChannelExercise(context, codeFour) // 채널 생성
            notifyNotificationExercise(context, codeFour, alertIntent, time) // 알림
        }
        else if (name.equals("newDay")) {
//            Log.d("home555", "newDay 발동! 데이터 초기화되는것은 확인완료! everydayCheck 확인하기!!")
            // 데이터 초기화
            initDataReset(context)
            // 알람 삭제 후 초기화
            deleteAlarm(context)
            initOnAlarm(context)

        }

    }


    // 알람 설정
    private fun deleteAlarm(context: Context) {
        for (code in 0..codeOne)
            cancelAlarm(code, context)
        for (code in 0..codeTwo)
            cancelAlarm(code, context)
        for (code in 0..codeThree)
            cancelAlarm(code, context)
        for (code in 0..codeFour)
            cancelAlarm(code, context)
//        Log.d("home22","deleteAlarm ${codeOne}, ${codeTwo}, ${codeThree}, ${codeFour}")
    }

    private fun cancelAlarm(code : Int, context: Context) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            code, // code
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE) // 있으면 가져오고 없으면 안만든다 (null)
        pendingIntent?.cancel() // 기존 알람 삭제
    }


    // 알람 켜기 끄기 버튼. as?는 형변환 시키는 것임(?는 널처리)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initOnAlarm(context: Context) {

        // 저장한 데이터를 확인한다
        val db : User2Database? = User2Database.getInstance(context) // 시간데이터 가져오기
        val dataList = db?.user2Dao()?.getAll() // 시간리스트

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
                    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

                    val intent = Intent(context, AlarmReceiver::class.java)
                    intent.putExtra("key", "approximately")
                    intent.putExtra("code", codeOne)
                    intent.putExtra("alertTime", time)
                    intent.putExtra("alertDayOfWeek", dayOfWeek)
                    intent.putExtra("alertEverydayCheck", dataList[i].everydayCheck)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
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
                    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

                    val intent = Intent(context, AlarmReceiver::class.java)
                    intent.putExtra("key", "bloodPressure")
                    intent.putExtra("code", codeTwo)
                    intent.putExtra("alertTime", time)
                    intent.putExtra("alertDayOfWeek", dayOfWeek)
                    intent.putExtra("alertEverydayCheck", dataList[i].everydayCheck)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
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
                    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

                    val intent = Intent(context, AlarmReceiver::class.java)
                    intent.putExtra("key", "food")
                    intent.putExtra("code", codeThree)
                    intent.putExtra("alertTime", time)
                    intent.putExtra("alertDayOfWeek", dayOfWeek)
                    intent.putExtra("alertEverydayCheck", dataList[i].everydayCheck)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
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
                    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

                    val intent = Intent(context, AlarmReceiver::class.java)
                    intent.putExtra("key", "exercise")
                    intent.putExtra("code", codeFour)
                    intent.putExtra("alertTime", time)
                    intent.putExtra("alertDayOfWeek", dayOfWeek)
                    intent.putExtra("alertEverydayCheck", dataList[i].everydayCheck)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
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
    }










    // 날짜 비교후 데이터 초기화 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initDataReset(context: Context) {
        // 1. 현재 날짜와 날짜 저장 초기화 하는 부분 처음실행시 saveDate의 파일은 없기때문에 기본값 0을 이용해 조건문 사용
        val now = System.currentTimeMillis()
        val date = Date(now)
        val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
        val mFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        mFormat.setTimeZone(tz) //DateFormat에 TimeZone 설정
        var currentDate = mFormat.format(date).toString()

        var db3 : User3Database? = User3Database.getInstance(context)
        if(MyApplication.prefs.getString("saveDate","0").equals("0")) { // 처음 실행해서 데이터가 없는 상황이라면
            MyApplication.prefs.setString("saveDate", currentDate) // 현재 날짜로 저장
        } else { // 데이터가 있는 상황이라면
            var saveDateYear = MyApplication.prefs.getString("saveDate","0").slice(IntRange(0,3)).toInt()
            var saveDateMonth = MyApplication.prefs.getString("saveDate","0").slice(IntRange(5,6)).toInt()
            var saveDateDay = MyApplication.prefs.getString("saveDate","0").slice(IntRange(8,9)).toInt()
            var currentYear = currentDate.slice(IntRange(0,3)).toInt()
            var currentMonth = currentDate.slice(IntRange(5,6)).toInt()
            var currentDay = currentDate.slice(IntRange(8,9)).toInt()
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



    private fun createNotificationChannelApproximately(context: Context, code : Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "${code}",
                "약 복용 알람",
                NotificationManager.IMPORTANCE_HIGH
            )

            NotificationManagerCompat.from(context)
                .createNotificationChannel(notificationChannel)
        }
    }

    private fun notifyNotificationApproximately(context: Context, code : Int, alertIntent : Intent, time : String) {
        MyApplication.prefs.setString("alertTitleNum", "1") // 0을 기본값 널로 하고 1부터 하기. 제목 보내기
        MyApplication.prefs.setString("alertTime", time) // 시간 보내기

        val pendingIntent : PendingIntent = PendingIntent.getActivity(context, 0, alertIntent, 0) // pendingintent설정
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, "${code}")
                .setContentTitle("알람")
                .setContentText("약 복용하실 시간입니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // 수정하기.
                .setAutoCancel(true) // 알림창 클릭시 자동 제거
                .setContentIntent(pendingIntent) // 알림창 클릭시 액티비티 띄우기
            notify(code, build.build())
        }
    }

    private fun createNotificationChannelBloodPressure(context: Context, code : Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "${code}",
                "혈압측정 알람",
                NotificationManager.IMPORTANCE_HIGH
            )

            NotificationManagerCompat.from(context)
                .createNotificationChannel(notificationChannel)
        }
    }
    private fun notifyNotificationBloodPressure(context: Context, code : Int, alertIntent : Intent, time : String) {
        MyApplication.prefs.setString("alertTitleNum", "2") // 0을 기본값 널로 하고 1부터 하기. 제목 보내기
        MyApplication.prefs.setString("alertTime", time) // 시간 보내기
        val pendingIntent : PendingIntent = PendingIntent.getActivity(context, 0, alertIntent, 0) // pendingintent설정
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, "${code}")
                .setContentTitle("알람")
                .setContentText("혈압측정 시간입니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true) // 알림창 클릭시 자동 제거
                .setContentIntent(pendingIntent) // 알림창 클릭시 액티비티 띄우기

            notify(code, build.build())
        }
    }

    private fun createNotificationChannelFood(context: Context, code : Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "${code}",
                "식사 알람",
                NotificationManager.IMPORTANCE_HIGH
            )

            NotificationManagerCompat.from(context)
                .createNotificationChannel(notificationChannel)
        }
    }
    private fun notifyNotificationFood(context: Context, code : Int, alertIntent : Intent, time : String) {
        MyApplication.prefs.setString("alertTitleNum", "3") // 0을 기본값 널로 하고 1부터 하기. 제목 보내기
        MyApplication.prefs.setString("alertTime", time) // 시간 보내기
        val pendingIntent : PendingIntent = PendingIntent.getActivity(context, 0, alertIntent, 0) // pendingintent설정
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, "${code}")
                .setContentTitle("알람")
                .setContentText("식사하실 시간입니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true) // 알림창 클릭시 자동 제거
                .setContentIntent(pendingIntent) // 알림창 클릭시 액티비티 띄우기

            notify(code, build.build())
        }
    }

    private fun createNotificationChannelExercise(context: Context, code : Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "${code}",
                "운동 알람",
                NotificationManager.IMPORTANCE_HIGH
            )

            NotificationManagerCompat.from(context)
                .createNotificationChannel(notificationChannel)
        }
    }
    private fun notifyNotificationExercise(context: Context, code : Int, alertIntent : Intent, time : String) {
        MyApplication.prefs.setString("alertTitleNum", "4") // 0을 기본값 널로 하고 1부터 하기. 제목 보내기
        MyApplication.prefs.setString("alertTime", time) // 시간 보내기
        val pendingIntent : PendingIntent = PendingIntent.getActivity(context, 0, alertIntent, 0) // pendingintent설정
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, "${code}")
                .setContentTitle("알람")
                .setContentText("운동하실 시간입니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true) // 알림창 클릭시 자동 제거
                .setContentIntent(pendingIntent) // 알림창 클릭시 액티비티 띄우기

            notify(code, build.build())
        }
    }

}
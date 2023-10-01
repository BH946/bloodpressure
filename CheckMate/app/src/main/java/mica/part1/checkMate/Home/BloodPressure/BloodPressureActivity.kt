package mica.part1.checkMate.Home.BloodPressure

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import mica.part1.checkMate.Home.BloodPressure.Room.User
import mica.part1.checkMate.Home.BloodPressure.Room.UserDatabase
import mica.part1.checkMate.BuildConfig
import mica.part1.checkMate.Home.BloodPressureReport.BloodPressureReportActivity
import mica.part1.checkMate.MainActivity
import mica.part1.checkMate.databinding.ActivityBloodPressureBinding
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class BloodPressureActivity : AppCompatActivity() {
    // assets레퍼지토리에 health_exam.xlsx파일을 넣으면 이는 어플로 같이 컴파일 된다.
    // filesDir의 경로처럼 휴대폰의 files경로에 health_exam.xlsx파일 또한 넣으면 여기서 잘 실행된다.
    // 그러나 어플로 컴파일시 휴대폰의 files경로는 당연히 어플의 경로가 아니기때문에 어플과 같이 컴파일 되지 않는다.
    // 따라서 어플에 실행시 health_exam.xlsx파일은 당연히 없다.

    // => 그렇다면 어떻게 어플은 이러한파일을 기계에서 받아서 사용을 하는가??? 궁금하다.
    // SD카드인 즉, 외부저장소에 기계에서 health_exam.xlsx파일을 저장할시 이를 이용한다던지, 그러는건가???
    // 만약 그렇다면 health_exam.xlsx파일을 SD카드의 어떤 경로로 지정을 해서 이를 보통 가져오는가?

    // assets 지우기.



    val binding by lazy { ActivityBloodPressureBinding.inflate(layoutInflater)}
    @RequiresApi(Build.VERSION_CODES.O) // date 위해서 필요
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val db : UserDatabase? = UserDatabase.getInstance(this)
        var bloodPressure : String = "" // 혈압

        // xlsx 파일 경로
//        val filePath = filesDir.toString()
        val filePath = "/data/data/mica.part1.checkMate"
        val fileName = "health_exam.xlsx" // 행 6만개정도??
//        val am = resources.assets
//        val file = am.open(fileName)
        val file = File("$filePath/$fileName")


        // 1. 데이터 뽑기(SAX 파싱 + poi-ooxml 라이브러리 이용)
        var excelSheetHandlerjob: ExcelSheetHandlerjob? = null
        excelSheetHandlerjob = ExcelSheetHandlerjob.readExcel(file)
        val excelDatesjob = excelSheetHandlerjob!!.getRows()
        var sys = excelDatesjob[8][4].toDouble() // 수축기 혈압
        var dia = excelDatesjob[11][4].toDouble() // 이완기 혈압
        var pulse = excelDatesjob[8][5].toDouble() // 심박수
        bloodPressure = "${sys.toInt()}\n${dia.toInt()}\n${pulse.toInt()}".toString()

        // 2. xlsx 혈압 UI에 출력하기.
        binding.bloodPressureResultTextView.text = "${bloodPressure}"

        // 3. 혈압 저장.Room
        val now = System.currentTimeMillis()
        val date = Date(now)
        val tz = TimeZone.getTimeZone("Asia/Seoul") // TimeZone에 표준시 설정
        val mFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN)
        mFormat.setTimeZone(tz) //DateFormat에 TimeZone 설정
        var dateTime = mFormat.format(date)

        db?.userDao()?.insert(User("${dateTime}", "${bloodPressure}"))
        // 4. room에 전부 저장했으니 xlsx 파일 삭제하기
        // => 잠시 주석 처리 하겠음.
        // File("$filePath/$fileName").delete()

        // 버튼 클릭
        binding.bloodPressureButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 혈압측정 기록 보는 버튼 => 이녀석은 혈압측정 완료후 옆에 위에 버튼 놔두게 옮기기 수정하기.
        binding.bloodPressureReportButton.setOnClickListener {
            val intent = Intent(this, BloodPressureReportActivity::class.java)
            startActivity(intent)
        }
    }

}
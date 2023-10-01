package mica.part1.checkMate.TodayMission.TodayMissionOption.DataClass

// 리사이클러뷰로 구성된 item의 약복용시간안에 시간들로 구성된 리사이클러뷰
data class TodayMissionOptionInner(
    var title : String,
    var dayOfTheWeek : String = "월", // 기본값은 월요일로 초기화
    var time : String = "00:00", // 기본값은 00:00으로 초기화
    var evrydayCheck : Int = 0, // 0은 매일 체크X, 1은 매일 체크 O로 구분
){}
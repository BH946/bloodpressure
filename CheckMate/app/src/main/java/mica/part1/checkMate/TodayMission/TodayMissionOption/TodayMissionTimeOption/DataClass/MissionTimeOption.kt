package mica.part1.checkMate.TodayMission.TodayMissionOption.TodayMissionTimeOption.DataClass

// +눌러서 들어간 시간설정하는 곳 리사이클러뷰
data class MissionTimeOption (
    var dayOfWeek :String,
    var hour : String,
    var minute : String,
    var everydayCheck : Int
        ) {
}
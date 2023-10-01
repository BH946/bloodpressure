package mica.part1.checkMate.TodayMission.TodayMissionOption.DataClass

// 약복용하는시간 떠있는 레이아웃의 리사이클러뷰
// 생성자는 어차피 자동 초기화
data class TodayMissionOption (
    var title : String,
    var isExpanded : Boolean = false // 확장인지 아닌지 체크
        ){
}
package mica.part1.checkMate.TodayMission.TodayMissionOption.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mica.part1.checkMate.TodayMission.Room.User2Database
import mica.part1.checkMate.TodayMission.TodayMissionOption.DataClass.TodayMissionOptionInner
import mica.part1.checkMate.databinding.ItemTimeInnerOptionBinding

class InnerExpandableAdapter(context : Context, val listData : MutableList<TodayMissionOptionInner>) : RecyclerView.Adapter<InnerExpandableAdapter.Holder>() {
    class Holder(val binding : ItemTimeInnerOptionBinding) : RecyclerView.ViewHolder(binding.root){
        var db2 : User2Database? = User2Database.getInstance(binding.root.context)
        fun bind(missionTime : TodayMissionOptionInner) {
            if(!missionTime.time.slice(IntRange(2,2)).equals(":")) {
                missionTime.time = "0" + missionTime.time // ex) 2:22를 02:22로 바꿔주는 부분
            }
            if (missionTime.evrydayCheck == 0) // false
                binding.switchButton.isChecked = false
            else binding.switchButton.isChecked = true
            binding.timeTextView.text = missionTime.time
            binding.dayOfWeekTextView.text = missionTime.dayOfTheWeek.slice(IntRange(0,0))

            // everydayCheck데이터 db2에 수정 위한 로직
            binding.switchButton.setOnClickListener{
                if(binding.switchButton.isChecked == true) { // 체크가 되어있다면 => everydayCheck를 1로 수정(db데이터안)
                    db2?.user2Dao()?.everydayCheckUpdateOne(missionTime.title, missionTime.time, missionTime.dayOfTheWeek)    
                } else { // 체크가 안되어있다면 => everydayCheck를 0로 수정(db데이터안)
                    db2?.user2Dao()?.everydayCheckUpdateZero(missionTime.title, missionTime.time, missionTime.dayOfTheWeek)
                }
            }
            

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder {
        return Holder(ItemTimeInnerOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
package mica.part1.checkMate.HealthReport.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mica.part1.checkMate.HealthReport.DataClass.CalendarRate
import mica.part1.checkMate.databinding.ItemCalendarRateBinding

class CalendarRateAdapter(val listData : MutableList<CalendarRate>) : RecyclerView.Adapter<CalendarRateAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemCalendarRateBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        // 실제로 create된곳에 값을 세팅하기위해 사용되는 것
        // 1. 사용할 데이터를 꺼내고
        val data = listData.get(position)
        // 2. 홀더에 데이터를 전달
        holder.setData(data)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    class Holder(val binding : ItemCalendarRateBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setData(calendarRate : CalendarRate) {
            val title = binding.titleTextView
            val rate = binding.rateTextView
            val rateProgressBar = binding.rateProgressBar
            title.text = calendarRate.title
            rate.text = calendarRate.rate.toString()
            rateProgressBar.setProgressPercentage(calendarRate.rate.toDouble())
        }

    }

}
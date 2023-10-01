package mica.part1.checkMate.HealthReport.CalendarDecorator

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import mica.part1.checkMate.R

class EventDecorator(rate : Int, context: Activity?, event: CalendarDay) : DayViewDecorator {
    val eventDay = event
    val rate = rate
    private val drawableOne: Drawable = context?.getDrawable(R.drawable.ic_custom_draw1)!! // 어째서 이렇게 작성해야하는지 잘 모르겠다.
    private val drawableTwo: Drawable = context?.getDrawable(R.drawable.ic_custom_draw2)!!
    private val drawableThree: Drawable = context?.getDrawable(R.drawable.ic_custom_draw3)!!
    private val drawableFour: Drawable = context?.getDrawable(R.drawable.ic_custom_draw4)!!
    private val drawableFive: Drawable = context?.getDrawable(R.drawable.ic_custom_draw5)!!


    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return (day?.month == eventDay.month-1 && day.day == eventDay.day)
    }

    override fun decorate(view: DayViewFacade?) {
        if(rate > 0 && rate < 20) { // 1단계
            view?.setBackgroundDrawable(drawableOne)
//            view?.addSpan(object:RelativeSizeSpan(2.4f){}) // 텍스트 크기 변경
//            view?.addSpan(object:StyleSpan(Typeface.BOLD){}) // 텍스트 진하게
        }
        else if (rate >= 20 && rate < 40) { // 2단계
            view?.setBackgroundDrawable(drawableTwo)
        }
        else if (rate >= 40 && rate < 60) { // 3단계
            view?.setBackgroundDrawable(drawableThree)
            view?.addSpan(object:ForegroundColorSpan(Color.WHITE){}) // 텍스트 흰색으로 변경
        }
        else if (rate >= 60 && rate <= 99) { // 4단계
            view?.setBackgroundDrawable(drawableFour)
            view?.addSpan(object:ForegroundColorSpan(Color.WHITE){}) // 텍스트 흰색으로 변경
        }
        else if (rate == 100) {
            view?.setBackgroundDrawable(drawableFive) // 마지막단계
            view?.addSpan(object:ForegroundColorSpan(Color.WHITE){}) // 텍스트 흰색으로 변경
        }
        view?.setDaysDisabled(true)
    }
}
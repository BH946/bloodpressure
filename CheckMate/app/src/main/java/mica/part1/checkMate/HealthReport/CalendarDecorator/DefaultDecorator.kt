package mica.part1.checkMate.HealthReport.CalendarDecorator

import android.app.Activity
import android.graphics.drawable.Drawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import mica.part1.checkMate.R

class DefaultDecorator(context: Activity?) : DayViewDecorator {
    private val drawable: Drawable = context?.getDrawable(R.drawable.ic_custom_draw_org)!! // 어째서 이렇게 작성해야하는지 잘 모르겠다.

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return (true)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.setBackgroundDrawable(drawable)
        view?.setDaysDisabled(true)
    }
}
package com.kongjak.ggcj.Tools

import android.graphics.Typeface
import android.text.style.StyleSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import org.threeten.bp.LocalDate

class TodayDecorator : DayViewDecorator {
    private var date: CalendarDay?
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return date != null && day == date
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(StyleSpan(Typeface.BOLD))
    }

    fun setDate(date: LocalDate?) {
        this.date = CalendarDay.from(date)
    }

    init {
        date = CalendarDay.today()
    }
}
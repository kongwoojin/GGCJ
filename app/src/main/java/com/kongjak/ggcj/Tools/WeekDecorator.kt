package com.kongjak.ggcj.Tools

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import org.threeten.bp.DayOfWeek

class WeekDecorator(context: Context) : DayViewDecorator {
    private val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    override fun shouldDecorate(day: CalendarDay): Boolean {
        val weekDay = day.date.dayOfWeek
        return weekDay != DayOfWeek.SATURDAY && weekDay != DayOfWeek.SUNDAY
    }

    override fun decorate(view: DayViewFacade) {
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {view.addSpan(ForegroundColorSpan(Color.BLACK))}
            Configuration.UI_MODE_NIGHT_YES -> {view.addSpan(ForegroundColorSpan(Color.WHITE))}
        }
    }
}
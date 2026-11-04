package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateHelper {
    fun getTodayDayOfMonth(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    fun getCurrentMonthName(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    }

    fun getCurrentMonthYear(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun getDaysInCurrentMonth(): Int {
        val cal = Calendar.getInstance()
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Number of blank cells before the 1st day of the month
     * for a Monday-first calendar (Mon = 0, Tue = 1, ... Sun = 6).
     */
    fun getFirstDayOfWeekOffset(): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ..., 7 = Saturday
        return (dayOfWeek + 5) % 7
    }
}

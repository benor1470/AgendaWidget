package benor.agendawidget

import android.content.Context
import benor.MLog.MLog
import java.util.TimeZone

object Globals {
    private const val TAG = "AgendaWidget"
    lateinit var db: DB
    lateinit var con: Context
    var editListInit = false
    var widget: Widget? = null
    private var days: Array<String>? = null

    fun clearWeekDayStr() {
        days = null
    }

    fun getWeekDay(day: Int): String {
        if (days == null) {
            val arrId = if (db.getLang() == CONST.LANG_ENG) R.array.DaysOfWeek_eng else R.array.DaysOfWeek_heb
            days = con.resources.getStringArray(arrId)
        }
        return days!![day - 1]
    }

    fun init(context: Context) {
        con = context.applicationContext
        MLog.getInstance().Init(con, TAG, false, true)
        db = DB()
    }

    fun getString(stringId: Int): String = con.resources.getString(stringId)

    /**
     * Converts an all-day event stored as midnight UTC to local midnight.
     * All-day events in CalendarContract are stored as midnight UTC; we
     * interpret the calendar date in the local timezone for display purposes.
     */
    fun convertAlldayUtcToLocal(utcTime: java.util.Calendar) {
        val utcCal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCal.timeInMillis = utcTime.timeInMillis
        val year = utcCal.get(java.util.Calendar.YEAR)
        val month = utcCal.get(java.util.Calendar.MONTH)
        val day = utcCal.get(java.util.Calendar.DAY_OF_MONTH)
        utcTime.set(year, month, day, 0, 0, 0)
        utcTime.set(java.util.Calendar.MILLISECOND, 0)
    }
}

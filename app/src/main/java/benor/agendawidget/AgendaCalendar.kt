package benor.agendawidget

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.widget.Toast

class AgendaCalendar(
    val name: String,
    val id: Int,
    val color: Int,
    var showOnWidget: Boolean
) {
    override fun toString() = name

    fun setShowOnWidget(show: Boolean) {
        if (showOnWidget != show) {
            val list = Globals.db.getShowOnWidgetList()
            list.removeAll { it == id }
            if (show) list.add(id)
            Globals.db.setShowOnWidgetList(list)
            showOnWidget = show
        }
    }

    companion object {
        fun readCalendars(): List<AgendaCalendar> {
            val calendars = mutableListOf<AgendaCalendar>()

            if (Globals.con.checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(Globals.con, "need read calendar permission", Toast.LENGTH_LONG).show()
                return calendars
            }

            val cursor = Globals.con.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI, null, null, null, null
            ) ?: return calendars

            cursor.use {
                if (it.moveToFirst()) {
                    do {
                        val name = it.getString(it.getColumnIndexOrThrow(CalendarContract.Calendars.NAME))
                        val id = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                        val color = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR))
                        calendars.add(AgendaCalendar(name, id, color, Globals.db.getShowOnWidgetList().contains(id)))
                    } while (it.moveToNext())
                }
            }
            return calendars
        }
    }
}

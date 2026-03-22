package benor.agendawidget

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import benor.MLog.MLog
import java.util.Calendar
import java.util.Date

class Event private constructor(
    private val title: String,
    val color: Int,
    private val start: Date,
    private val end: Date,
    val allDayEvent: Boolean,
    var calendarEventID: Int,
    private val isRecurring: Boolean
) : Comparable<Event> {

    val dayOfMonth: Int
    val happeningNow: Boolean

    init {
        val cal = Calendar.getInstance()
        cal.time = start
        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        // event started before now (and hasn't ended yet, enforced by the query)
        happeningNow = start.before(Date())
    }

    override fun compareTo(other: Event): Int = start.compareTo(other.start)

    override fun toString(): String {
        val now = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val startCal = Calendar.getInstance().apply { time = start }
        val endCal = Calendar.getInstance().apply { time = end }

        if (allDayEvent) {
            Globals.convertAlldayUtcToLocal(startCal)
            Globals.convertAlldayUtcToLocal(endCal)
            endCal.add(Calendar.DATE, -1)
        }

        var printedEnd = false
        val time = buildString {
            when {
                startCal.get(Calendar.DATE) == now.get(Calendar.DATE) &&
                        startCal.get(Calendar.MONTH) == now.get(Calendar.MONTH) -> {
                    append(Globals.getString(R.string.today))
                }
                startCal.get(Calendar.DATE) == tomorrow.get(Calendar.DATE) &&
                        startCal.get(Calendar.MONTH) == tomorrow.get(Calendar.MONTH) -> {
                    append(Globals.getString(R.string.nextDay))
                    append("(${Globals.getWeekDay(startCal.get(Calendar.DAY_OF_WEEK))})")
                }
                else -> {
                    append(startCal.get(Calendar.DAY_OF_MONTH))
                    if (startCal.get(Calendar.DAY_OF_MONTH) != endCal.get(Calendar.DAY_OF_MONTH) &&
                        startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)
                    ) {
                        append("-${endCal.get(Calendar.DAY_OF_MONTH)}")
                        printedEnd = true
                    }
                    append("/${startCal.get(Calendar.MONTH) + 1}")
                    if (startCal.get(Calendar.YEAR) != now.get(Calendar.YEAR)) {
                        append("/${startCal.get(Calendar.YEAR)}")
                    }
                    append("(${Globals.getWeekDay(startCal.get(Calendar.DAY_OF_WEEK))})")
                }
            }

            if (startCal.get(Calendar.DAY_OF_MONTH) != endCal.get(Calendar.DAY_OF_MONTH) && !printedEnd) {
                append("-${endCal.get(Calendar.DAY_OF_MONTH)}/${endCal.get(Calendar.MONTH) + 1}")
            }

            if (!allDayEvent) {
                val hour = startCal.get(Calendar.HOUR_OF_DAY)
                val minute = startCal.get(Calendar.MINUTE)
                append(" $hour:${minute.toString().padStart(2, '0')}")
            }
        }

        val recurring = if (isRecurring) Globals.con.getString(R.string.recurring) else ""
        return "$recurring$title - $time"
    }

    companion object {

        private fun fromCursor(cursor: Cursor): Event? {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
            val color = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_COLOR))
            val calendarId = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID))
            val allDay = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY)) != 0
            val eventId = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID))

            val (start, end): Pair<Date, Date> =
                if (cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.RRULE)) == null) {
                    Pair(
                        Date(cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))),
                        Date(cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND)))
                    )
                } else {
                    val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
                    ContentUris.appendId(builder, Date().time)
                    ContentUris.appendId(builder, Long.MAX_VALUE)
                    val where = "Instances.event_id = $eventId AND ${CalendarContract.Instances.END} > ${Date().time}"
                    val instanceCursor = Globals.con.contentResolver.query(
                        builder.build(), null, where, null, "${CalendarContract.Instances.BEGIN} LIMIT 1"
                    )
                    instanceCursor?.use { ic ->
                        if (ic.moveToFirst()) {
                            Pair(
                                Date(ic.getLong(ic.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN))),
                                Date(ic.getLong(ic.getColumnIndexOrThrow(CalendarContract.Instances.END)))
                            )
                        } else null
                    } ?: return null
                }

            if (!Globals.db.getShowOnWidgetList().contains(calendarId)) return null

            return Event(title, color, start, end, allDay, eventId, false)
        }

        fun readEvents(): List<Event> {
            val events = mutableListOf<Event>()
            val now = Date().time

            if (Globals.con.checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(Globals.con, "missing the \"READ_CALENDAR\" permission", Toast.LENGTH_LONG).show()
                return events
            }

            val shownCalendars = Globals.db.getShowOnWidgetList()
            if (shownCalendars.isEmpty()) return events

            MLog.i("reading events for calendars: $shownCalendars")

            val calSelector = shownCalendars.joinToString(" OR ") { "${CalendarContract.Events.CALENDAR_ID} == $it" }
            val where = "${CalendarContract.Events.SELF_ATTENDEE_STATUS} != 2 " +
                    "AND (${CalendarContract.Events.DTEND} > $now " +
                    "OR (SELECT COUNT(*) FROM Instances WHERE Instances.event_id = view_events._id AND Instances.end > $now) > 0) " +
                    "AND ($calSelector)"

            val cursor = Globals.con.contentResolver.query(
                CalendarContract.Events.CONTENT_URI, null, where, null, CalendarContract.Events.DTSTART
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        fromCursor(it)?.let { event -> events.add(event) }
                    } while (it.moveToNext())
                }
            } ?: MLog.i("can't find any values")

            return events.sorted()
        }
    }
}

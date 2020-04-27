package benor.agendawidget;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import benor.MLog.MLog;

public class Event implements Comparable<Event> {
	final int color;
	final int dayOfMonth;
	final boolean allDayEvent;
	final boolean happeningNow;
	private final String title;
	private final Date start;
	private final Date end;
	int calendarEventID;
	private Boolean isRecurring;

	private Event(String title, int color, Date start, Date end, boolean allDayEvent, int calendarEventID, boolean isRecurring) {
		this.title = title;
		this.color = color;
		this.start = start;
		this.end = end;
		this.allDayEvent = allDayEvent;
		this.calendarEventID = calendarEventID;
		GregorianCalendar now = new GregorianCalendar();
		now.setTime(start);
		this.dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
		this.isRecurring = isRecurring;
		//start before now, if the event ended it wont be selected
		happeningNow = this.start.before(new Date());
	}

	private static Event CreateEventFromCursor(Cursor cursor) {
		String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
		int color = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_COLOR));
		int calendar_id = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
		boolean allDayEvent = (cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.ALL_DAY)) != 0);
		int calendarEventID = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events._ID));

		Date start;
		Date end;
		if (cursor.getString(cursor.getColumnIndex(CalendarContract.Events.RRULE)) == null) {
			start = new Date(cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART)));
			end = new Date(cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND)));
		} else {
			String eventId = cursor.getString(cursor.getColumnIndex(CalendarContract.Events._ID));

			Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
			ContentUris.appendId(builder, new Date().getTime());
			ContentUris.appendId(builder, Long.MAX_VALUE);


			String where = "Instances.event_id = " + eventId + " AND " + CalendarContract.Instances.END + " > " + new Date().getTime();
			try (Cursor event = Globals.con.getContentResolver().query(builder.build(), null, where, null, CalendarContract.Instances.BEGIN + " LIMIT 1")) {
				if (event != null && event.moveToFirst()) {
					start = new Date(event.getLong(event.getColumnIndex(CalendarContract.Instances.BEGIN)));
					end = new Date(event.getLong(event.getColumnIndex(CalendarContract.Instances.END)));
				} else {
					end = start = new Date(0);
				}
			}
		}

		Event eventGotFromDB = new Event(title, color, start, end, allDayEvent, calendarEventID, false);


		//to clean custom events color
/*
		String eventColor = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_COLOR_KEY));
		if (eventColor != null && !"".equals(eventColor)) {
			ContentValues values = new ContentValues();
			Uri updateUri = null;
			// The new title for the event
			values.put(Events.EVENT_COLOR_KEY, "");
			updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, calendarEventID);
			int rows = Globals.con.getContentResolver().update(updateUri, values, null, null);
			MLog.i("!@updated: " + rows + " ?1 - " + title + " color was " + eventColor);
		} else {
			//MLog.i("not updated "+title+" color="+(eventColor==null?"null":(eventColor.length()==0)?"length=0":color));
		}
*/


		if (!Globals.DB.getShowOnWidgetList().contains(calendar_id)) {
			MLog.e("got event " + eventGotFromDB + " with out being on the calendarts list");
		} else {
			return eventGotFromDB;
			//	MLog.i("added event "+eventGotFromDB);
		}


		return null;
	}

	static List<Event> readEvents() {
		MLog.i("created widgets " + CalendarContract.Events.CONTENT_URI.toString());
		LinkedList<Event> events = new LinkedList<>();

		long now = new Date().getTime();
		String whereCondition = CalendarContract.Events.SELF_ATTENDEE_STATUS + " != 2 "
				+ "AND (" +
				CalendarContract.Events.DTEND + ">" + now +
				" OR " +
				"(SELECT COUNT(*) FROM Instances WHERE Instances.event_id = view_events._id AND Instances.end>" + now + ")>0"
				+ ")";

		StringBuilder calendarsSelector = new StringBuilder();
		if (Globals.DB.getShowOnWidgetList().size() > 0) {
			calendarsSelector.append(" AND (1==2");
			for (Integer id : Globals.DB.getShowOnWidgetList()) {
				calendarsSelector.append(" OR " + Events.CALENDAR_ID + " == ").append(id);
			}
			calendarsSelector.append(")");
		}
		whereCondition += calendarsSelector;
		MLog.i("selector " + whereCondition);

		if (Globals.con.checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			try {
				Looper.prepare();
			} catch (Exception e) {
				//can't prepare looper, most like already prepared
			}
			Toast.makeText(Globals.con, "missing the \"READ_CALENDAR\" permission", Toast.LENGTH_LONG).show();
		}
		Cursor eventsCursor = Globals.con.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null,
				whereCondition, null, CalendarContract.Events.DTSTART);

		/*Calendar nowCal = Calendar.getInstance();
		nowCal.setTime(new Date());
		events.add(new Event(
				nowCal.get(Calendar.MINUTE) + ":" + nowCal.get(Calendar.SECOND) + "",
				0xFF0000FF,
				new Date(),
				new Date(new Date().getTime() + 1000 * 60),
				false,
				1,
				false));
		nowCal.set(Calendar.DATE, nowCal.get(Calendar.DATE) + 1);
		events.add(new Event(
				"tomorrow",
				0xFF0000FF,
				nowCal.getTime(),
				new Date(nowCal.getTime().getTime() + 1000 * 60),
				false,
				1,
				false));*/

		if (eventsCursor != null && eventsCursor.getCount() > 0) {
			eventsCursor.moveToFirst();
			do {
				Event event = CreateEventFromCursor(eventsCursor);
				if (event != null) {
					events.add(event);
				}
			} while (eventsCursor.moveToNext());
		} else {
			MLog.i("can't find any values");
		}
		Collections.sort(events);
		return events;
	}

	@Override
	public String toString() {
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());

		Calendar tomorrow = Calendar.getInstance();
		tomorrow.setTime(new Date());
		tomorrow.roll(Calendar.DAY_OF_WEEK, 1);

		Calendar start = Calendar.getInstance();
		start.setTime(this.start);

		Calendar end = Calendar.getInstance();
		end.setTime(this.end);


		boolean printedEnd = false;
		if (allDayEvent) {
			Globals.convertAlldayUtcToLocal(start);
			Globals.convertAlldayUtcToLocal(end);
			end.roll(Calendar.DATE, -1);
		}
		String time = "";
		if (start.get(Calendar.DATE) == now.get(Calendar.DATE) && start.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
			time += Globals.getString(R.string.today);
		} else if (start.get(Calendar.DATE) == tomorrow.get(Calendar.DATE) && start.get(Calendar.MONTH) == tomorrow.get(Calendar.MONTH)) {
			time += Globals.getString(R.string.nextDay) + "(" + Globals.getWeekDay(start.get(Calendar.DAY_OF_WEEK)) + ")";
		} else {
			time += start.get(Calendar.DAY_OF_MONTH);
			if (start.get(Calendar.DAY_OF_MONTH) != end.get(Calendar.DAY_OF_MONTH) && (start.get(Calendar.MONTH) == end.get(Calendar.MONTH))) {
				time += "-" + end.get(Calendar.DAY_OF_MONTH);
				printedEnd = true;
			}
			time += "/" + (start.get(Calendar.MONTH) + 1);
			if (start.get(Calendar.YEAR) != now.get(Calendar.YEAR)) {
				time += "/" + start.get(Calendar.YEAR);
			}
			time += "(" + Globals.getWeekDay(start.get(Calendar.DAY_OF_WEEK)) + ")";
		}
		if (start.get(Calendar.DAY_OF_MONTH) != end.get(Calendar.DAY_OF_MONTH) && !printedEnd) {
			time += "-" + end.get(Calendar.DAY_OF_MONTH) + "/" + (end.get(Calendar.MONTH) + 1);
		}
		if (!allDayEvent) {
			time += " " + start.get(Calendar.HOUR_OF_DAY) + ":" + start.get(Calendar.MINUTE);
			if (start.get(Calendar.MINUTE) < 10) {
				time += "0";
			}
		}
		return (isRecurring ? Globals.con.getString(R.string.recurring) : "") + title + " - " + time;
	}

	@Override
	public int compareTo(Event another) {
		return end.compareTo(another.end);
	}
}

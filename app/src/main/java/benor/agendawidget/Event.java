package benor.agendawidget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.annotation.NonNull;

import benor.MLog.MLog;

public class Event implements Comparable<Event> {
	private final String title;
	private final int color;
	private final Date start;
	private final Date end;
	private final int dayOfMonth;
	private final int calendar_id;
	private final boolean allDayEvent;
	private final boolean happeningNow;
	private int calendarEventID;
	private Boolean isRecurring;

	private Event(String title, int color, Date start, Date end, int calendar_id, boolean allDayEvent, int calendarEventID, boolean isRecurring) {
		this.title = title;
		this.color = color;
		this.start = start;
		this.end = end;
		this.calendar_id = calendar_id;
		this.allDayEvent = allDayEvent;
		this.calendarEventID = calendarEventID;
		GregorianCalendar now = new GregorianCalendar();
		now.setTime(start);
		this.dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
		this.isRecurring = isRecurring;
		happeningNow = this.start.before(new Date());
	}

	public Uri getCalendarIcon() {
		return benor.agendawidget.Calendar.getCalIcon(calendar_id, color);
	}

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
			start = Globals.convertAlldayUtcToLocal(start);
			end = Globals.convertAlldayUtcToLocal(end);
			end.roll(Calendar.DATE, -1);
		}
		String time = "";
		if (start.get(Calendar.DATE) == now.get(Calendar.DATE) && start.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
			time += Globals.getString(R.string.today);
		} else if (start.get(Calendar.DATE) == tomorrow.get(Calendar.DATE) && start.get(Calendar.MONTH) == tomorrow.get(Calendar.MONTH)) {
			time += Globals.getString(R.string.tomorrow) + "(" + Globals.getWeekDay(start.get(Calendar.DAY_OF_WEEK)) + ")";
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

	private static Event createEventFromCursor(Cursor cursor) {
		String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
		int color = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_COLOR));
		String eventColor = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_COLOR_KEY));
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

		Event eventGotFromDB = new Event(title, color, start, end, calendar_id, allDayEvent, calendarEventID, false);


		//to clean custom events color
		if (eventColor != null && !"".equals(eventColor)) {
			ContentValues values = new ContentValues();
			// The new title for the event
			values.put(Events.EVENT_COLOR_KEY, "");
			Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, calendarEventID);
			int rows = Globals.con.getContentResolver().update(updateUri, values, null, null);
			MLog.i("!@updated: " + rows + " ?1 - " + title + " color was " + eventColor);
		}


		if (!Globals.DB.getShowOnWidgetList().contains(calendar_id)) {
			MLog.e("got event " + eventGotFromDB + " which is not in selected calendar, ignoring");
		} else {
			return eventGotFromDB;
			//	MLog.i("added event "+eventGotFromDB);
		}


		return null;
	}

	static List<Event> readEvents() {
		MLog.i("created widgets " + CalendarContract.Events.CONTENT_URI.toString());
		List<Event> events = new LinkedList<>();

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

		@SuppressLint("MissingPermission")
		Cursor eventsCursor = Globals.con.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null,
				whereCondition, null, CalendarContract.Events.DTSTART);

//		String currentTime = new SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(new Date());
//		events.add(new Event("last render " + currentTime, -11958553, new Date(), new Date(System.currentTimeMillis() + 100), Globals.DB.getShowOnWidgetList().get(0), false, 1324, false));

		if (eventsCursor != null && eventsCursor.getCount() > 0) {
			eventsCursor.moveToFirst();
			do {
				Event event = createEventFromCursor(eventsCursor);
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
	public int compareTo(@NonNull Event another) {
		return start.compareTo(another.start);
	}

	public int getColor() {
		return color;
	}

	public boolean isHappeningNow() {
		return happeningNow;
	}

	public boolean isAllDayEvent() {
		return allDayEvent;
	}

	public int getDayOfMonth() {
		return dayOfMonth;
	}
}

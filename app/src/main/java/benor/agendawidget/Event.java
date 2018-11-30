package benor.agendawidget;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import benor.MLog.MLog;

public class Event implements Comparable<Event> {
	private final String title;
	public final int color;
	private final Date start;
	private final Date end;
	final int dayOfMonth;
	private final String calendar;
	private final int calendar_id;
	final boolean allDayEvent;
	final boolean happeningNow;
	int calendarEventID;
	private Boolean isRecurring;

	public Event(String title, int color, Date start, Date end, String calendar, int calendar_id, boolean allDayEvent, int calendarEventID, boolean isRecurring) {
		this.title = title;
		this.color = color;
		this.start = start;
		this.end = end;
		this.calendar = calendar;
		this.calendar_id = calendar_id;
		this.allDayEvent = allDayEvent;
		this.calendarEventID = calendarEventID;
		GregorianCalendar now = new GregorianCalendar();
		now.setTime(start);
		this.dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
		this.isRecurring = isRecurring;
		if (this.start.before(new Date())) {
			happeningNow = true;
		} else {
			happeningNow = false;
		}
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

		String val = (isRecurring ? Globals.con.getString(R.string.recurring) : "") + title + " - " + time;
		//MLog.i(val+" = "+start+","+end+","+calendar_id+","+allDayEvent);
		return val;
	}

	private static Event CreateEventFromCursor(Cursor cursor) {
		String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
		int color = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_COLOR));
		String eventColor = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_COLOR_KEY));
		String calendar = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_DISPLAY_NAME));
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
			Cursor event = Globals.con.getContentResolver().query(builder.build(), null, where, null, CalendarContract.Instances.BEGIN + " LIMIT 1");//
			if (event.moveToFirst()) {
				start = new Date(event.getLong(event.getColumnIndex(CalendarContract.Instances.BEGIN)));
				end = new Date(event.getLong(event.getColumnIndex(CalendarContract.Instances.END)));
			} else {
				end = start = new Date(0);
			}
		}

		Event eventGotFromDB = new Event(title, color, start, end, calendar, calendar_id, allDayEvent, calendarEventID, false);


		//to clean custom events color
		if (eventColor != null && "".equals(eventColor) == false) {
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


		if (!Globals.DB.getShowOnWidgetList().contains(calendar_id)) {
			MLog.e("got event " + eventGotFromDB + " which is not in selected calendar, ignoring");
		} else {
			return eventGotFromDB;
			//	MLog.i("added event "+eventGotFromDB);
		}


		return null;
	}

	public static List<Event> readEvents() {
		MLog.i("created widgets " + CalendarContract.Events.CONTENT_URI.toString());
		LinkedList<Event> events = new LinkedList<Event>();

		long now = new Date().getTime();
		String whereCondition = CalendarContract.Events.SELF_ATTENDEE_STATUS + " != 2 "
				+ "AND (" +
				CalendarContract.Events.DTEND + ">" + now +
				" OR " +
				"(SELECT COUNT(*) FROM Instances WHERE Instances.event_id = view_events._id AND Instances.end>" + now + ")>0"
				+ ")";

		String calendarsSelector = "";
		if (Globals.DB.getShowOnWidgetList().size() > 0) {
			calendarsSelector += " AND (1==2";
			for (Integer id : Globals.DB.getShowOnWidgetList()) {
				calendarsSelector += " OR " + CalendarContract.Events.CALENDAR_ID + " == " + id;
			}
			calendarsSelector += ")";
		}
		whereCondition += calendarsSelector;
		MLog.i("selector " + whereCondition);

		@SuppressLint("MissingPermission")
		Cursor eventsCursor = Globals.con.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null,
				whereCondition, null, CalendarContract.Events.DTSTART);
		eventsCursor.moveToFirst();


		if (eventsCursor != null && eventsCursor.getCount() > 0) {
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
	public int compareTo(Event another) {
		return end.compareTo(another.end);
	}
}

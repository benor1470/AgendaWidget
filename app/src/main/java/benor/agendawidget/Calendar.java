package benor.agendawidget;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Calendar {
	final int color;
	public final Integer id;
	private final String name;
	boolean showOnWidget;

	private Calendar(String name, int id, int color) {
		this.showOnWidget = Globals.DB.getShowOnWidgetList().contains(id);
		this.name = name;
		this.id = id;
		this.color = color;
	}

	static List<Calendar> readCalendars() {
		ArrayList<Calendar> calendars = new ArrayList<>();

		if (Globals.con.checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(Globals.con, "need read calendar permission", Toast.LENGTH_LONG).show();
		}

		Cursor cursor = Globals.con.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null,
				null, null, null);
		if (cursor == null) {
			return new ArrayList<>();
		}
		try {
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				do {
					String name = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.NAME));
					int id = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars._ID));
					int color = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR));
					calendars.add(new Calendar(name, id, color));

				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		return calendars;
	}

	@Override
	public String toString() {
		return name + "";
	}

	void setShowOnWidget(boolean show) {
		if (this.showOnWidget != show) {
			LinkedList<Integer> list = Globals.DB.getShowOnWidgetList();
			Iterator<Integer> itr = list.iterator();
			while (itr.hasNext()) {
				if (itr.next().equals(id)) {
					itr.remove();
				}
			}
			if (show) {
				list.add(this.id);
			}
			Globals.DB.setShowOnWidgetList(list);
			this.showOnWidget = show;
		}
	}
}

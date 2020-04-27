package benor.agendawidget;

import android.content.Context;
import android.text.format.Time;

import java.util.Calendar;

import benor.MLog.MLog;

public class Globals {
	private static final String TAG = "AgendaWidget";
	static DB DB;
	static Context con;
	static boolean editListInit;
	public static Widget widget;
	private static String[] days = null;

	static void clearWeekDayStr() {
		days = null;
	}

	static String getWeekDay(int day) {
		if (days == null) {
			int arrId;
			if (Globals.DB.getLang().equals(CONST.LANG_ENG)) {
				arrId = R.array.DaysOfWeek_eng;
			} else {
				arrId = R.array.DaysOfWeek_heb;
			}

			days = Globals.con.getResources().getStringArray(arrId);
		}
		return days[day - 1];
	}

	public static void init(Context con) {
		Globals.con = con;
		MLog.getInstance().Init(Globals.con, Globals.TAG, false, true);
		Globals.DB = new DB();
	}

	public static String getString(int string_id) {
		return Globals.con.getResources().getString(string_id);
	}

	/**
	 * changes the given calendar (not sure to what)
	 */
	static void convertAlldayUtcToLocal(Calendar utcTime) {
		Time recycle = new Time();
		recycle.timezone = Time.TIMEZONE_UTC;
		recycle.set(utcTime.getTimeInMillis());
		recycle.timezone = Time.getCurrentTimezone();
		utcTime.setTimeInMillis(recycle.normalize(true));
	}
}
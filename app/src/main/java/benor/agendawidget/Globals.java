package benor.agendawidget;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.text.format.Time;

import benor.MLog.MLog;

public class Globals {
	private static final String TAG = "AgendaWidget";
	static DB DB;
	static Context con;
	static boolean editListInit;
	static Widget widget;

	static String getWeekDay(int day) {
		String[] days = Globals.con.getResources().getStringArray(R.array.DaysOfWeek);
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

	public static Calendar convertAlldayUtcToLocal(Calendar utcTime) {
		Time recycle = new Time();
		recycle.timezone = Time.TIMEZONE_UTC;
		recycle.set(utcTime.getTimeInMillis());
		recycle.timezone = Time.getCurrentTimezone();
		utcTime.setTimeInMillis(recycle.normalize(true));
		return utcTime;
	}
}
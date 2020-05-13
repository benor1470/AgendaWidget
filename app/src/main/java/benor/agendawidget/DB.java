package benor.agendawidget;

import android.content.SharedPreferences;

import java.util.Collections;
import java.util.LinkedList;

import benor.MLog.MLog;

public class DB {
	private SharedPreferences SP;
	private final String CALENDARS_SHOWN_ON_WIDGET = "CALENDARS_SHOWN_ON_WIDGET";
	private final String CALENDARS_APP_INTENT = "CALENDARS_APP_INTENT";
	private final String SAVED_LANGUAGE = "SAVED_LANGUAGE";
	private final String BG_COLOR = "BG_COLOR";
	private LinkedList<Integer> calendarsCache;

	private SharedPreferences getSP() {
		if (SP == null) {
			SP = Globals.con.getSharedPreferences("data", 0);
		}
		return SP;
	}

	LinkedList<Integer> getShowOnWidgetList() {
		if (calendarsCache == null) {
			LinkedList<Integer> calendars = new LinkedList<Integer>();
			String list = getSP().getString(CALENDARS_SHOWN_ON_WIDGET, "");
			assert list != null;
			if (list.length() > 0) {
				for (String id : list.split(",")) {
					try {
						calendars.add(Integer.parseInt(id));
					} catch (Exception e) {
					}
				}
			}
			this.calendarsCache = calendars;
		}
		return calendarsCache;
	}

	void setShowOnWidgetList(LinkedList<Integer> calendars) {
		String temp = "";
		for (Integer integer : calendars) {
			temp += integer + ",";
		}
		MLog.i("before " + temp);
		Collections.sort(calendars);
		temp = "";
		for (Integer integer : calendars) {
			temp += integer + ",";
		}
		MLog.i("after " + temp);

		calendarsCache = calendars;

		String list = "";
		list = calendars.toString();
		list = list.replace('[', ' ').replace(']', ' ').replace(" ", "");
		getSP().edit().putString(CALENDARS_SHOWN_ON_WIDGET, list).apply();
	}

	String getCalendarApp() {
		return getSP().getString(CALENDARS_APP_INTENT, null);
	}

	void setCalendarApp(String intent) {
		getSP().edit().putString(CALENDARS_APP_INTENT, intent).apply();

	}

	String getLang() {
		return getSP().getString(SAVED_LANGUAGE, CONST.LANG_ENG);
	}

	void setLang(String widgetLang) {
		Globals.clearWeekDayStr();
		getSP().edit().putString(SAVED_LANGUAGE, widgetLang).apply();
	}

	public int getBgColor() {
		return getSP().getInt(BG_COLOR, 0x550000FF);
	}

	void setBgColor(int bgColor) {
		getSP().edit().putInt(BG_COLOR, bgColor).apply();
	}
}

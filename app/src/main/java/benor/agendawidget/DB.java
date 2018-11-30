package benor.agendawidget;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import benor.MLog.MLog;

import android.content.SharedPreferences;

class DB {
	private static final Logger logger = Logger.getLogger(DB.class.getName());
	private SharedPreferences SP;
	private final String CALENDARS_SHOWN_ON_WIDGET = "CALENDARS_SHOWN_ON_WIDGET";
	private final String CALENDARS_APP_INTENT = "CALENDARS_APP_INTENT";
	private List<Integer> calendarsCache;

	private SharedPreferences getSP() {
		if (SP == null) {
			SP = Globals.con.getSharedPreferences("data", 0);
		}
		return SP;
	}

	List<Integer> getShowOnWidgetList() {
		if (calendarsCache == null) {
			List<Integer> calendars = new LinkedList<>();
			String list = getSP().getString(CALENDARS_SHOWN_ON_WIDGET, "");
			if (list.length() > 0) {
				for (String id : list.split(",")) {
					try {
						calendars.add(Integer.parseInt(id));
					} catch (Exception e) {
						logger.log(Level.SEVERE, "cant parse id " + id, e);
					}
				}
			}
			this.calendarsCache = calendars;
		}
		return calendarsCache;
	}

	void setShowOnWidgetList(List<Integer> calendars) {
		StringBuilder temp = new StringBuilder();
		for (Integer integer : calendars) {
			temp.append(integer).append(",");
		}
		MLog.i("before " + temp);
		Collections.sort(calendars);
		temp=new StringBuilder();
		for (Integer integer : calendars) {
			temp.append(integer).append(",");
		}
		MLog.i("after " + temp);

		calendarsCache = calendars;

		String list = calendars.toString();
		list = list.replace('[', ' ').replace(']', ' ').replace(" ", "");
		getSP().edit().putString(CALENDARS_SHOWN_ON_WIDGET, list).apply();
	}

	String getCalendarApp() {
		return getSP().getString(CALENDARS_APP_INTENT, null);
	}

	void setCalendarApp(String intent) {
		getSP().edit().putString(CALENDARS_APP_INTENT, intent).apply();

	}

}

package benor.agendawidget;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import benor.MLog.MLog;

public class ClickListener {
	public static final String CLICK_ACTION = "CLICK_ACTION";

	public static void handleWidgetClick(Context context) {
		MLog.i("got clicked");
		Widget.sendUpdateRequest(context);
		String calendarApp = Globals.DB.getCalendarApp();
		if (calendarApp != null && calendarApp.split("!").length == 2) {
			//if(currentForegroundIsLauncher(context)){
			MLog.i("calendarApp= " + calendarApp);
			Intent i = new Intent("android.intent.action.MAIN");
			i.setComponent(new ComponentName(calendarApp.split("!")[0], calendarApp.split("!")[1]));//mikado.bizcalpro
			i.addCategory("android.intent.category.LAUNCHER");
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
			//}
		} else {
			Toast.makeText(Globals.con, "choose calendar app", Toast.LENGTH_LONG).show();
		}
	}


	/**
	 * sometimes i would still get a call here when using the phone, this will help reduce those events
	 */
	@SuppressWarnings("unused")
	private static boolean currentForegroundIsLauncher(Context context) {
		String foreground = getForegroundPackage(context);
		MLog.i("foreground=" + foreground);

		String defaultLauncher = null;
		{
			PackageManager pm = context.getPackageManager();
			Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addCategory(Intent.CATEGORY_HOME);
			ResolveInfo mInfo = pm.resolveActivity(i, 0);
			if (mInfo != null) {
				defaultLauncher = mInfo.activityInfo.packageName;
			}
		}
		Toast.makeText(Globals.con, "didn't open current=" + foreground + " launcher=" + defaultLauncher, Toast.LENGTH_LONG).show();
		return foreground == null || foreground.equals(defaultLauncher);
	}

	private static String getForegroundPackage(Context context) {
		long ts = System.currentTimeMillis();
		UsageStatsManager mUsageStatsManager = context.getSystemService(UsageStatsManager.class);
		List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 1000, ts);
		if (usageStats == null || usageStats.size() == 0) {
			return null;
		}
		Collections.sort(usageStats, new Comparator<UsageStats>() {
			@Override
			public int compare(UsageStats lhs, UsageStats rhs) {
				return Long.compare(rhs.getLastTimeUsed(), lhs.getLastTimeUsed());

			}
		});
		return usageStats.get(0).getPackageName();
	}
}

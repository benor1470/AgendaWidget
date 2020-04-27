package benor.agendawidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import benor.MLog.MLog;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

public class Widget extends AppWidgetProvider {

	private static final String ACTION_CLICK = "AgendaWidgetGotClicked";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Globals.init(context);
		Globals.widget = this;

		for (int i = 0; i < appWidgetIds.length; ++i) {
			RemoteViews remoteViews = getWidgetView(context, appWidgetIds[i]);
			appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	private static RemoteViews getWidgetView(Context context, int widgetId) {
		MLog.i("recreating widget display");

		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
		Intent svcIntent = new Intent(context, AgendaWidgetService.class);
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		rv.setRemoteAdapter(R.id.LV_events, svcIntent);

		Intent intent = new Intent(context, Widget.class);
		intent.addCategory(ACTION_CLICK);
		intent.setAction(ACTION_APPWIDGET_UPDATE);
		PendingIntent clickPI = PendingIntent.getBroadcast(context, 0, intent, 0);
		rv.setPendingIntentTemplate(R.id.LV_events, clickPI);

		return rv;
	}

	public static void updateWidget(Context context) {
		AppWidgetManager manager = AppWidgetManager.getInstance(context);

		ComponentName thisWidget = new ComponentName(context, Widget.class);
		int[] ids = manager.getAppWidgetIds(thisWidget);
		for (int i = 0; i < ids.length; ++i) {
			manager.notifyAppWidgetViewDataChanged(ids, R.id.LV_events);


		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		Globals.init(context);

		MLog.i("got clicked");
		if (intent.hasCategory(ACTION_CLICK)) {
			ClickListener.openCalendarApp(context);
		}

		updateWidget(context);
	}

}


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

public class Widget extends AppWidgetProvider {

	private static final String ACTION_CLICK = "AgendaWidgetGotClicked";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Globals.init(context);
		Globals.widget = this;

		for (int appWidgetId : appWidgetIds) {
			RemoteViews remoteViews = getWidgetView(context, appWidgetId);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (ACTION_CLICK.equals(intent.getAction())) {
			Globals.init(context);
			ClickListener.handleWidgetClick(context);
		}
	}

	private RemoteViews getWidgetView(Context context, int widgetId) {
		MLog.i("recreating widget display");

		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
		//RemoteViews Service needed to provide adapter for ListView
		Intent svcIntent = new Intent(context, AgendaWidgetService.class);
		//passing app widget id to that RemoteViews Service
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		rv.setRemoteAdapter(R.id.LV_events, svcIntent);

		Intent intent = new Intent(context, getClass());
		intent.setAction(ACTION_CLICK);
		PendingIntent clickPI = PendingIntent.getBroadcast(context, 0, intent, 0);
		rv.setPendingIntentTemplate(R.id.LV_events, clickPI);

		return rv;
	}

	public static void sendUpdateRequest(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		ComponentName thisWidget = new ComponentName(context, Widget.class);
		AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, remoteViews);

	}
}


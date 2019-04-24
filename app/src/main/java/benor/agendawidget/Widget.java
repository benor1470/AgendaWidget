package benor.agendawidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.RemoteViews;
import android.widget.Toast;

import benor.MLog.MLog;

public class Widget extends AppWidgetProvider {

	public static final String ACTION_CLICK = "AgendaWidgetGotClicked";
	public static final String WIDGET_ID = "WIDGET_ID";

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
			int widgetId = intent.getIntExtra(WIDGET_ID, -1);
			ClickListener.handleWidgetClick(context, widgetId);
		}
	}

	private RemoteViews getWidgetView(Context context, int widgetId) {
		MLog.i("recreating widget display");

		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);

		Intent svcIntent = new Intent(context, WidgetListViewService.class);
		//passing app widget id to that RemoteViews Service
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		rv.setRemoteAdapter(R.id.LV_events, svcIntent);

		Intent intent = new Intent(context, getClass());
		intent.setAction(ACTION_CLICK);
		intent.putExtra(WIDGET_ID, widgetId);
		PendingIntent clickPI = PendingIntent.getBroadcast(context, 0, intent, 0);
		rv.setPendingIntentTemplate(R.id.LV_events, clickPI);

		return rv;
	}

	public static void sendUpdateRequest(Context context, int appWidgetId) {
		//Toast.makeText(context, "called an update", Toast.LENGTH_LONG).show();
		AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.LV_events);
//		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//		int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
//				new ComponentName(context, Widget.class));
//		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listview);

//		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
//		ComponentName thisWidget = new ComponentName(context, Widget.class);


//		Intent intentUpdate = new Intent(context, Widget.class);
//		intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//
//
//		int[] idArray = new int[]{appWidgetId};
//		intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
//
//		PendingIntent pendingUpdate = PendingIntent.getBroadcast(
//				context, appWidgetId, intentUpdate,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//		try {
//			pendingUpdate.send();
//		} catch (PendingIntent.CanceledException e) {
//			MLog.e("canceled");
//		}


	}
}


package benor.agendawidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import benor.MLog.MLog;

public class WidgetListViewService extends RemoteViewsService implements RemoteViewsService.RemoteViewsFactory {
	private List<Event> events = new ArrayList<>();
	private int widgetId;

	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		WidgetListViewService widgetListViewService = new WidgetListViewService(this.getApplicationContext(), intent);
		Bundle all = intent.getExtras();
		MLog.i("keys = " + all.keySet().toString());
		widgetListViewService.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -2);
		return widgetListViewService;
	}

	public WidgetListViewService() {

	}

	public WidgetListViewService(Context context, Intent intent) {
		Globals.init(context);
		widgetId = intent.getIntExtra(Widget.WIDGET_ID, -3);
	}

	public void onCreate() {
		try {
			Globals.init(getApplicationContext());
		} catch (Exception e) {
			MLog.e("can't create", e);
		}
		this.events = Event.readEvents();
	}

	public void onDestroy() {
		this.events = null;
	}

	public int getCount() {
		return events.size();
	}

	public RemoteViews getViewAt(int position) {
		return createListItem(position, true, events, widgetId);
	}

	public RemoteViews getLoadingView() {
		return null;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public long getItemId(int position) {
		return position;
	}

	public boolean hasStableIds() {
		return true;
	}

	public void onDataSetChanged() {
		this.events = Event.readEvents();
	}

	public static RemoteViews createListItem(int position, boolean onWidget, List<Event> events, int widgetId) {
		//on widget not activity

		RemoteViews rv = new RemoteViews(Globals.con.getPackageName(), R.layout.list_item);
		if (events == null || events.size() < position || events.get(position) == null) {
			return rv;//failed
		}

		Event event = events.get(position);
		//rv.setTextViewText(R.id.list_item_text, e.toString());
		rv.setTextColor(R.id.list_item_text, event.getColor());
		String text = event.toString();

		SpannableString s = new SpannableString(text);
		if (event.isAllDayEvent()) {
			s.setSpan(new UnderlineSpan(), 0, text.length(), 0);
		}
		if (event.isHappeningNow()) {
			s.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), 0);
		}
		rv.setTextViewText(R.id.list_item_text, s);

		if (position > 0) {
			if (event.getDayOfMonth() != events.get(position - 1).getDayOfMonth()) {
				rv.setViewVisibility(R.id.spacer, View.VISIBLE);
			} else {
				rv.setViewVisibility(R.id.spacer, View.GONE);
			}
		} else {
			rv.setViewVisibility(R.id.spacer, View.GONE);
		}


		Intent i = new Intent(Widget.ACTION_CLICK);
		Bundle extras = new Bundle();

		extras.putInt("calendarEventID", 1234);
		extras.putInt(Widget.WIDGET_ID, widgetId);
		i.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.list_item_text, i);

		return rv;
	}

}

package benor.agendawidget;

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

public class AgendaWidgetService extends RemoteViewsService implements RemoteViewsService.RemoteViewsFactory {
	private Event[] events;

	/**
	 * it's a service, constructor can't be deleted
	 */
	public AgendaWidgetService() {
	}

	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new AgendaWidgetService(this.getApplicationContext(), intent);
	}

	public AgendaWidgetService(Context context, Intent intent) {
		Globals.init(context);
	}

	public void onCreate() {
		try {
			Globals.init(getApplicationContext());
		} catch (Exception e) {
		}

		events = new Event[1];
		this.events = Event.readEvents().toArray(events);
	}

	public void onDestroy() {
		this.events = null;
	}

	public int getCount() {
		return events.length;
	}

	public RemoteViews getViewAt(int position) {
		return createListItem(position, true, events);
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
		this.events = Event.readEvents().toArray(events);
	}

	public static RemoteViews createListItem(int position, boolean onWidget, Event[] events) {
		//on widget not activity

		RemoteViews rv = new RemoteViews(Globals.con.getPackageName(), R.layout.list_item);
		if (events == null || events.length < position || events[position] == null) {
			return rv;//failed
		}

		Event event = events[position];
		//rv.setTextViewText(R.id.list_item_text, e.toString());
		rv.setTextColor(R.id.list_item_text, event.color);
		String text = event.toString();

		SpannableString s = new SpannableString(text);
		if (event.allDayEvent) {
			s.setSpan(new UnderlineSpan(), 0, text.length(), 0);
		}
		if (event.happeningNow) {
			s.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), 0);
		}
		rv.setTextViewText(R.id.list_item_text, s);

		if (position > 0) {
			if (event.dayOfMonth != events[position - 1].dayOfMonth) {
				rv.setViewVisibility(R.id.spacer, View.VISIBLE);
			} else {
				rv.setViewVisibility(R.id.spacer, View.GONE);
			}
		} else {
			rv.setViewVisibility(R.id.spacer, View.GONE);
		}


		Intent i = new Intent(ClickListener.CLICK_ACTION);
		Bundle extras = new Bundle();

		extras.putInt("calendarEventID", event.calendarEventID);
		i.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.list_item_text, i);

		return rv;
	}

}

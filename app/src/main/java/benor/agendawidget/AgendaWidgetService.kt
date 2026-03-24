package benor.agendawidget

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class AgendaWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return EventsFactory(applicationContext, intent)
    }

    class EventsFactory(private val context: Context, intent: Intent) : RemoteViewsFactory {
        private var events: List<Event> = emptyList()

        init {
            Globals.init(context)
        }

        override fun onCreate() {
            try {
                Globals.init(context)
            } catch (_: Exception) {
            }
            events = Event.readEvents()
        }

        override fun onDataSetChanged() {
            events = Event.readEvents()
        }

        override fun onDestroy() {
            events = emptyList()
        }

        override fun getCount() = events.size

        override fun getViewAt(position: Int): RemoteViews {
            val rv = RemoteViews(Globals.con.packageName, R.layout.list_item)
            if (position >= events.size) return rv

            val event = events[position]
            val text = event.toString()
            val s = SpannableString(text).apply {
                if (event.allDayEvent) setSpan(UnderlineSpan(), 0, text.length, 0)
                if (event.happeningNow) setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)
            }
            rv.setTextViewText(R.id.list_item_text, s)
            rv.setTextColor(R.id.list_item_text, event.color)

            val showSpacer = position > 0 && event.dayOfMonth != events[position - 1].dayOfMonth
            rv.setViewVisibility(R.id.spacer, if (showSpacer) View.VISIBLE else View.GONE)

            val fillIn = Intent(ClickListener.CLICK_ACTION).apply {
                putExtras(Bundle().apply { putInt("calendarEventID", event.calendarEventID) })
            }
            rv.setOnClickFillInIntent(R.id.list_item_text, fillIn)

            return rv
        }

        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 1
        override fun getItemId(position: Int) = position.toLong()
        override fun hasStableIds() = true
    }
}

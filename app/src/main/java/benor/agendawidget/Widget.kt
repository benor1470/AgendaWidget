package benor.agendawidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import benor.MLog.MLog

class Widget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Globals.init(context)
        Globals.widget = this
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, getWidgetView(context, id))
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Globals.init(context)
        MLog.i("widget received broadcast")
        if (intent.hasCategory(ACTION_CLICK)) {
            ClickListener.openCalendarApp(context)
        }
        updateWidget(context)
    }

    companion object {
        private const val ACTION_CLICK = "AgendaWidgetGotClicked"

        private fun getWidgetView(context: Context, widgetId: Int): RemoteViews {
            MLog.i("recreating widget display")
            val rv = RemoteViews(context.packageName, R.layout.widget)

            val svcIntent = Intent(context, AgendaWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            rv.setRemoteAdapter(R.id.LV_events, svcIntent)

            val bgColor = Globals.db.getBgColor()
            if (bgColor == null) {
                rv.setImageViewResource(R.id.IV_background, R.drawable.widget_list_shape)
            } else {
                rv.setImageViewBitmap(R.id.IV_background, generateBgBitmap(bgColor))
            }

            val clickIntent = Intent(context, Widget::class.java).apply {
                addCategory(ACTION_CLICK)
                action = ACTION_APPWIDGET_UPDATE
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
            val clickPI = PendingIntent.getBroadcast(context, 0, clickIntent, flags)
            rv.setPendingIntentTemplate(R.id.LV_events, clickPI)

            return rv
        }

        private fun generateBgBitmap(color: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            Canvas(bitmap).drawRect(0f, 0f, 100f, 100f, Paint().apply { this.color = color })
            return bitmap
        }

        fun updateWidget(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, Widget::class.java))
            for (id in ids) {
                manager.updateAppWidget(ids, getWidgetView(context, id))
            }
            manager.notifyAppWidgetViewDataChanged(ids, R.id.LV_events)
        }
    }
}

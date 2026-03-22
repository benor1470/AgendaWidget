package benor.agendawidget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import benor.MLog.MLog

object ClickListener {
    const val CLICK_ACTION = "CLICK_ACTION"

    fun openCalendarApp(context: Context) {
        val calendarApp = Globals.db.getCalendarApp()
        val parts = calendarApp?.split("!") ?: emptyList()
        if (parts.size == 2) {
            MLog.i("opening calendarApp: $calendarApp")
            val intent = Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName(parts[0], parts[1])
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(Globals.con, "choose calendar app", Toast.LENGTH_LONG).show()
        }
    }
}

package benor.agendawidget

import android.content.SharedPreferences

class DB {
    private var sp: SharedPreferences? = null
    private var calendarsCache: MutableList<Int>? = null

    private fun getSP(): SharedPreferences {
        if (sp == null) {
            sp = Globals.con.getSharedPreferences("data", 0)
        }
        return sp!!
    }

    fun getShowOnWidgetList(): MutableList<Int> {
        if (calendarsCache == null) {
            val list = mutableListOf<Int>()
            val saved = getSP().getString(CALENDARS_SHOWN_ON_WIDGET, "") ?: ""
            if (saved.isNotEmpty()) {
                saved.split(",").forEach { id -> id.toIntOrNull()?.let { list.add(it) } }
            }
            calendarsCache = list
        }
        return calendarsCache!!
    }

    fun setShowOnWidgetList(calendars: MutableList<Int>) {
        calendarsCache = calendars.toMutableList()
        val sorted = calendars.sorted().joinToString(",")
        getSP().edit().putString(CALENDARS_SHOWN_ON_WIDGET, sorted).apply()
    }

    fun getCalendarApp(): String? = getSP().getString(CALENDARS_APP_INTENT, null)

    fun setCalendarApp(intent: String) {
        getSP().edit().putString(CALENDARS_APP_INTENT, intent).apply()
    }

    fun getLang(): String = getSP().getString(SAVED_LANGUAGE, CONST.LANG_ENG) ?: CONST.LANG_ENG

    fun setLang(lang: String) {
        Globals.clearWeekDayStr()
        getSP().edit().putString(SAVED_LANGUAGE, lang).apply()
    }

    fun getBgColor(): Int? =
        if (getSP().contains(BG_COLOR)) getSP().getInt(BG_COLOR, 0x550000FF) else null

    fun setBgColor(bgColor: Int?) {
        val edit = getSP().edit()
        if (bgColor == null) edit.remove(BG_COLOR) else edit.putInt(BG_COLOR, bgColor)
        edit.apply()
    }

    companion object {
        private const val CALENDARS_SHOWN_ON_WIDGET = "CALENDARS_SHOWN_ON_WIDGET"
        private const val CALENDARS_APP_INTENT = "CALENDARS_APP_INTENT"
        private const val SAVED_LANGUAGE = "SAVED_LANGUAGE"
        private const val BG_COLOR = "BG_COLOR"
    }
}

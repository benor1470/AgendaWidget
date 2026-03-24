package benor.MLog

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MLog private constructor() : Thread.UncaughtExceptionHandler {

    private var tag: String = "App"
    private var context: Context? = null
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    fun Init(context: Context, tag: String, autoSend: Boolean, sendToLogcat: Boolean) {
        if (this.context != null) return
        this.tag = tag
        this.context = context.applicationContext
        sendToLogcat_ = sendToLogcat
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        saveToFile("Uncaught exception:\n$sw")
        previousHandler?.uncaughtException(t, e)
    }

    private fun saveToFile(content: String) {
        try {
            val file = File(context?.filesDir, "crash_${System.currentTimeMillis()}.stacktrace")
            file.writeText(content)
        } catch (_: Exception) {
        }
    }

    companion object {
        private var instance: MLog? = null
        private var sendToLogcat_ = true
        private var tag_ = "App"

        fun getInstance(): MLog {
            if (instance == null) instance = MLog()
            return instance!!
        }

        fun i(msg: String): String {
            if (sendToLogcat_) Log.i(tag_, msg)
            return msg
        }

        fun e(msg: String): String {
            if (sendToLogcat_) Log.e(tag_, msg)
            return msg
        }

        fun e(msg: String, throwable: Throwable): String {
            if (sendToLogcat_) Log.e(tag_, msg, throwable)
            return msg
        }
    }
}

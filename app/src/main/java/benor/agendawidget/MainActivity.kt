package benor.agendawidget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import benor.MLog.MLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var calendars: List<AgendaCalendar> = emptyList()
    private var installedAppsData: List<HashMap<String, String>> = emptyList()
    private var loadedApps = false
    private var displayingStartScreen = false

    private val colorPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data?.hasExtra("color") == true) {
                Globals.db.setBgColor(data.getIntExtra("color", 0))
            } else {
                Globals.db.setBgColor(null)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // After permissions dialog, the system will call onResume() automatically.
        // If denied, onResume() will exit early at the permission check below.
        if (permissions[Manifest.permission.READ_CALENDAR] == true) {
            Globals.init(applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
            )
            return
        }
        Globals.init(applicationContext)
        Globals.editListInit = false
    }

    override fun onResume() {
        super.onResume()
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) return
        // Ensure Globals is initialized even if onCreate() returned early (permissions flow)
        Globals.init(applicationContext)
        calendars = AgendaCalendar.readCalendars()
        setScreen()
        loadAppsInBackground()
    }

    private fun loadAppsInBackground() {
        if (loadedApps) return
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val pm = Globals.con.packageManager
                pm.queryIntentActivities(mainIntent, 0)
                    .map { app ->
                        hashMapOf(
                            "name" to app.loadLabel(pm).toString(),
                            "intent" to "${app.activityInfo.packageName}!${app.activityInfo.name}"
                        )
                    }
                    .sortedBy { it["name"] }
            }
            installedAppsData = apps
            loadedApps = true
            if (displayingStartScreen) {
                findViewById<View>(R.id.btn_select_calendar_app)?.isEnabled = true
            }
        }
    }

    private fun setScreen() {
        setContentView(R.layout.activity_main)
        displayingStartScreen = true
        Globals.editListInit = false

        val calsList = findViewById<ListView>(R.id.LV_cals)
        calsList.adapter = CalsAdapter(applicationContext, calendars)

        handleLanguageSelection()

        findViewById<View>(R.id.btn_select_bg_color).setOnClickListener {
            colorPickerLauncher.launch(
                Intent(applicationContext, ColorSelector::class.java).apply {
                    Globals.db.getBgColor()?.let { putExtra("color", it) }
                }
            )
        }

        findViewById<View>(R.id.btn_select_calendar_app).apply {
            isEnabled = loadedApps
            setOnClickListener {
                if (loadedApps) showAppsList()
            }
        }

        // Delay editListInit to prevent spurious checkbox callbacks during list setup
        Handler(Looper.getMainLooper()).postDelayed({
            MLog.i("list init complete")
            Globals.editListInit = true
        }, 1000)
    }

    private fun showAppsList() {
        displayingStartScreen = false
        val apps = ListView(this)
        val adapter = SimpleAdapter(
            this, installedAppsData, R.layout.app_item,
            arrayOf("name"), intArrayOf(R.id.app_name)
        )
        apps.adapter = adapter
        apps.setOnItemClickListener { _, _, position, _ ->
            MLog.i("clicked ${installedAppsData[position]["intent"]}")
            Globals.db.setCalendarApp(installedAppsData[position]["intent"] ?: "")
            setScreen()
        }
        setContentView(apps)
    }

    private fun handleLanguageSelection() {
        val spinner = findViewById<Spinner>(R.id.languageSelector)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf(CONST.LANG_ENG, CONST.LANG_HEB)
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(if (Globals.db.getLang() == CONST.LANG_ENG) 0 else 1, false)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Globals.db.setLang(if (position == 0) CONST.LANG_ENG else CONST.LANG_HEB)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (!displayingStartScreen) {
            setScreen()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        MLog.i("widget is ${Globals.widget}")
        Widget.updateWidget(applicationContext)
    }
}

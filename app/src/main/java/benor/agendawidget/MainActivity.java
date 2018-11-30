package benor.agendawidget;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import benor.MLog.MLog;

public class MainActivity extends Activity {
    public final int PERMISSIONS_REQUEST = 1;
    private List<Calendar> calendars;
    private List<ResolveInfo> pkgAppsList;
    private List<HashMap<String, Object>> installedAppsData;
    private boolean loadedApps = false;
    private boolean displayingStartScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                    PERMISSIONS_REQUEST);
            return;
        }
        Globals.root = this;
        Globals.init(getApplicationContext());
        Globals.editListInit = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        calendars = Calendar.readCalendars();

        setScreen();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!loadedApps) {
                    final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    PackageManager pm = Globals.con.getPackageManager();
                    pkgAppsList = pm.queryIntentActivities(mainIntent, 0);

                    installedAppsData = new ArrayList<>();
                    for (ResolveInfo app : pkgAppsList) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("name", app.loadLabel(pm).toString());
                        map.put("intent", app.activityInfo.packageName + "!" + app.activityInfo.name);
                        installedAppsData.add(map);
                    }
                }
                Collections.sort(installedAppsData, new Comparator<HashMap<String, Object>>() {
                    @Override
                    public int compare(HashMap<String, Object> item1, HashMap<String, Object> item2) {
                        return item1.get("name").toString().compareTo(item2.get("name").toString());
                    }
                });
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        loadedApps = true;
                        findViewById(R.id.btn_select_calendar_app).setEnabled(true);
                    }
                });

            }
        }).start();
    }

    private void setScreen() {
        setContentView(R.layout.activity_main);
        displayingStartScreen = true;
        Globals.editListInit = false;
        ListView cals = (ListView) this.findViewById(R.id.LV_cals);
        cals.setAdapter(new CalsAdapter(Globals.root.getApplicationContext(), calendars));
        findViewById(R.id.btn_select_calendar_app).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] from = new String[]{"name"};
                int[] to = new int[]{R.id.app_name};

                ListView apps = new ListView(Globals.con);
                SimpleAdapter adapter = new SimpleAdapter(Globals.con, installedAppsData, R.layout.app_item, from, to);
                apps.setAdapter(adapter);
                apps.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int position, long id) {
                        MLog.i("clicked " + installedAppsData.get(position).get("intent"));
                        Globals.DB.setCalendarApp(installedAppsData.get(position).get("intent").toString());
                        setScreen();
                    }
                });
                displayingStartScreen = false;
                setContentView(apps);


            }
        });
        if (loadedApps) {
            findViewById(R.id.btn_select_calendar_app).setEnabled(true);
        }
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                MLog.i("finished?");
                Globals.editListInit = true;
            }
        }, 1000);
    }

    public void onBackPressed() {
        if (!displayingStartScreen) {
            setScreen();
        }
    }

    public void onPause() {
        super.onPause();
        MLog.i("widget is " + Globals.widget);
        //Widget.doUpdate(AppWidgetManager.getInstance(this));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}

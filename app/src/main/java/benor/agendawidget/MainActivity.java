package benor.agendawidget;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.ActivityCompat;
import benor.MLog.MLog;

public class MainActivity extends Activity {
	public final int PERMISSIONS_REQUEST = 1;
	public final int BG_COLOR_REQUEST = 2;
	private List<Calendar> calendars;
	private List<ResolveInfo> pkgAppsList;
	private List<HashMap<String, String>> installedAppsData;
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
		Globals.init(getApplicationContext());
		Globals.editListInit = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BG_COLOR_REQUEST) {
			if (data.hasExtra("color")) {
				Globals.DB.setBgColor(data.getIntExtra("color", 0));
			} else {
				Globals.DB.setBgColor(null);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
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
						HashMap<String, String> map = new HashMap<>();
						map.put("name", app.loadLabel(pm).toString());
						map.put("intent", app.activityInfo.packageName + "!" + app.activityInfo.name);
						installedAppsData.add(map);
					}
				}
				Collections.sort(installedAppsData, new Comparator<HashMap<String, String>>() {
					@Override
					public int compare(HashMap<String, String> item1, HashMap<String, String> item2) {
						return Objects.requireNonNull(item1.get("name")).compareTo(Objects.requireNonNull(item2.get("name")));
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
		ListView cals = this.findViewById(R.id.LV_cals);
		cals.setAdapter(new CalsAdapter(getApplicationContext(), calendars));

		handleLanguageSelection();
		findViewById(R.id.btn_select_bg_color).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ColorSelector.class);
				intent.putExtra("color", Globals.DB.getBgColor());
				startActivityForResult(intent, BG_COLOR_REQUEST);
			}
		});


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
						Globals.DB.setCalendarApp(installedAppsData.get(position).get("intent"));
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

	private void handleLanguageSelection() {
		Spinner languageSelector = this.findViewById(R.id.languageSelector);
		ArrayAdapter<String> myAdapter = new ArrayAdapter<>(MainActivity.this,
				android.R.layout.simple_spinner_dropdown_item, new String[]{CONST.LANG_ENG, CONST.LANG_HEB});
		myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languageSelector.setAdapter(myAdapter);
		languageSelector.setSelection(Globals.DB.getLang().equals(CONST.LANG_ENG) ? 0 : 1, false);
		languageSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				if (i == 0) {
					Globals.DB.setLang(CONST.LANG_ENG);
				} else {
					Globals.DB.setLang(CONST.LANG_HEB);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
	}

	public void onBackPressed() {
		if (!displayingStartScreen) {
			setScreen();
		}
	}

	public void onPause() {
		super.onPause();
		MLog.i("widget is " + Globals.widget);
		Widget.updateWidget(getApplicationContext());
	}
}

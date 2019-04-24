package benor.agendawidget;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.CalendarContract;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import benor.MLog.MLog;


public class Calendar {
	public final String name;
	public final Integer id;
	public final int color;
	public boolean showOnWidget;

	public Calendar(String name, int id, int color) {
		this.showOnWidget = Globals.DB.getShowOnWidgetList().contains(id);
		this.name = name;
		this.id = id;
		this.color = color;
	}

	public String toString() {
		return name;//+(showOnWidget?"V":"X")+"_"+this.id;
	}

	void setShowOnWidget(boolean show) {
		if (this.showOnWidget != show) {
			List<Integer> list = Globals.DB.getShowOnWidgetList();
			Iterator<Integer> itr = list.iterator();
			while (itr.hasNext()) {
				if (itr.next().equals(id)) {
					itr.remove();
				}
			}
			if (show) {
				list.add(this.id);
			}
			Globals.DB.setShowOnWidgetList(list);
			this.showOnWidget = show;
		}
	}

	public static List<Calendar> readCalendars() {
		List<Calendar> calendars = new ArrayList<>();

		if (Globals.con.getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			return new ArrayList<>();
		}


		Cursor cursor = null;
		try {
			cursor = Globals.con.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null,
					null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
			/*	MLog.i("---------------------------");
				for(int i=0;i<cursor.getColumnCount();++i){
					MLog.i(cursor.getColumnName(i)+ " = "+cursor.getString(i));
				}*/
					String name = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.NAME));
					int id = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars._ID));
					int color = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR));
					calendars.add(new Calendar(name, id, color));

				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return calendars;
	}

	private static Map<Integer, Bitmap> calendarsIcons = new HashMap<>();

	public static Uri getCalIcon(int cal, int color) {
		File f = new File(Globals.con.getCacheDir() + File.separator + color + ".jpg");
		if (f.exists()) {//just while debugging
			f.delete();
			MLog.i("deleted file");
		}

		if (!f.exists()) {
			Bitmap bm = Bitmap.createBitmap(10, 10, Config.ARGB_4444);
			Canvas c = new Canvas(bm);
			Paint p = new Paint();
			p.setColor(Color.RED);
			c.drawCircle(5, 5, 4, p);
			calendarsIcons.put(cal, bm);


			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
			FileOutputStream fo;
			try {
				f.createNewFile();
				//write the bytes in file
				fo = new FileOutputStream(f);

				fo.write(bytes.toByteArray());
				fo.close();
			} catch (IOException e) {
				MLog.e("can't create file " + f.getAbsolutePath(), e);
				return null;
			}
		}
		return Uri.fromFile(f);
	}
}

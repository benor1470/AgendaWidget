package benor.agendawidget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import benor.MLog.MLog;

public class CalsAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Calendar> cals;

	public CalsAdapter(Context context, List<Calendar> cals) {
		this.cals = cals;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return cals.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.cal_item, null);
		}

		Calendar holder = cals.get(position);
		final CheckBox itemCheckBox = convertView.findViewById(R.id.list_cal_checkbox);
		itemCheckBox.setChecked(holder.showOnWidget);
		itemCheckBox.setTag(holder);
		itemCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton view, boolean isChecked) {
				Calendar cal = (Calendar) view.getTag();
				if (Globals.editListInit) {
					MLog.i("CheckedChanged " + cal + " got " + isChecked + " had " + cal.showOnWidget);
					cal.setShowOnWidget(isChecked);
				} else {
					MLog.e("blocked " + cal + " changed");
				}
				//((CheckBox)view).setSelected(cal.showOnWidget);
			}
		});


		TextView itemText = convertView.findViewById(R.id.list_cal_text);
		itemText.setText(holder.toString());
		itemText.setBackgroundColor(holder.color);
		itemText.setTextColor((holder.color + 0x7F7F7F) % 0xFFFFFF + 0xFF000000);
		itemText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				itemCheckBox.performClick();
			}
		});

		LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.list_cal_layout);
		layout.setBackgroundColor(0xFF0000);
		return convertView;
	}

}

package benor.agendawidget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import benor.MLog.MLog

class CalsAdapter(context: Context, private val cals: List<AgendaCalendar>) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)

    override fun getCount() = cals.size
    override fun getItem(position: Int) = cals[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.cal_item, parent, false)
        val cal = cals[position]

        val checkBox = view.findViewById<CheckBox>(R.id.list_cal_checkbox)
        // Clear listener before updating state to avoid spurious callbacks
        checkBox.setOnCheckedChangeListener(null)
        checkBox.isChecked = cal.showOnWidget
        checkBox.tag = cal
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (Globals.editListInit) {
                MLog.i("CheckedChanged $cal isChecked=$isChecked")
                cal.setShowOnWidget(isChecked)
            } else {
                MLog.e("blocked $cal changed")
            }
        }

        val textView = view.findViewById<TextView>(R.id.list_cal_text)
        textView.text = cal.toString()
        textView.setBackgroundColor(cal.color)
        textView.setTextColor((cal.color + 0x7F7F7F) % 0xFFFFFF + 0xFF000000.toInt())
        textView.setOnClickListener { checkBox.performClick() }

        return view
    }
}

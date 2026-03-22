package benor.agendawidget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.OpacityBar

class ColorSelector : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bg_selector)

        val picker = findViewById<ColorPicker>(R.id.picker)
        val opacityBar = findViewById<OpacityBar>(R.id.opacitybar)
        picker.addOpacityBar(opacityBar)
        picker.color = intent.getIntExtra("color", 0x000000)

        findViewById<android.view.View>(R.id.btn_done).setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra("color", picker.color))
            finish()
        }

        findViewById<android.view.View>(R.id.btn_revertToDefault).setOnClickListener {
            setResult(Activity.RESULT_OK, Intent())
            finish()
        }
    }
}

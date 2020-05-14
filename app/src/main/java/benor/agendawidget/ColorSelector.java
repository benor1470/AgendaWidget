package benor.agendawidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;

import androidx.annotation.Nullable;

public class ColorSelector extends Activity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bg_selector);
		final ColorPicker picker = findViewById(R.id.picker);
		OpacityBar opacityBar = findViewById(R.id.opacitybar);
		picker.addOpacityBar(opacityBar);

		picker.setColor(getIntent().getIntExtra("color", 0x000000));

		findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent result = new Intent();
				result.putExtra("color", picker.getColor());
				setResult(Activity.RESULT_OK, result);
				finish();
			}
		});

		findViewById(R.id.btn_revertToDefault).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent result = new Intent();
				setResult(Activity.RESULT_OK, result);
				finish();
			}
		});


	}

}

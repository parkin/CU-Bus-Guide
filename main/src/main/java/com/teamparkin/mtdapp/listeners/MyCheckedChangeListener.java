package com.teamparkin.mtdapp.listeners;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

/**
 * This is a workaround for a bug in Android 2.2 where onCheckChanged is called
 * twice! boooo! :(
 * 
 * @author parkin
 * 
 */
public abstract class MyCheckedChangeListener implements OnClickListener {

	public MyCheckedChangeListener() {
	}

	@Override
	public void onClick(View v) {
		if (v instanceof CheckBox) {
			boolean result = ((CheckBox) v).isChecked();
//			((CheckBox) v).setChecked(result);
			onCheckedChanged(result);
		}
	}

	public abstract void onCheckedChanged(boolean isChecked);

}

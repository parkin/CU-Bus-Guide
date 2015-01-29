package com.teamparkin.mtdapp.fragments;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class MyTimePickerFragment extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {

	private TimePickerDialog.OnTimeSetListener listener;
	int hour;
	int min;

	public MyTimePickerFragment() {
		listener = this;
		// use current time as default
		final Calendar c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		min = c.get(Calendar.MINUTE);
	}

	public void setInitialTime(int hour, int min) {
		this.hour = hour;
		this.min = min;
	}

	public void setListener(TimePickerDialog.OnTimeSetListener listener) {
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), listener, hour, min,
				DateFormat.is24HourFormat(getActivity()));
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Do something with the time chosen by the user
	}
}
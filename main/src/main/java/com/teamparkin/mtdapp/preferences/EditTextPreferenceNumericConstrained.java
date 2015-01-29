package com.teamparkin.mtdapp.preferences;

import android.content.Context;
import android.database.Cursor;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

import com.teamparkin.mtdapp.contentproviders.MyLocationContract;

/**
 * An EditTextPreference that requires inputType=number and only allows numbers bigger than
 * MIN_NUMBER.
 */
public class EditTextPreferenceNumericConstrained extends EditTextPreference {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = EditTextPreferenceNumericConstrained.class.getSimpleName();

    private static final int MIN_NUMBER = 60;

    public EditTextPreferenceNumericConstrained(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public EditTextPreferenceNumericConstrained(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextPreferenceNumericConstrained(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                int number;
                try {
                    number = Integer.parseInt((String) o);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Could not parse to Integer: " + o,
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (number < MIN_NUMBER) {
                    EditTextPreferenceNumericConstrained.this.setText("" + MIN_NUMBER);
                    Toast.makeText(getContext(), "Must keep at least + " + MIN_NUMBER + " entries.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    return true;
                }
            }
        });
    }

    @Override
    public CharSequence getSummary() {
        // Get the current number of rows.
        long currCount = 0;
        Cursor c = getContext().getContentResolver().query(MyLocationContract.GooglePlaces
                .COUNT_URI, null, MyLocationContract.GooglePlaces.FAVORITE + "!=1", null, null);
        if (c.moveToFirst()) {
            currCount = c.getLong(0);
        }
        c.close();
        return "Max number of entries: " + getText() + "\n"
                + "Curr number of entries: " + currCount + "\n"
                + "Note: Old entries removed only when app is closed.";
    }
}

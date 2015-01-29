package com.teamparkin.mtdapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.teamparkin.mtdapp.contentproviders.MyLocationContract;

public class DatabaseClearingService extends IntentService {
	@SuppressWarnings("unused")
	private static final String TAG = DatabaseClearingService.class
			.getSimpleName();

	public static final String SHAVE_PLACE_CACHE = "com.teamparkin.mtdapp.DatabaseClearingService.SHAVE_PLACE_CACHE";

	public DatabaseClearingService() {
		super("DatabaseClearingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(SHAVE_PLACE_CACHE))
			shavePlaceCache();
	}

	/**
	 * Deletes any nonfavorite googleplace where the timestamp <= the timestamp
	 * of the PREFERRED_MAX_COUNT nonfavorite row in the database.
	 */
	private void shavePlaceCache() {
		// Count the number of nonFavorite google places in the database
		Cursor c = getContentResolver().query(
				MyLocationContract.GooglePlaces.CONTENT_URI, null,
				MyLocationContract.GooglePlaces.FAVORITE + "!=1", null,
				MyLocationContract.GooglePlaces.TIMESTAMP + " ASC");
		int count = c.getCount();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int preferredMaxCount = Integer.parseInt(sharedPref.getString(SettingsActivity
                .KEY_PREF_DATA_STORAGE_PLACE_CACHE_MAX_SIZE, "" + 100));

		if (count > preferredMaxCount && c.moveToFirst()) {
			// position where we need to move the cursor to.
			int position = count - preferredMaxCount - 1;
			if (c.moveToPosition(position)) {
				long timestamp = c
						.getLong(c
								.getColumnIndex(MyLocationContract.GooglePlaces.TIMESTAMP));
                // delete old google places.
				getContentResolver().delete(
						MyLocationContract.GooglePlaces.CONTENT_URI,
						MyLocationContract.GooglePlaces.TIMESTAMP + " <= "
								+ timestamp + " AND " + MyLocationContract.GooglePlaces.FAVORITE
                                + " != 1"
                        , null);
			}
		}
		c.close();
	}
}

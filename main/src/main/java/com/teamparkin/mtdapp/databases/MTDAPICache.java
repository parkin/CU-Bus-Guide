package com.teamparkin.mtdapp.databases;

import com.teamparkin.mtdapp.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MTDAPICache {
	private static final String TAG = MTDAPICache.class.getSimpleName();

	// // columns in the table
	protected static final String KEY_QUERY = "QUERY";
	protected static final String KEY_JSONDATA = "JSONDATA";
	private static final String KEY_TIMESTAMP = "TIMESTAMP";

	private static final String DATABASE_TABLE = "mtdapicache";

	private static int count = 0;

	private static long MAX_DATABASE_SIZE = 1024 * 1024;

	private MTDAPIOpenHelper mHelperDatabase;
	private SQLiteDatabase mDatabase;

	private static MTDAPICache mSingleton;

	public static synchronized MTDAPICache getInstance(Context context) {
		if (mSingleton == null) {
			return new MTDAPICache(context);
		}
		return mSingleton;
	}

	private MTDAPICache(Context context) {
		mHelperDatabase = new MTDAPIOpenHelper(context);
		mDatabase = mHelperDatabase.getWritableDatabase();
		mDatabase.setMaximumSize(MAX_DATABASE_SIZE);
		mDatabase.setLockingEnabled(true);
	}

	public void openDatabase() {
		if (!mDatabase.isOpen()) {
			mDatabase = mHelperDatabase.getWritableDatabase();
		}
	}

	public void closeDatabase() {
		mDatabase.close();
	}

	/**
	 * Checks the cache for the exact query. If it exists, returns the string
	 * data. If not, returns null.
	 * 
	 * @param query
	 * @return
	 */
	public String checkQuery(String query) {
		String result = null;
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_QUERY,
				KEY_JSONDATA }, "UPPER(" + KEY_QUERY + ") LIKE UPPER('" + query
				+ "')", null, null, null, null);
		if (c.moveToFirst()) {
			result = c.getString(c.getColumnIndex(KEY_JSONDATA));
		}
		c.close();
		if (result != null) {
			// update timestamp if we are using this row
			ContentValues cv = new ContentValues();
			cv.put(KEY_TIMESTAMP, System.currentTimeMillis());
			try {
				mDatabase.update(DATABASE_TABLE, cv, "UPPER(" + KEY_QUERY
						+ ") LIKE UPPER('" + query + "')", null);
			} catch (Exception e) {
				Log.e(TAG, "Database full?");
				Log.e(TAG, e.getMessage());
			}
		}
		return result;
	}

	/**
	 * Insert the query as a new row in the database. Returns true if
	 * successful.
	 */
	public boolean cacheQueryResult(String query, String jsonData) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_QUERY, query);
		cv.put(KEY_JSONDATA, jsonData);
		cv.put(KEY_TIMESTAMP, System.currentTimeMillis());

		count++;

		boolean done = false;
		int x = 0;
		while (!done && x < 15) {
			long id = mDatabase.insert(DATABASE_TABLE, null, cv);
			if (id < 0) {
				x++;
				Log.i(TAG, "cache Database error!!! count: " + count
						+ " Page size: " + mDatabase.getPageSize());
				int removed = removeEarliestRow();
				count -= removed;
				Log.i(TAG, "Database error!!! count : " + count + " removed: "
						+ removed);
				if (x == 15) {
					Log.e(TAG, "Failed to cache :(");
				}
			} else {
				done = true;
			}
		}

		return true;
	}

	/**
	 * Delete the earliest entry in the
	 */
	private int removeEarliestRow() {
		long minTime = getMinTimeStamp();
		return mDatabase.delete(DATABASE_TABLE, KEY_TIMESTAMP + "=? ",
				new String[] { "" + minTime });
		// AND (SELECT " + KEY_FAVORITE	+ " FROM " + DATABASE_TABLE + ") = 0
	}

	//
	private long getMinTimeStamp() {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { "min("
				+ KEY_TIMESTAMP + ")" }, null, null, null, null, null);
		c.moveToFirst(); // ADD THIS!
		long timestamp = c.getLong(0);
		c.close();
		return timestamp;
	}

	/**
	 * remove a query from the table if it exists
	 * 
	 * @author parkin
	 * 
	 */
	public void removePlace(String query) {
		mDatabase.delete(DATABASE_TABLE, KEY_QUERY + "=?",
				new String[] { query });
	}

	private class MTDAPIOpenHelper extends SQLiteOpenHelper {

		private SQLiteDatabase mDatabase;

		private static final String CACHE_TABLE_CREATE = "CREATE TABLE "
				+ DATABASE_TABLE + " (" + KEY_QUERY
				+ " TEXT UNIQUE ON CONFLICT REPLACE, " + KEY_JSONDATA
				+ " TEXT, " + KEY_TIMESTAMP + " INTEGER);";

		public MTDAPIOpenHelper(Context context) {
			super(context, DATABASE_TABLE, null, context.getResources()
					.getInteger(R.integer.mtdapi_database_version));

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			mDatabase = db;
			mDatabase.execSQL(CACHE_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data.");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
			// Log.w(TAG, "Upgrading database.  For now do nothing.");
		}

	}

}

package com.teamparkin.mtdapp.databases;

import com.teamparkin.mtdapp.util.TableUtil;

import android.database.sqlite.SQLiteDatabase;

public class StopsTable {
	@SuppressWarnings("unused")
	private static final String TAG = StopsTable.class.getSimpleName();

	public static final String TABLE = "mylocations_abstractstops_stops";

	public interface Columns {
		public static final String DATA_ID = "_id";
	}

	private static final String COLUMN_INFO = "(" + Columns.DATA_ID
			+ " INTEGER PRIMARY KEY NOT NULL ON CONFLICT REPLACE, FOREIGN KEY("
			+ Columns.DATA_ID + ") REFERENCES " + AbstractStopTable.TABLE + "("
			+ AbstractStopTable.Columns.LOCATION_ID + ") ON DELETE CASCADE)";

	private StopsTable() {
	}

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE + COLUMN_INFO);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		TableUtil.onUpgrade(database, oldVersion, newVersion, TABLE,
				COLUMN_INFO);
	}

}

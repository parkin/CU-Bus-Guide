package com.teamparkin.mtdapp.databases;

import com.teamparkin.mtdapp.util.TableUtil;

import android.database.sqlite.SQLiteDatabase;

public class StopPointsTable {
	@SuppressWarnings("unused")
	private static final String TAG = StopPointsTable.class.getSimpleName();

	public static final String TABLE = "mylocations_absctractstops_stoppoints";

	public interface Columns {
		public static final String AUTO_ID = "_id";
	}

	private static final String COLUMN_INFO = "(" + Columns.AUTO_ID
			+ " INTEGER PRIMARY KEY NOT NULL ON CONFLICT REPLACE, FOREIGN KEY("
			+ Columns.AUTO_ID + ") REFERENCES " + AbstractStopTable.TABLE + "("
			+ AbstractStopTable.Columns.LOCATION_ID + ") ON DELETE CASCADE)";

	private StopPointsTable() {
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

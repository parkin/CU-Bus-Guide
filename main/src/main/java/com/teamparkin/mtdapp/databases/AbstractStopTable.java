package com.teamparkin.mtdapp.databases;

import com.teamparkin.mtdapp.util.TableUtil;

import android.database.sqlite.SQLiteDatabase;

public abstract class AbstractStopTable {
	public static final String TABLE = "mylocations_abstractstops";

	// Columns in the table.
	public interface Columns {
		public static final String LOCATION_ID = "_id";
		public static final String CODE = "code";
	}

	private static final String COLUMN_INFO = "(" + Columns.LOCATION_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Columns.CODE
			+ " TEXT, FOREIGN KEY(" + Columns.LOCATION_ID + ") REFERENCES "
			+ MyLocationTable.TABLE + "(" + MyLocationTable.Columns._ID
			+ ") ON DELETE CASCADE)";

	protected static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE + COLUMN_INFO);
	}

	protected static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		TableUtil.onUpgrade(database, oldVersion, newVersion, TABLE,
				COLUMN_INFO);
	}
}

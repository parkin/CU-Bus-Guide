package com.teamparkin.mtdapp.databases;

import com.teamparkin.mtdapp.util.TableUtil;

import android.database.sqlite.SQLiteDatabase;

public abstract class MyLocationTable {
	@SuppressWarnings("unused")
	private static final String TAG = MyLocationTable.class.getSimpleName();

	protected MyLocationTable() {
	}

	public static final String TABLE = "mylocations";

	public interface Columns {
		/**
		 * Column tag for Integer primary key autoincrement.
		 */
		public static final String _ID = "_id";
		public static final String ID = "string_id";
		/**
		 * Column tag for MyLocation's String id.
		 */
		public static final String NAME = "name";
		public static final String LAT = "lat";
		public static final String LON = "lon";
		public static final String TYPE = "type";
		public static final String SNIPPET = "snippet";
	}

	private static final String COLUMN_INFO = "(" + Columns._ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Columns.ID
			+ " TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, " + Columns.NAME
			+ " TEXT, " + Columns.LAT + " REAL, " + Columns.LON + " REAL, "
			+ Columns.TYPE + " INTEGER NOT NULL, " + Columns.SNIPPET + " TEXT)";

	protected static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE + COLUMN_INFO);
	}

	protected static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		TableUtil.onUpgrade(database, oldVersion, newVersion, TABLE,
				COLUMN_INFO);
	}

}

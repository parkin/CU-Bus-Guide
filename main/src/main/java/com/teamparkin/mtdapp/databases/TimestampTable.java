package com.teamparkin.mtdapp.databases;

import android.database.sqlite.SQLiteDatabase;
import com.teamparkin.mtdapp.util.TableUtil;

public abstract class TimestampTable {

	public static final String TABLE = "timestamps";

	// Columns in the table.
	public interface Columns {
		public static final String LOCATION_ID = "_id";
		public static final String TIMESTAMP = "timestamp";
	}

	private static final String COLUMN_INFO = "(" + Columns.LOCATION_ID
			+ " INTEGER PRIMARY KEY UNIQUE ON CONFLICT REPLACE, "
			+ Columns.TIMESTAMP
			+ " INTEGER DEFAULT (strftime('%s', 'now')) NOT NULL, FOREIGN KEY("
			+ Columns.LOCATION_ID + ") REFERENCES " + MyLocationTable.TABLE
			+ "(" + MyLocationTable.Columns._ID + ") ON DELETE CASCADE)";

	protected static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE + COLUMN_INFO);
	}

	protected static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		TableUtil.onUpgrade(database, oldVersion, newVersion, TABLE,
				COLUMN_INFO);
	}

}

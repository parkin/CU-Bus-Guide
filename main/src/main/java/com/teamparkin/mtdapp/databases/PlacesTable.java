package com.teamparkin.mtdapp.databases;

import com.teamparkin.mtdapp.util.TableUtil;

import android.database.sqlite.SQLiteDatabase;

public class PlacesTable {

	private PlacesTable() {
	}

	public static final String TABLE = "mylocations_places";

	// Columns in the table.
	public interface Columns {
		public static final String AUTO_ID = "_id";
	}

	private static final String COLUMN_INFO = "(" + Columns.AUTO_ID
			+ " INTEGER PRIMARY KEY NOT NULL ON CONFLICT REPLACE, FOREIGN KEY("
			+ Columns.AUTO_ID + ") REFERENCES " + MyLocationTable.TABLE + "("
			+ MyLocationTable.Columns._ID + ") ON DELETE CASCADE)";

	protected static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE + COLUMN_INFO);
	}

	protected static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		TableUtil.onUpgrade(database, oldVersion, newVersion, TABLE,
				COLUMN_INFO);
	}

}

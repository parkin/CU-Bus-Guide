package com.teamparkin.mtdapp.databases;

import android.database.sqlite.SQLiteDatabase;

import com.teamparkin.mtdapp.util.TableUtil;

public class FavoritesTable {

	public static final String TABLE = "favorites";

	public interface Columns {
		public static final String AUTO_ID = "_id";

		/**
		 * Column for holding whether or not the _id is a favorite. 1 for
		 * favorite. Note this table should only hold favorites.
		 */
		public static final String VALUE = "value";
	}

	private static final String COLUMN_INFO = "(" + Columns.AUTO_ID
			+ " INTEGER PRIMARY KEY NOT NULL UNIQUE ON CONFLICT REPLACE, "
			+ Columns.VALUE + " INTEGER DEFAULT 0, FOREIGN KEY("
			+ Columns.AUTO_ID + ") REFERENCES " + MyLocationTable.TABLE + "("
			+ MyLocationTable.Columns._ID + ") ON DELETE CASCADE)";

	/**
	 * Don't allow instantiation from outside.
	 */
	private FavoritesTable() {
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

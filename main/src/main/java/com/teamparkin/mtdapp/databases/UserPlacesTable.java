package com.teamparkin.mtdapp.databases;

import com.teamparkin.mtdapp.util.TableUtil;

import android.database.sqlite.SQLiteDatabase;

public class UserPlacesTable {

	private UserPlacesTable() {
	}

	public static final String TABLE = "mylocations_places_userplaces";

	public interface Columns {
		public static final String AUTO_ID = "_id";
		public static final String COMMENT = "comment";
	}

	private static final String COLUMN_INFO = "(" + Columns.AUTO_ID
			+ " INTEGER PRIMARY KEY NOT NULL ON CONFLICT REPLACE, "
			+ Columns.COMMENT + " TEXT, FOREIGN KEY(" + Columns.AUTO_ID
			+ ") REFERENCES " + PlacesTable.TABLE + "("
			+ PlacesTable.Columns.AUTO_ID + ") ON DELETE CASCADE)";

	protected static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE + COLUMN_INFO);
	}

	protected static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		TableUtil.onUpgrade(database, oldVersion, newVersion, TABLE,
				COLUMN_INFO);
	}

}

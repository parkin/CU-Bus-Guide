package com.teamparkin.mtdapp.databases;

import android.database.sqlite.SQLiteDatabase;

import com.teamparkin.mtdapp.util.TableUtil;

public class GooglePlacesDetailsTable {

	public static final String TABLE = "googleplaces_details";

	// // columns in the table
	public interface Columns {
		public static final String AUTO_ID = "_id";

		/**
		 * JSON
		 */
		public static final String ADDRESS_COMPONENTS = "address_components";
		public static final String FORMATTED_PHONE_NUMBER = "formatted_phone_number";
		public static final String INTERNATIONAL_PHONE_NUMBER = "international_phone_number";
		/**
		 * JSON
		 */
		public static final String REVIEWS = "reviews";
		public static final String UTD_OFFSET = "utc_offset";
		public static final String WEBSITE = "website";
	}

	private static final String COLUMN_INFO = "(" + Columns.AUTO_ID
			+ " INTEGER PRIMARY KEY NOT NULL ON CONFLICT REPLACE, "
			+ Columns.ADDRESS_COMPONENTS + " TEXT, "
			+ Columns.FORMATTED_PHONE_NUMBER + " TEXT, "
			+ Columns.INTERNATIONAL_PHONE_NUMBER + " TEXT, " + Columns.REVIEWS
			+ " TEXT, " + Columns.UTD_OFFSET + " TEXT, " + Columns.WEBSITE
			+ " TEXT, FOREIGN KEY(" + Columns.AUTO_ID + ") REFERENCES "
			+ PlacesTable.TABLE + "(" + GooglePlacesTable.Columns.AUTO_ID
			+ ") ON DELETE CASCADE)";

	private GooglePlacesDetailsTable() {
	}

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE + COLUMN_INFO);
	}

	protected static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		TableUtil.onUpgrade(database, oldVersion, newVersion, TABLE,
				COLUMN_INFO);
	}

}

package com.teamparkin.mtdapp.databases;

import com.teamparkin.mtdapp.util.TableUtil;

import android.database.sqlite.SQLiteDatabase;

public class GooglePlacesTable extends MyLocationTable {
	@SuppressWarnings("unused")
	private static final String TAG = GooglePlacesTable.class.getSimpleName();

	public static final String TABLE = "mylocations_places_googleplaces";

	// // columns in the table
	public interface Columns {
		public static final String AUTO_ID = "_id";
		/**
		 * Contains text of a JSON array of the events.
		 */
		public static final String EVENTS = "events";
		/**
		 * Not guaranteed to be non-null.
		 */
		public static final String FORMATTED_ADDRESS = "formatted_address";
		/**
		 * Contains text of the url to the icon;
		 */
		public static final String ICON = "icon";
		/**
		 * Text. JSON array.
		 */
		public static final String OPENING_HOURS = "opening_hours";
		/**
		 * Text. JSON array.
		 */
		public static final String PHOTOS = "photos";
		/**
		 * 0 — Free 1 — Inexpensive 2 — Moderate 3 — Expensive 4 — Very
		 * Expensive
		 */
		public static final String PRICE_LEVEL = "price_level";
		public static final String RATING = "rating";
		public static final String REFERENCE = "reference";
		public static final String TYPES = "types";
		public static final String VICINITY = "vicinity";
	}

	private static final String COLUMN_INFO = "(" + Columns.AUTO_ID
			+ " INTEGER PRIMARY KEY NOT NULL ON CONFLICT REPLACE, "
			+ Columns.EVENTS + " TEXT, " + Columns.FORMATTED_ADDRESS
			+ " TEXT, " + Columns.ICON + " TEXT, " + Columns.OPENING_HOURS
			+ " TEXT, " + Columns.PHOTOS + " TEXT, " + Columns.PRICE_LEVEL
			+ " INTEGER, " + Columns.RATING + " REAL, " + Columns.REFERENCE
			+ " TEXT, " + Columns.TYPES + " TEXT, " + Columns.VICINITY
			+ " TEXT, FOREIGN KEY(" + Columns.AUTO_ID + ") REFERENCES "
			+ PlacesTable.TABLE + "(" + PlacesTable.Columns.AUTO_ID
			+ ") ON DELETE CASCADE)";

	private GooglePlacesTable() {
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

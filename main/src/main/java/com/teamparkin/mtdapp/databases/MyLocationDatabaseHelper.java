package com.teamparkin.mtdapp.databases;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;

public class MyLocationDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "mylocations.db";

	private Context mContext;

	private static MyLocationDatabaseHelper mSingleton;

	public interface Triggers {
		public static final String DELETE_STOP = "delete_stop";
		public static final String DELETE_STOPPOINT = "delete_stopopint";
		public static final String DELETE_GOOGLE_PLACE = "delete_google_place";
		public static final String DELETE_USER_PLACE = "delete_user_place";
		public static final String DELETE_FAVORITE = "delete_favorite";

		public static final String INSERT_MYLOCATION = "insert_mylocation";
		public static final String INSERT_FAVORITE = "insert_favorite";
		public static final String INSERT_GOOGLE_PLACE_DETAILS = "insert_google_place_details";

		public static final String UPDATE_MYLOCATION = "update_mylocation";
		public static final String UPDATE_ABSTRACT_STOPS = "update_abstract_stops";
		public static final String UPDATE_STOPS = "update_stops";
		public static final String UPDATE_STOPPOINTS = "update_stoppoints";
		public static final String UPDATE_PLACE = "update_place";
		public static final String UPDATE_GOOGLE_PLACE = "update_google_place";
		public static final String UPDATE_GOOGLE_PLACE_DETAILS = "update_google_place_details";
		public static final String UPDATE_USER_PLACE = "update_user_place";
		public static final String UPDATE_FAVORITES = "update_favorites";
	}

	/**
	 * After a row in the stops database is deleted, a row in the mylocations
	 * database will be deleted.
	 */
	private static final String TRIGGER_DELETE_STOP = "CREATE TRIGGER "
			+ Triggers.DELETE_STOP + " AFTER DELETE ON " + StopsTable.TABLE
			+ " BEGIN DELETE FROM " + MyLocationTable.TABLE + " WHERE "
			+ MyLocationTable.TABLE + "." + MyLocationTable.Columns._ID
			+ " = OLD." + StopsTable.Columns.DATA_ID + "; end;";

	private static final String TRIGGER_DELETE_STOPPOINT = "CREATE TRIGGER "
			+ Triggers.DELETE_STOPPOINT + " AFTER DELETE ON "
			+ StopPointsTable.TABLE + " BEGIN DELETE FROM "
			+ MyLocationTable.TABLE + " WHERE " + MyLocationTable.TABLE + "."
			+ MyLocationTable.Columns._ID + " = OLD."
			+ StopPointsTable.Columns.AUTO_ID + "; end;";

	private static final String TRIGGER_DELETE_GOOGLE_PLACE = "CREATE TRIGGER "
			+ Triggers.DELETE_GOOGLE_PLACE + " AFTER DELETE ON "
			+ GooglePlacesTable.TABLE + " BEGIN DELETE FROM "
			+ MyLocationTable.TABLE + " WHERE " + MyLocationTable.TABLE + "."
			+ MyLocationTable.Columns._ID + " = OLD."
			+ GooglePlacesTable.Columns.AUTO_ID + "; end;";

	private static final String TRIGGER_DELETE_USER_PLACE = "CREATE TRIGGER "
			+ Triggers.DELETE_USER_PLACE + " AFTER DELETE ON "
			+ UserPlacesTable.TABLE + " BEGIN DELETE FROM "
			+ MyLocationTable.TABLE + " WHERE " + MyLocationTable.TABLE + "."
			+ MyLocationTable.Columns._ID + " = OLD."
			+ UserPlacesTable.Columns.AUTO_ID + "; end;";

	private static final String TRIGGER_INSERT_MYLOCATION = "CREATE TRIGGER "
			+ Triggers.INSERT_MYLOCATION + " AFTER INSERT ON "
			+ MyLocationTable.TABLE + " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE + "(" + TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW." + MyLocationTable.Columns._ID + "); end;";

	private static final String TRIGGER_UPDATE_MYLOCATION = "CREATE TRIGGER "
			+ Triggers.UPDATE_MYLOCATION + " AFTER UPDATE ON "
			+ MyLocationTable.TABLE + " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE + "(" + TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW." + MyLocationTable.Columns._ID + "); end;";

	private static final String TRIGGER_UPDATE_ABSTRACTSTOPS = "CREATE TRIGGER "
			+ Triggers.UPDATE_ABSTRACT_STOPS
			+ " AFTER UPDATE ON "
			+ AbstractStopTable.TABLE
			+ " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE
			+ "("
			+ TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW."
			+ AbstractStopTable.Columns.LOCATION_ID
			+ "); end;";

	private static final String TRIGGER_UPDATE_STOPS = "CREATE TRIGGER "
			+ Triggers.UPDATE_STOPS + " AFTER UPDATE ON " + StopsTable.TABLE
			+ " BEGIN INSERT OR REPLACE INTO " + TimestampTable.TABLE + "("
			+ TimestampTable.Columns.LOCATION_ID + ") VALUES(NEW."
			+ StopsTable.Columns.DATA_ID + "); end;";

	private static final String TRIGGER_UPDATE_STOPPOINTS = "CREATE TRIGGER "
			+ Triggers.UPDATE_STOPPOINTS + " AFTER UPDATE ON "
			+ StopPointsTable.TABLE + " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE + "(" + TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW." + StopPointsTable.Columns.AUTO_ID + "); end;";

	private static final String TRIGGER_UPDATE_PLACE = "CREATE TRIGGER "
			+ Triggers.UPDATE_PLACE + " AFTER UPDATE ON " + PlacesTable.TABLE
			+ " BEGIN INSERT OR REPLACE INTO " + TimestampTable.TABLE + "("
			+ TimestampTable.Columns.LOCATION_ID + ") VALUES(NEW."
			+ PlacesTable.Columns.AUTO_ID + "); end;";

	private static final String TRIGGER_UPDATE_GOOGLE_PLACE = "CREATE TRIGGER "
			+ Triggers.UPDATE_GOOGLE_PLACE + " AFTER UPDATE ON "
			+ GooglePlacesTable.TABLE + " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE + "(" + TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW." + GooglePlacesTable.Columns.AUTO_ID + "); end;";

	private static final String TRIGGER_UPDATE_GOOGLE_PLACE_DETAILS = "CREATE TRIGGER "
			+ Triggers.UPDATE_GOOGLE_PLACE_DETAILS
			+ " AFTER UPDATE ON "
			+ GooglePlacesDetailsTable.TABLE
			+ " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE
			+ "("
			+ TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW."
			+ GooglePlacesDetailsTable.Columns.AUTO_ID
			+ "); end;";

	private static final String TRIGGER_INSERT_GOOGLE_PLACE_DETAILS = "CREATE TRIGGER "
			+ Triggers.INSERT_GOOGLE_PLACE_DETAILS
			+ " AFTER INSERT ON "
			+ GooglePlacesDetailsTable.TABLE
			+ " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE
			+ "("
			+ TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW."
			+ GooglePlacesDetailsTable.Columns.AUTO_ID
			+ "); end;";

	private static final String TRIGGER_UPDATE_USER_PLACE = "CREATE TRIGGER "
			+ Triggers.UPDATE_USER_PLACE + " AFTER UPDATE ON "
			+ UserPlacesTable.TABLE + " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE + "(" + TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW." + UserPlacesTable.Columns.AUTO_ID + "); end;";

	private static final String TRIGGER_UPDATE_FAVORITES = "CREATE TRIGGER "
			+ Triggers.UPDATE_FAVORITES + " AFTER UPDATE ON "
			+ FavoritesTable.TABLE + " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE + "(" + TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW." + FavoritesTable.Columns.AUTO_ID + "); end;";

	private static final String TRIGGER_INSERT_FAVORITES = "CREATE TRIGGER "
			+ Triggers.INSERT_FAVORITE + " AFTER INSERT ON "
			+ FavoritesTable.TABLE + " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE + "(" + TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(NEW." + FavoritesTable.Columns.AUTO_ID + "); end;";

	private static final String TRIGGER_DELETE_FAVORITES = "CREATE TRIGGER "
			+ Triggers.DELETE_FAVORITE + " AFTER DELETE ON "
			+ FavoritesTable.TABLE + " BEGIN INSERT OR REPLACE INTO "
			+ TimestampTable.TABLE + "(" + TimestampTable.Columns.LOCATION_ID
			+ ") VALUES(OLD." + FavoritesTable.Columns.AUTO_ID + "); end;";

	public interface Views {
		public static final String MYLOCATIONS = "view_mylocations";
		public static final String ABSTRACT_STOPS = "view_abstractstops";
		public static final String STOPS = "view_stops";
		public static final String STOPPOINTS = "view_stoppoints";
		public static final String PLACES = "view_places";
		public static final String GOOGLE_PLACES = "view_google_places";
		public static final String USER_PLACES = "view_user_places";
	}

	/* **************** Table strings, with joins if necessary ************ */

	/**
	 * mylocationtable join favorite_locations on (mlt._id=ft._id)
	 */
	private static final String tMY_LOCATION_JOIN = MyLocationTable.TABLE
			+ " LEFT OUTER JOIN " + FavoritesTable.TABLE + " ON ("
			+ MyLocationTable.TABLE + "." + MyLocationTable.Columns._ID + " = "
			+ FavoritesTable.TABLE + "." + FavoritesTable.Columns.AUTO_ID
			+ ") JOIN " + TimestampTable.TABLE + " ON ("
			+ MyLocationTable.TABLE + "." + MyLocationTable.Columns._ID + " = "
			+ TimestampTable.TABLE + "." + TimestampTable.Columns.LOCATION_ID
			+ ")";

	/**
	 * mylocationtable join favorite_locations on (mlt._id=ft._id) join
	 * abstractstops on (mlt._id=abs._id)
	 */
	private static final String tABSTRACT_STOPS_JOIN = tMY_LOCATION_JOIN
			+ " JOIN " + AbstractStopTable.TABLE + " ON ("
			+ MyLocationTable.TABLE + "." + MyLocationTable.Columns._ID + " = "
			+ AbstractStopTable.TABLE + "."
			+ AbstractStopTable.Columns.LOCATION_ID + ")";

	/**
	 * mylocationtable join favorite_locations on (mlt._id=ft._id)join
	 * abstractstops on (mlt._id=abs._id) left outer join stops on
	 * (stops._id=abs._id)
	 */
	private static final String tSTOPS_JOIN = tABSTRACT_STOPS_JOIN + " JOIN "
			+ StopsTable.TABLE + " ON (" + AbstractStopTable.TABLE + "."
			+ AbstractStopTable.Columns.LOCATION_ID + " = " + StopsTable.TABLE
			+ "." + StopsTable.Columns.DATA_ID + ")";

	/**
	 * mylocationtable join favorite_locations on (mlt._id=ft._id)join
	 * abstractstops on (mlt._id=abs._id) left outer join stoppoints on
	 * (stoppoints._id=abs._id)
	 */
	private static final String tSTOPPOINTS_JOIN = tABSTRACT_STOPS_JOIN
			+ " JOIN " + StopPointsTable.TABLE + " ON ("
			+ AbstractStopTable.TABLE + "."
			+ AbstractStopTable.Columns.LOCATION_ID + " = "
			+ StopPointsTable.TABLE + "." + StopPointsTable.Columns.AUTO_ID
			+ ")";

	private static final String tPLACE_JOIN = tMY_LOCATION_JOIN + " JOIN "
			+ PlacesTable.TABLE + " ON (" + MyLocationTable.TABLE + "."
			+ MyLocationTable.Columns._ID + " = " + PlacesTable.TABLE + "."
			+ PlacesTable.Columns.AUTO_ID + ")";

	private static final String tGOOGLE_PLACE_JOIN = tPLACE_JOIN + " JOIN "
			+ GooglePlacesTable.TABLE + " ON (" + PlacesTable.TABLE + "."
			+ PlacesTable.Columns.AUTO_ID + " = " + GooglePlacesTable.TABLE
			+ "." + GooglePlacesTable.Columns.AUTO_ID + ") LEFT OUTER JOIN "
			+ GooglePlacesDetailsTable.TABLE + " ON ("
			+ GooglePlacesTable.TABLE + "." + GooglePlacesTable.Columns.AUTO_ID
			+ " = " + GooglePlacesDetailsTable.TABLE + "."
			+ GooglePlacesDetailsTable.Columns.AUTO_ID + ")";

	private static final String tUSER_PLACE_JOIN = tPLACE_JOIN + " JOIN "
			+ UserPlacesTable.TABLE + " ON (" + PlacesTable.TABLE + "."
			+ PlacesTable.Columns.AUTO_ID + " = " + UserPlacesTable.TABLE + "."
			+ UserPlacesTable.Columns.AUTO_ID + ")";

	/* ********************* FROM strings ************************ */

	private static final String tMY_LOCATION_SELECT = MyLocationTable.TABLE
			+ "." + MyLocationTable.Columns._ID + " AS "
			+ MyLocationContract.MyLocation.DATA_ID + ", "
			+ MyLocationTable.Columns.ID + " AS "
			+ MyLocationContract.MyLocation.ID + ", "
			+ MyLocationTable.Columns.LAT + " AS "
			+ MyLocationContract.MyLocation.LAT + ", "
			+ MyLocationTable.Columns.LON + " AS "
			+ MyLocationContract.MyLocation.LON + ", "
			+ MyLocationTable.Columns.NAME + " AS "
			+ MyLocationContract.MyLocation.NAME + ", "
			+ MyLocationTable.Columns.TYPE + " AS "
			+ MyLocationContract.MyLocation.TYPE + ", "
			+ MyLocationTable.Columns.SNIPPET + " AS "
			+ MyLocationContract.MyLocation.SNIPPET + ", " + "IFNULL("
			+ FavoritesTable.TABLE + "." + FavoritesTable.Columns.VALUE
			+ ", 0)" + " AS " + MyLocationContract.MyLocation.FAVORITE + ", "
			+ TimestampTable.TABLE + "." + TimestampTable.Columns.TIMESTAMP
			+ " AS " + MyLocationContract.MyLocation.TIMESTAMP;

	private static final String tABSTRACT_STOPS_SELECT = tMY_LOCATION_SELECT
			+ ", " + MyLocationContract.StopPoints.CODE;

	private static final String tSTOPS_SELECT = tABSTRACT_STOPS_SELECT;

	private static final String tSTOPPOINTS_SELECT = tABSTRACT_STOPS_SELECT;

	private static final String tPLACES_SELECT = tMY_LOCATION_SELECT;

	private static final String tGOOGLE_PLACES_SELECT = tPLACES_SELECT
			+ ", "
			+ GooglePlacesTable.Columns.EVENTS
			+ " AS "
			+ MyLocationContract.GooglePlaces.EVENTS
			+ ", "
			+ GooglePlacesTable.Columns.FORMATTED_ADDRESS
			+ " AS "
			+ MyLocationContract.GooglePlaces.FORMATTED_ADDRESS
			+ ", "
			+ GooglePlacesTable.Columns.ICON
			+ " AS "
			+ MyLocationContract.GooglePlaces.ICON
			+ ", "
			+ GooglePlacesTable.Columns.OPENING_HOURS
			+ " AS "
			+ MyLocationContract.GooglePlaces.OPENING_HOURS
			+ ", "
			+ GooglePlacesTable.Columns.PHOTOS
			+ " AS "
			+ MyLocationContract.GooglePlaces.PHOTOS
			+ ", "
			+ GooglePlacesTable.Columns.PRICE_LEVEL
			+ " AS "
			+ MyLocationContract.GooglePlaces.PRICE_LEVEL
			+ ", "
			+ GooglePlacesTable.Columns.RATING
			+ " AS "
			+ MyLocationContract.GooglePlaces.RATING
			+ ", "
			+ GooglePlacesTable.Columns.REFERENCE
			+ " AS "
			+ MyLocationContract.GooglePlaces.REFERENCE
			+ ", "
			+ GooglePlacesTable.Columns.TYPES
			+ " AS "
			+ MyLocationContract.GooglePlaces.TYPES
			+ ", "
			+ GooglePlacesTable.Columns.VICINITY
			+ " AS "
			+ MyLocationContract.GooglePlaces.VICINITY
			+ ", "
			+ GooglePlacesDetailsTable.Columns.ADDRESS_COMPONENTS
			+ " AS "
			+ MyLocationContract.GooglePlaces.DetailsColumns.ADDRESS_COMPONENTS
			+ ", "
			+ GooglePlacesDetailsTable.Columns.FORMATTED_PHONE_NUMBER
			+ " AS "
			+ MyLocationContract.GooglePlaces.DetailsColumns.FORMATTED_PHONE_NUMBER
			+ ", "
			+ GooglePlacesDetailsTable.Columns.INTERNATIONAL_PHONE_NUMBER
			+ " AS "
			+ MyLocationContract.GooglePlaces.DetailsColumns.INTERNATIONAL_PHONE_NUMBER
			+ ", " + GooglePlacesDetailsTable.Columns.REVIEWS + " AS "
			+ MyLocationContract.GooglePlaces.DetailsColumns.REVIEWS + ", "
			+ GooglePlacesDetailsTable.Columns.UTD_OFFSET + " AS "
			+ MyLocationContract.GooglePlaces.DetailsColumns.UTD_OFFSET + ", "
			+ GooglePlacesDetailsTable.Columns.WEBSITE + " AS "
			+ MyLocationContract.GooglePlaces.DetailsColumns.WEBSITE;

	private static final String tUSER_PLACES_SELECT = tPLACES_SELECT + ", "
			+ UserPlacesTable.Columns.COMMENT + " AS "
			+ MyLocationContract.UserPlaces.COMMENT;

	public static MyLocationDatabaseHelper getInstance(Context context) {
		if (mSingleton == null)
			return new MyLocationDatabaseHelper(context);
		return mSingleton;
	}

	public MyLocationDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, context.getResources().getInteger(
				R.integer.database_mylocation_version));
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// ** Create the tables
		MyLocationTable.onCreate(db);
		AbstractStopTable.onCreate(db);
		StopsTable.onCreate(db);
		StopPointsTable.onCreate(db);
		PlacesTable.onCreate(db);
		GooglePlacesTable.onCreate(db);
		GooglePlacesDetailsTable.onCreate(db);
		UserPlacesTable.onCreate(db);
		FavoritesTable.onCreate(db);
		TimestampTable.onCreate(db);

		// ** Create the triggers.
		db.execSQL(TRIGGER_DELETE_STOP);
		db.execSQL(TRIGGER_DELETE_STOPPOINT);
		db.execSQL(TRIGGER_DELETE_GOOGLE_PLACE);
		db.execSQL(TRIGGER_DELETE_USER_PLACE);
		db.execSQL(TRIGGER_DELETE_FAVORITES);

		db.execSQL(TRIGGER_INSERT_MYLOCATION);
		db.execSQL(TRIGGER_INSERT_FAVORITES);
		db.execSQL(TRIGGER_INSERT_GOOGLE_PLACE_DETAILS);

		db.execSQL(TRIGGER_UPDATE_MYLOCATION);
		db.execSQL(TRIGGER_UPDATE_ABSTRACTSTOPS);
		db.execSQL(TRIGGER_UPDATE_STOPS);
		db.execSQL(TRIGGER_UPDATE_STOPPOINTS);
		db.execSQL(TRIGGER_UPDATE_PLACE);
		db.execSQL(TRIGGER_UPDATE_GOOGLE_PLACE);
		db.execSQL(TRIGGER_UPDATE_GOOGLE_PLACE_DETAILS);
		db.execSQL(TRIGGER_UPDATE_USER_PLACE);
		db.execSQL(TRIGGER_UPDATE_FAVORITES);

		// ** Create the Views
		db.execSQL("CREATE VIEW " + Views.MYLOCATIONS + " AS SELECT "
				+ tMY_LOCATION_SELECT + " FROM " + tMY_LOCATION_JOIN);
		db.execSQL("CREATE VIEW " + Views.ABSTRACT_STOPS + " AS SELECT "
				+ tABSTRACT_STOPS_SELECT + " FROM " + tABSTRACT_STOPS_JOIN);
		db.execSQL("CREATE VIEW " + Views.STOPS + " AS SELECT " + tSTOPS_SELECT
				+ " FROM " + tSTOPS_JOIN);
		db.execSQL("CREATE VIEW " + Views.STOPPOINTS + " AS SELECT "
				+ tSTOPPOINTS_SELECT + " FROM " + tSTOPPOINTS_JOIN);
		db.execSQL("CREATE VIEW " + Views.PLACES + " AS SELECT "
				+ tPLACES_SELECT + " FROM " + tPLACE_JOIN);
		db.execSQL("CREATE VIEW " + Views.GOOGLE_PLACES + " AS SELECT "
				+ tGOOGLE_PLACES_SELECT + " FROM " + tGOOGLE_PLACE_JOIN);
		db.execSQL("CREATE VIEW " + Views.USER_PLACES + " AS SELECT "
				+ tUSER_PLACES_SELECT + " FROM " + tUSER_PLACE_JOIN);

		// Set the bad database flags so we know to download everything.
		setDatabaseFlagBad();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO update tables if needed
		// StopsTable.onUpgrade(db, oldVersion, newVersion);
		// StopPointsTable.onUpgrade(db, oldVersion, newVersion);
		// GooglePlacesTable.onUpgrade(db, oldVersion, newVersion);
		// FavoritesTable.onUpgrade(db, oldVersion, newVersion);

		// TODO update triggers on updating.
		// // ** drop old triggers so we can recreate them.
		// db.execSQL("DROP TRIGGER IF EXISTS "
		// + Triggers.AFTER_MYLOCATIONS_DELETE_DELETE_FAVORITE);
		// db.execSQL("DROP TRIGGER IF EXISTS "
		// + Triggers.AFTER_STOPPOINTS_DELETE_DELETE_FROM_MYLOCATION);
		// db.execSQL("DROP TRIGGER IF EXISTS "
		// + Triggers.AFTER_STOPS_DELETE_DELETE_FROM_MYLOCATION);
		//
		// // ** Create the triggers.
		// db.execSQL(TRIGGER_AFTER_STOPS_DELETE_STRING);
		// db.execSQL(TRIGGER_AFTER_STOPPOINTS_DELETE_STRING);
		// db.execSQL(TRIGGER_DELETE_AFTER_GOOGLE_PLACE_DELETE);
		// db.execSQL(TRIGGER_DELETE_AFTER_USER_PLACE_DELETE);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		// Enable foreign key constraints. Note the only time this is enforced
		// is during writes, so don't need this on during reads.
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	private void setDatabaseFlagBad() {
		SharedPreferences settings = mContext.getSharedPreferences(
				DatabaseAdapter.DbOk, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(DatabaseAdapter.DbOkStops, DatabaseAdapter.IS_BAD);
		editor.commit();

	}

}

package com.teamparkin.mtdapp.databases;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.teamparkin.mtdapp.dataclasses.Route;

public class RoutesDatabase {
	private static final String TAG = RoutesDatabase.class.getSimpleName();

	// columns in the table
	protected static final String KEY_ID = "ID";
	private static final String KEY_COLOR = "COLOR";
	private static final String KEY_LONGNAME = "LONGNAME";
	private static final String KEY_SHORTNAME = "SHORTNAME";
	private static final String KEY_TEXTCOLOR = "TEXTCOLOR";
	private static final String KEY_FAVORITE = "FAV";

	/*
	 * Do NOT change without Will's approval
	 */
	private static final int DATABASE_VERSION = 3;
	private static final String DATABASE_TABLE = "routes";
	private static final String UPDATE_TABLE = "routesupdate";

	private StopsDatabaseOpenHelper mHelperDatabase;
	private SQLiteDatabase mDatabase;

	private static RoutesDatabase instance;

	public static synchronized RoutesDatabase getInstance(Context context) {
		if (instance == null) {
			return new RoutesDatabase(context);
		}
		return instance;
	}

	private RoutesDatabase(Context context) {
		instance = this;
		mHelperDatabase = new StopsDatabaseOpenHelper(context);
		mDatabase = mHelperDatabase.getWritableDatabase();
	}

	public void openDatabase() {
		if (!mDatabase.isOpen()) {
			mDatabase = mHelperDatabase.getWritableDatabase();
		}
	}

	public void closeDatabase() {
		mDatabase.close();
	}

	public boolean contains(Route route) {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID }, KEY_ID + "=?",
				new String[] { route.getId() }, null, null, null);
		boolean ret = false;
		if (c.moveToFirst())
			ret = true;
		c.close();
		return ret;
	}

	public void setFavorite(Route route, boolean b) {
		ContentValues cv = new ContentValues();
		if (b)
			cv.put(KEY_FAVORITE, 1);
		else
			cv.put(KEY_FAVORITE, 0);

		mDatabase.update(DATABASE_TABLE, cv, KEY_ID + "='" + route.getId() + "'", null);
	}

	public boolean isFavorite(Route route) {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_FAVORITE }, KEY_ID
				+ "=?", new String[] { route.getId() }, null, null, null);
		boolean ret = false;
		if (c.moveToFirst()) {
			ret = c.getInt(c.getColumnIndex(KEY_FAVORITE)) == 1;
		}
		c.close();
		return ret;
	}

	public List<Route> searchRouteName(String name) {
		List<Route> result = new ArrayList<Route>();
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, "UPPER(" + KEY_LONGNAME
				+ ") LIKE UPPER('%" + name + "%') OR UPPER(" + KEY_SHORTNAME + ") LIKE UPPER('%"
				+ name + "%')", null, null, null, KEY_LONGNAME + " COLLATE NOCASE");
		if (c.moveToFirst()) {
			do {
				result.add(getRouteFromCursor(c));
			} while (c.moveToNext());
		}

		c.close();
		return result;
	}

	public Route getRouteByName(String name) {
		Route result = null;
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, "UPPER(" + KEY_LONGNAME
				+ ") = UPPER('" + name + "') OR UPPER(" + KEY_SHORTNAME + ") = UPPER('%" + name
				+ "%')", null, null, null, null);
		if (c.moveToFirst()) {
			result = getRouteFromCursor(c);
		}

		c.close();
		return result;
	}

	public Route getRouteById(String id) {
		Route result = null;
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, KEY_ID + "='" + id + "'", null, null,
				null, null);
		if (c.moveToFirst()) {
			result = getRouteFromCursor(c);
		}

		c.close();
		return result;
	}

	public List<Route> getAllFavoriteRoutes() {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, KEY_FAVORITE + "=?",
				new String[] { "" + 1 }, null, null, KEY_LONGNAME + " COLLATE NOCASE");
		List<Route> result = new ArrayList<Route>();
		if (c.moveToFirst()) {
			do {
				result.add(getRouteFromCursor(c));
			} while (c.moveToNext());
		}
		c.close();
		return result;
	}

	public List<Route> getAllRoutes() {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, null, null, null, null, KEY_SHORTNAME
				+ " ASC, " + KEY_LONGNAME + " COLLATE NOCASE");
		List<Route> result = new ArrayList<Route>();
		if (c.moveToFirst()) {
			do {
				result.add(getRouteFromCursor(c));
			} while (c.moveToNext());
		}
		c.close();
		return result;
	}

	public List<Route> getWeekdayDaytimeRoutes() {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, null, null, null, null, KEY_SHORTNAME
				+ " ASC, " + KEY_LONGNAME + " COLLATE NOCASE");
		List<Route> result = new ArrayList<Route>();
		String shortname;
		if (c.moveToFirst()) {
			do {
				shortname = c.getString(c.getColumnIndex(KEY_SHORTNAME));
				if (shortname.equals("10") || !shortname.contains("0"))
					result.add(getRouteFromCursor(c));
			} while (c.moveToNext());
		}
		c.close();
		return result;
	}

	public List<Route> getWeekdayNightRoutes() {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, null, null, null, null, KEY_SHORTNAME
				+ " ASC, " + KEY_LONGNAME + " COLLATE NOCASE");
		List<Route> result = new ArrayList<Route>();
		String shortname;
		String longname;
		if (c.moveToFirst()) {
			do {
				shortname = c.getString(c.getColumnIndex(KEY_SHORTNAME));
				longname = c.getString(c.getColumnIndex(KEY_LONGNAME)).toUpperCase(Locale.US);
				if (shortname.contains("0")
						&& !(shortname.equals("10") || longname.contains("WEEKEND")
								|| longname.contains("SATURDAY") || longname.contains("SUNDAY")))
					result.add(getRouteFromCursor(c));
			} while (c.moveToNext());
		}
		c.close();
		return result;
	}

	public List<Route> getSaturdayDaytimeRoutes() {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, null, null, null, null, KEY_SHORTNAME
				+ " ASC, " + KEY_LONGNAME + " COLLATE NOCASE");
		List<Route> result = new ArrayList<Route>();
		String shortname;
		String longname;
		if (c.moveToFirst()) {
			do {
				shortname = c.getString(c.getColumnIndex(KEY_SHORTNAME));
				longname = c.getString(c.getColumnIndex(KEY_LONGNAME)).toUpperCase(Locale.US);
				if (shortname.contains("0") && !shortname.equals("10")
						&& (longname.contains("WEEKEND") || longname.contains("SATURDAY")))
					result.add(getRouteFromCursor(c));
			} while (c.moveToNext());
		}
		c.close();
		return result;
	}

	public List<Route> getSaturdayNightRoutes() {
		return getWeekdayNightRoutes();
	}

	public List<Route> getSundayDaytimeRoutes() {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, null, null, null, null, KEY_SHORTNAME
				+ " ASC, " + KEY_LONGNAME + " COLLATE NOCASE");
		List<Route> result = new ArrayList<Route>();
		String shortname;
		String longname;
		if (c.moveToFirst()) {
			do {
				shortname = c.getString(c.getColumnIndex(KEY_SHORTNAME));
				longname = c.getString(c.getColumnIndex(KEY_LONGNAME)).toUpperCase(Locale.US);
				if (shortname.contains("0") && !shortname.equals("10")
						&& (longname.contains("WEEKEND") || longname.contains("SUNDAY")))
					result.add(getRouteFromCursor(c));
			} while (c.moveToNext());
		}
		c.close();
		return result;
	}

	public List<Route> getSundayEveningRoutes() {
		Cursor c = mDatabase.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_COLOR, KEY_ID,
				KEY_LONGNAME, KEY_SHORTNAME, KEY_TEXTCOLOR }, null, null, null, null, KEY_SHORTNAME
				+ " ASC, " + KEY_LONGNAME + " COLLATE NOCASE");
		List<Route> result = new ArrayList<Route>();
		String shortname;
		String longname;
		if (c.moveToFirst()) {
			do {
				shortname = c.getString(c.getColumnIndex(KEY_SHORTNAME));
				longname = c.getString(c.getColumnIndex(KEY_LONGNAME)).toUpperCase(Locale.US);
				if (shortname.contains("0")
						&& !shortname.equals("10")
						&& !longname.contains("EVENING")
						&& (shortname.equals("50") || shortname.equals("220")
								|| shortname.equals("100") || shortname.equals("120") || shortname
									.equals("130")))
					result.add(getRouteFromCursor(c));
			} while (c.moveToNext());
		}
		c.close();
		return result;
	}

	private Route getRouteFromCursor(Cursor c) {
		return new Route(c.getString(c.getColumnIndex(KEY_COLOR)), c.getString(c
				.getColumnIndex(KEY_ID)), c.getString(c.getColumnIndex(KEY_LONGNAME)),
				c.getString(c.getColumnIndex(KEY_SHORTNAME)), c.getString(c
						.getColumnIndex(KEY_TEXTCOLOR)));
	}

	/**
	 * Insert the stop as a new row in the database. Returns true if successful.
	 */
	public boolean addRoute(Route route) {
		return addRoute(route, DATABASE_TABLE);
	}

	private boolean addRoute(Route route, String table) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_FAVORITE, 0);
		cv.put(KEY_ID, route.getId());
		cv.put(KEY_COLOR, route.getRoute_color());
		cv.put(KEY_LONGNAME, route.getRoute_long_name());
		cv.put(KEY_SHORTNAME, route.getRoute_short_name());
		cv.put(KEY_TEXTCOLOR, route.getRoute_text_color());
		cv.put(KEY_FAVORITE, 0);

		mDatabase.insert(table, null, cv);

		return true;
	}

	public String addRoutesFromJson(final Context context, JSONObject jobj,
			Notification notification, NotificationManager mNotificationManager,
			int routeNotificationId, PendingIntent contentIntent) {
		return addRoutesFromJsonHelper(context, jobj, notification, mNotificationManager,
				routeNotificationId, contentIntent, DATABASE_TABLE);
	}

	private String addRoutesFromJsonHelper(final Context context, JSONObject jobj,
			Notification notification, NotificationManager mNotificationManager,
			int routeNotificationId, PendingIntent contentIntent, String table) {
		String ret = "";
		JSONArray jarray;
		boolean success = false;
		try {
			jarray = jobj.getJSONArray("routes");
			JSONObject jroute;
			mDatabase.beginTransaction();
			for (int i = 0; i < jarray.length(); i++) {
				jroute = jarray.getJSONObject(i);
				if (jroute == null) {
					Log.i(TAG, "jroute null");
				}
				addRoute(new Route(jroute.getString("route_color"), jroute.getString("route_id"),
						jroute.getString("route_long_name"), jroute.getString("route_short_name"),
						jroute.getString("route_text_color")), table);
				// publish progress
				if (i % 20 == 0) {
					String s = "Route(" + i + "/" + jarray.length() + "): "
							+ jroute.getString("route_short_name");
					notification.setLatestEventInfo(context, "Initializing routes database", s,
							contentIntent);
					mNotificationManager.notify(routeNotificationId, notification);
				}
			}
			success = true;
		} catch (JSONException e) {
			Log.e(TAG, "downloadRotues" + " " + e.getMessage());
			e.printStackTrace();
			ret = e.getMessage();
		}
		if (table.equals(UPDATE_TABLE)) {
			mDatabase.execSQL("DROP TABLE " + DATABASE_TABLE + ";");
			mDatabase.execSQL("ALTER TABLE " + UPDATE_TABLE + " RENAME TO " + DATABASE_TABLE + ";");
		}
		if (success) {
			mDatabase.setTransactionSuccessful();
		}
		mDatabase.endTransaction();
		return ret;
	}

	public String updateRoutesFromJson(Context context, JSONObject jobj, Notification notification,
			NotificationManager mNotificationManager, int routeNotificationId,
			PendingIntent contentIntent) {
		String createString = "CREATE TABLE " + UPDATE_TABLE + " (" + KEY_ID
				+ " TEXT UNIQUE ON CONFLICT REPLACE, " + KEY_LONGNAME + " TEXT, " + KEY_SHORTNAME
				+ " TEXT, " + KEY_COLOR + " TEXT, " + KEY_TEXTCOLOR + " TEXT, " + KEY_FAVORITE
				+ " INTEGER);";
		mDatabase.execSQL(createString);
		return addRoutesFromJsonHelper(context, jobj, notification, mNotificationManager,
				routeNotificationId, contentIntent, UPDATE_TABLE);
	}

	private class StopsDatabaseOpenHelper extends SQLiteOpenHelper {

		private SQLiteDatabase mDatabase;

		private Context mHContext;

		private static final String ROUTES_TABLE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " ("
				+ KEY_ID + " TEXT UNIQUE ON CONFLICT REPLACE, " + KEY_LONGNAME + " TEXT, "
				+ KEY_SHORTNAME + " TEXT, " + KEY_COLOR + " TEXT, " + KEY_TEXTCOLOR + " TEXT, "
				+ KEY_FAVORITE + " INTEGER);";

		public StopsDatabaseOpenHelper(Context context) {
			super(context, DATABASE_TABLE, null, DATABASE_VERSION);
			mHContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			mDatabase = db;
			mDatabase.execSQL(ROUTES_TABLE_CREATE);
			setDatabaseFlagBad();
		}

		private void setDatabaseFlagBad() {
			SharedPreferences settings = mHContext.getSharedPreferences(DatabaseAdapter.DbOk, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(DatabaseAdapter.DbOkRoutes, DatabaseAdapter.IS_BAD);
			editor.commit();
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database.  For now do nothing.");
		}

	}

}

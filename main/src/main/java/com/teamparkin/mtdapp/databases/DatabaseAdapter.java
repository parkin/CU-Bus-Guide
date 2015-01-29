package com.teamparkin.mtdapp.databases;

import android.content.Context;

public class DatabaseAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = DatabaseAdapter.class.getSimpleName();

	private RoutesDatabase mDatabaseRoutes;
	private MTDAPICache mDatabaseMTDAPICache;

	public static final String DbOk = "DatabaseOk";
	public static final String DbOkStops = "StopsDbOk";
	public static final String DbOkRoutes = "RoutesDbOk";
	public static final String DbStopsUpdateTime = "StopsUpdateTime";
	public static final String DbRoutesUpdateTime = "RoutesUpdateTime";
	/**
	 * Store System.currentTimeMills() from last GTFS check.
	 */
	public static final String DbLastGTFSCheck = "LastGTFSCheck";

	/**
	 * Database has not been loaded.
	 */
	public static final int IS_BAD = 13;
	/**
	 * Database has been loaded properly!
	 */
	public static final int IS_OK = 14;
	/**
	 * Database is currently loading.
	 */
	public static final int IS_WORKING = 15;

	public static final int IS_UPDATING = 16;

	private static DatabaseAdapter mSingleton;

	private DatabaseAdapter(Context context) {
		mDatabaseRoutes = RoutesDatabase.getInstance(context);
		mDatabaseMTDAPICache = MTDAPICache.getInstance(context);
	}

	public static synchronized DatabaseAdapter getInstance(Context context) {
		if (mSingleton == null) {
			mSingleton = new DatabaseAdapter(context);
		}
		return mSingleton;
	}

	public void openDatabase() {
		mDatabaseRoutes.openDatabase();
		mDatabaseMTDAPICache.openDatabase();
	}

	public void closeDatabase() {
		mDatabaseRoutes.closeDatabase();
		mDatabaseMTDAPICache.closeDatabase();
	}

	/**
	 * Returns JSON string, or null if not in cache.
	 * 
	 * @param method
	 * @param parameters
	 * @return
	 */
	public String checkMTDAPICache(String query) {
		mDatabaseMTDAPICache.openDatabase();
		return mDatabaseMTDAPICache.checkQuery(query);
	}

	public void cacheQueryResult(String query, String jsonString) {
		mDatabaseMTDAPICache.openDatabase();
		mDatabaseMTDAPICache.cacheQueryResult(query, jsonString);
	}

}

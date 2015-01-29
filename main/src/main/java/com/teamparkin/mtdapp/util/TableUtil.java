package com.teamparkin.mtdapp.util;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableUtil {
	private static final String TAG = TableUtil.class.getSimpleName();

	private TableUtil() {
	}

	/**
	 * 
	 * @param database
	 * @param oldVersion
	 * @param newVersion
	 * @param table
	 * @param createString
	 * @param columnInfo
	 *            - your substring of your create table string, the part where
	 *            you initialize the columns. Eg
	 *            "(_id integer primary key, chat text, lat real, lon real)".
	 *            Please include the ().
	 */
	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion, String table, String columnInfo) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion);

		// NOTE: the below does not handle table downgrades.
		// http://stackoverflow.com/questions/3505900/sqliteopenhelper-onupgrade-confusion-android

		database.beginTransaction();

		// when doing an upgrade, this table might not exist, so make it if it
		// doesn't
		database.execSQL("CREATE TABLE IF NOT EXISTS " + table + columnInfo);

		// get the columns of the current table.
		List<String> columns = DbUtil.GetColumns(database, table);

		String tempTableString = table + "_temp";

		// backup table
		database.execSQL("ALTER TABLE " + table + " RENAME TO "
				+ tempTableString);

		// Create table with new schema
		database.execSQL("CREATE TABLE " + table + columnInfo);

		// get all of the columns from the old table still in the new table.
		columns.retainAll(DbUtil.GetColumns(database, table));

		// restore data from old table into new table.
		String cols = DbUtil.join(columns, ",");
		database.execSQL(String.format("INSERT INTO %s (%s) SELECT %s from %s",
				table, cols, cols, tempTableString));

		// remove the temporary table
		database.execSQL("DROP TABLE IF EXISTS " + tempTableString);

		database.setTransactionSuccessful();
		database.endTransaction();

	}

	// /**
	// * Moves the current table to TEMP_TABLE and creates a new table with the
	// * normal table name.
	// *
	// */
	// public void putCurrentTableInTempTableAndCreateNew() {
	// SQLiteDatabase db = getWritableDatabase();
	// // call create regular table first just in case it's not created yet.
	// db.execSQL(getCreateIfNotExistsString(getTableString()));
	// // make sure to drop the temp table if it's still there for some reason.
	// db.execSQL("DROP TABLE IF EXISTS " + getTempTableString());
	// db.execSQL("ALTER TABLE " + getTableString() + " RENAME TO "
	// + getTempTableString() + ";");
	// db.execSQL(getCreateIfNotExistsString(getTableString()));
	// }

	/**
	 * Drops the temp table.
	 * 
	 * @param db
	 *            a writeable database.
	 */
	public static void dropUpdateTableHelper(SQLiteDatabase db,
			String tempTable) {
		db.execSQL("DROP TABLE IF EXISTS " + tempTable + ";");
	}

}

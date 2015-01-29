package com.teamparkin.mtdapp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DbUtil {

	private DbUtil() {
	}

	public static List<String> GetColumns(SQLiteDatabase db, String tableName) {
		List<String> ar = null;
		Cursor c = null;
		try {
			c = db.rawQuery("select * from " + tableName + " limit 1", null);
			if (c != null) {
				ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
			}
		} catch (Exception e) {
			Log.v(tableName, e.getMessage(), e);
			e.printStackTrace();
		} finally {
			if (c != null)
				c.close();
		}
		return ar;
	}

	public static String join(List<String> list, String delim) {
		StringBuilder buf = new StringBuilder();
		int num = list.size();
		for (int i = 0; i < num; i++) {
			if (i != 0)
				buf.append(delim);
			buf.append((String) list.get(i));
		}
		return buf.toString();
	}

	/**
	 * Creates a column info string for the columnNames and columnTypes. Eg if
	 * columnNames={"id", "height", "name"} and
	 * columnTypes={"integer primary key autoincrement", "real", "text"}, this
	 * function will return
	 * "id integer primary key autoincrement, height real, name text".
	 * 
	 * This function returns null if either columnNames or columnTypes is null,
	 * or if either of their sizes is less than one.
	 * 
	 * This function only adds up to Math.min(columnNames.length,
	 * columnTypes.length) entries.
	 * 
	 * @param columnNames
	 * @param columnTypes
	 * @return
	 */
	public static String createColumnInfo(String[] columnNames,
			String[] columnTypes) {
		if (columnNames == null || columnTypes == null
				|| columnNames.length < 1 || columnTypes.length < 1) {
			return null;
		}
		String ret = "";
		int minSize = Math.min(columnNames.length, columnTypes.length);
		for (int i = 0; i < minSize; i++) {
			ret += columnNames[i] + " " + columnTypes[i];
			if (i < minSize - 1)
				ret += ", ";
		}
		return ret;
	}

}

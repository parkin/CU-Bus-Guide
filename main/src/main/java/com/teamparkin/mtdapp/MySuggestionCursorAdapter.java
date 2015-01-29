package com.teamparkin.mtdapp;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MySuggestionCursorAdapter extends CursorAdapter {

	public MySuggestionCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		TextView bigText = (TextView) view
				.findViewById(R.id.suggestion_big_text);
		bigText.setText(cursor.getString(cursor
				.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)));

		TextView smallText = (TextView) view
				.findViewById(R.id.suggestion_small_text);
		smallText.setText(cursor.getString(cursor
				.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)));

		ImageView iv = (ImageView) view.findViewById(R.id.suggestion_icon);
		int columnIndex = cursor
				.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1);
		iv.setImageResource(cursor.getInt(columnIndex));

		ImageView iv2 = (ImageView) view
				.findViewById(R.id.suggestion_icon2);
		columnIndex = cursor
				.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_2);
		iv2.setImageResource(cursor.getInt(columnIndex));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(
				R.layout.suggestion_item, parent, false);
	}

}
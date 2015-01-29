package com.teamparkin.mtdapp.views;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContentProvider;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.dataclasses.MyLocation;

public class LocationItemView extends LinearLayout {
	@SuppressWarnings("unused")
	private static final String TAG = LocationItemView.class.getSimpleName();

	private MyLocation mLocation;

	private OnFavoriteCheckChangedListener mFavoriteCheckListener;

	public LocationItemView(Context context) {
		this(context, null);
	}

	public LocationItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.stoplist_item, this, true);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public LocationItemView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setLocationInfo(Cursor cursor) {
		boolean isFav = cursor.getInt(cursor
				.getColumnIndex(MyLocationContract.MyLocation.FAVORITE)) == 1;
		int type = cursor.getInt(cursor
				.getColumnIndex(MyLocationContract.MyLocation.TYPE));
		final Uri uri = MyLocationContentProvider.getUriFromCursor(cursor);
		ImageView typeImage = (ImageView) findViewById(R.id.stop_list_item_type);
		typeImage.setImageResource(MyLocationContract
				.getLocationTypeDrawableResId(type));
		typeImage.setBackgroundColor(MyLocationContract
				.getLocationTypeBackgroundColor(type));

		TextView nameTv = (TextView) findViewById(R.id.stoplist_item_bigtext);
		nameTv.setText(cursor.getString(cursor
				.getColumnIndex(MyLocationContract.MyLocation.NAME)));

		TextView snip = (TextView) findViewById(R.id.stoplist_item_smalltext);
		snip.setText(cursor.getString(cursor
				.getColumnIndex(MyLocationContract.MyLocation.SNIPPET)));

		CheckBox starCheckBox = (CheckBox) findViewById(R.id.stoplist_item_checkBox);
		// remove checkedChangeListener before setting checked so old location
		// doesn't get accidentally set.
		starCheckBox.setOnCheckedChangeListener(null);
		starCheckBox.setChecked(isFav);
		final long rowId = cursor.getLong(cursor
				.getColumnIndex(MyLocationContract.MyLocation.DATA_ID));
		starCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						ContentValues values = new ContentValues();
						values.put(MyLocationContract.MyLocation.FAVORITE,
								isChecked ? 1 : 0);
						getContext().getContentResolver().update(uri, values,
								null, null);
						dispatchOnFavoriteCheckChanged(rowId, isChecked);
					}
				});
	}

	public MyLocation getLocation() {
		return mLocation;
	}

	public void setOnFavoriteCheckChangedListener(
			OnFavoriteCheckChangedListener listener) {
		mFavoriteCheckListener = listener;
	}

	private void dispatchOnFavoriteCheckChanged(long rowId, boolean isChecked) {
		if (mFavoriteCheckListener != null)
			mFavoriteCheckListener.onFavoriteCheckChanged(rowId, isChecked);
		else
			Log.e(TAG, "dispatchOnFavoriteCheckChanged listener null");
	}

	public interface OnFavoriteCheckChangedListener {
		public void onFavoriteCheckChanged(long rowId, boolean isChecked);
	}

}

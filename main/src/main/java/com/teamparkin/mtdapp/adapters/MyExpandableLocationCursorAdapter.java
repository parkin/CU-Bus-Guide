package com.teamparkin.mtdapp.adapters;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContentProvider;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener.OnLocationSelectedListener;
import com.teamparkin.mtdapp.restadapters.GoogleStreetViewAPIAdapter;
import com.teamparkin.mtdapp.views.LocationItemView;
import com.teamparkin.mtdapp.views.StreetViewTouchImageView;

public class MyExpandableLocationCursorAdapter extends
		MyExpandableCursorAdapter {
	private static final String TAG = MyExpandableLocationCursorAdapter.class
			.getSimpleName();

	private Map<String, Boolean> mUrlIsOpen = new HashMap<String, Boolean>();
	private GoogleStreetViewAPIAdapter mStreetViewAdapter;
	private OnLocationSelectedListener mLocationSelectedListener;

	public MyExpandableLocationCursorAdapter(Context context, Cursor c,
			int flags, int layoutResId, int titleParentResId,
			int contentParentResId) {
		super(context, c, flags, layoutResId, titleParentResId,
				contentParentResId);
		mStreetViewAdapter = GoogleStreetViewAPIAdapter.getInstance(context);
	}

	@Override
	public View onExpandRequested(View contentView) {
		StreetViewTouchImageView iv = (StreetViewTouchImageView) contentView
				.findViewById(R.id.mylocationlistitem_child_image);
		if (iv != null) {
			Picasso.with(mContext).setDebugging(true);
			Picasso.with(mContext)
					.load(iv.getUrl())
					.placeholder(
							GoogleStreetViewAPIAdapter
									.getDefaultDrawable(mContext)).into(iv);
			mUrlIsOpen.put(iv.getUrl(), true);
		}
		return null;
	}

	@Override
	public View onCollapseRequested(View contentView) {
		StreetViewTouchImageView iv = (StreetViewTouchImageView) contentView
				.findViewById(R.id.mylocationlistitem_child_image);
		if (iv != null) {
			mUrlIsOpen.remove(iv.getUrl());
		}
		return null;
	}

	@Override
	public View getTitleView(Cursor cursor, View convertView, ViewGroup parent) {
		View parentView = convertView;
		if (parentView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			parentView = inflater.inflate(
					R.layout.mylocationlistitem_card_parent, null);
		}
		final Uri uri = MyLocationContentProvider.getUriFromCursor(cursor);
		LocationItemView view = (LocationItemView) parentView
				.findViewById(R.id.mylocationlistitem_locationitem);
		view.setLocationInfo(cursor);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchLocationSelected(uri);
			}
		});
		// TODO implement long click listener.
		// view.setOnLongClickListener(getOnLongClickListener(loc));
		return parentView;
	}

	@Override
	public View getContentView(Cursor cursor, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.mylocationlistitem_card_child,
					null);
		}
		StreetViewTouchImageView iv = (StreetViewTouchImageView) view
				.findViewById(R.id.mylocationlistitem_child_image);
		// explicitly set so that the list adapter doesn't give it another
		// item's value. This means that we load on each opening, however
		// images
		// should be cached so it's fine.
		// doesn't reuse values
		double lat = cursor.getDouble(cursor
				.getColumnIndex(MyLocationContract.MyLocation.LAT));
		double lon = cursor.getDouble(cursor
				.getColumnIndex(MyLocationContract.MyLocation.LON));
		String streetViewUrl = mStreetViewAdapter.getStreetViewUrl(lat, lon);
		iv.setUrl(streetViewUrl);
		if (mUrlIsOpen.containsKey(streetViewUrl)
				&& mUrlIsOpen.get(streetViewUrl)) {
			// if the iv is expanded, make sure its image is correct.
			Picasso.with(mContext).setDebugging(true);
			Picasso.with(mContext)
					.load(iv.getUrl())
					.placeholder(
							GoogleStreetViewAPIAdapter
									.getDefaultDrawable(mContext)).into(iv);
		} else {
			iv.setImageDrawable(GoogleStreetViewAPIAdapter
					.getDefaultDrawable(mContext));
		}
		return view;
	}

	public void setOnLocationSelectedListener(
			MyFragmentsListener.OnLocationSelectedListener listener) {
		this.mLocationSelectedListener = listener;
	}

	private void dispatchLocationSelected(Uri location) {
		if (this.mLocationSelectedListener != null)
			this.mLocationSelectedListener.onLocationSelected(location);
		else
			Log.e(TAG, "dispatchLocationSelected listener null");
	}

}
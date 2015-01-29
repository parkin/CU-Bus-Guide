package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.teamparkin.mtdapp.R;

public abstract class Place extends MyLocation {
	@SuppressWarnings("unused")
	private static final String TAG = Place.class.getSimpleName();

	private static final int IMAGE_ID = R.drawable.place_icon;
	public static final int MAP_ICON_FAV = R.drawable.ic_circle_orange_star;
	public static final int MAP_ICON_NONFAV = R.drawable.ic_circle_orange;
	// Set where maps should
	private static final float[] ANCHOR_NONFAV = { 0.5f, 0.5f };
	private static final float[] ANCHOR_FAV = { 0.5f, 0.5f };

	public Place(String name, String id, LatLng latLng, long rowId) {
		super(id, name, latLng, rowId);
	}

	public Place(Parcel source) {
		super(source);
	}

	@Override
	public int getMapIconId(boolean favorite) {
		if (favorite)
			return MAP_ICON_FAV;
		else
			return MAP_ICON_NONFAV;
	}

	@Override
	public float[] getAnchorPosition(boolean favorite) {
		if (favorite)
			return ANCHOR_FAV;
		else
			return ANCHOR_NONFAV;
	}

	@Override
	public int getImageId() {
		return IMAGE_ID;
	}

	@Override
	public float getMarkerHue() {
		return BitmapDescriptorFactory.HUE_ORANGE;
	}

}

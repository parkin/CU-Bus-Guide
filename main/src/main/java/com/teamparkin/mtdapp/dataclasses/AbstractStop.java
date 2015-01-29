package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.teamparkin.mtdapp.R;

public abstract class AbstractStop extends MyLocation {

	private static final int IMAGE_ID = R.drawable.bus_icon;
	private static final int MAP_ICON_FAV = R.drawable.ic_circle_blue_star;
	private static final int MAP_ICON_NONFAV = R.drawable.ic_circle_blue;
	private static final float[] ANCHOR_POSITION = new float[] { 0.5f, 0.5f };
	private static final float[] ANCHOR_NONFAV = { 0.5f, 0.5f };

	private String code;

	public AbstractStop(String id, String name, LatLng latlng, String code, long rowId) {
		super(id, name, latlng, rowId);
		this.code = code;
	}

	public AbstractStop(Parcel source) {
		super(source);
		this.code = source.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(code);
	}

	public String getCode() {
		return code;
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
		return favorite ? ANCHOR_POSITION : ANCHOR_NONFAV;
	}

	@Override
	public int getImageId() {
		return IMAGE_ID;
	}

	@Override
	public float getMarkerHue() {
		return BitmapDescriptorFactory.HUE_BLUE;
	}

}

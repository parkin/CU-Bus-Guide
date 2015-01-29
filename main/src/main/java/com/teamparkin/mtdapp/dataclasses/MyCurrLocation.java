package com.teamparkin.mtdapp.dataclasses;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.teamparkin.mtdapp.R;

public class MyCurrLocation extends MyLocation {
	@SuppressWarnings("unused")
	private static final String TAG = MyCurrLocation.class.getSimpleName();

	private static final int IMAGE_ID = R.drawable.walker;
	private static final int MYLOCATION_PIN = R.drawable.mylocation_pin;

	boolean isSet = false;

	private static MyCurrLocation mSingleton;
	private static final String NAME = "My Location";

	public synchronized static MyCurrLocation getInstance() {
		if (mSingleton == null) {
			mSingleton = new MyCurrLocation("My Location", new LatLng(0.0, 0.0));
		}
		return mSingleton;
	}

	protected MyCurrLocation(String id, LatLng latlng) {
		super(id, NAME, latlng, -1);
	}

	public static final Parcelable.Creator<MyCurrLocation> CREATOR = new Parcelable.Creator<MyCurrLocation>() {
		@Override
		public MyCurrLocation createFromParcel(Parcel source) {
			return new MyCurrLocation(source);
		}

		@Override
		public MyCurrLocation[] newArray(int size) {
			return new MyCurrLocation[size];
		}
	};

	public MyCurrLocation(Parcel source) {
		super(source);
		isSet = (source.readInt() == 1) ? true : false;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(isSet ? 1 : 0);
	}

	public void setLocation(Location location) {
		this.latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		isSet = true;
	}

	public boolean isSet() {
		return isSet;
	}

	@Override
	public int getMapIconId(boolean favorite) {
		return MYLOCATION_PIN;
	}

	@Override
	public float[] getAnchorPosition(boolean favorite) {
		float f[] = new float[2];
		f[0] = 0.5f;
		f[1] = 0.5f;
		return f;
	}

	@Override
	public int getImageId() {
		return IMAGE_ID;
	}

}

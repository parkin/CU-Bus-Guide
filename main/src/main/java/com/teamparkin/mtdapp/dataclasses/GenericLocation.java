package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class GenericLocation extends MyLocation {

	public static final Parcelable.Creator<GenericLocation> CREATOR = new Parcelable.Creator<GenericLocation>() {
		@Override
		public GenericLocation createFromParcel(Parcel source) {
			return new GenericLocation(source);
		}

		@Override
		public GenericLocation[] newArray(int size) {
			return new GenericLocation[size];
		}
	};

	public GenericLocation(String id, String name, LatLng latLng) {
		// rowId is not applicable
		super(id, name, latLng, -1);
	}

	public GenericLocation(Parcel source) {
		super(source);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeParcelable(latLng, 0);
	}

	@Override
	public int getMapIconId(boolean favorite) {
		return 0;
	}

	@Override
	public float[] getAnchorPosition(boolean favorite) {
		return null;
	}

	@Override
	public int getImageId() {
		return 0;
	}

}

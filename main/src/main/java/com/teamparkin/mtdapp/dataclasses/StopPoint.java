package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class should likely be the super class of Stop. Not sure
 * Parcelable.Creator would work well like that though.
 * 
 * @author will
 * 
 */
public class StopPoint extends AbstractStop {
	@SuppressWarnings("unused")
	private static final String TAG = StopPoint.class.getSimpleName();

	public static final Parcelable.Creator<StopPoint> CREATOR = new Parcelable.Creator<StopPoint>() {
		@Override
		public StopPoint createFromParcel(Parcel source) {
			return new StopPoint(source);
		}

		@Override
		public StopPoint[] newArray(int size) {
			return new StopPoint[size];
		}
	};

	public StopPoint(String stop_id, double stop_lat, double stop_lon,
			String stop_name, String code, long rowId) {
		super(stop_id, stop_name, new LatLng(stop_lat, stop_lon), code, rowId);
	}

	public StopPoint(Parcel source) {
		super(source);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
	}

}

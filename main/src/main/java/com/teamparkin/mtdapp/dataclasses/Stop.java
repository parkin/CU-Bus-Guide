package com.teamparkin.mtdapp.dataclasses;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Stop extends AbstractStop {
	@SuppressWarnings("unused")
	private static final String TAG = Stop.class.getSimpleName();

	protected List<StopPoint> stopPoints;

	public Stop(String id, String name, LatLng latlng, String code, long rowId) {
		super(id, name, latlng, code, rowId);
		stopPoints = new ArrayList<StopPoint>();
	}

	public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
		@Override
		public Stop createFromParcel(Parcel source) {
			return new Stop(source);
		}

		@Override
		public Stop[] newArray(int size) {
			return new Stop[size];
		}
	};

	public Stop(String id, String stopName, String code,
			List<StopPoint> stopPoints, LatLng latLng, long rowId) {
		this(id, stopName, latLng, code, rowId);
		this.stopPoints = stopPoints;
	}

	public Stop(String id, String stopName, String code,
			List<StopPoint> stopPoints, double lat, double lon, long rowId) {
		this(id, stopName, code, stopPoints, new LatLng(lat, lon), rowId);
	}

	public Stop(Parcel source) {
		super(source);
		stopPoints = new ArrayList<StopPoint>();
		source.readList(stopPoints, StopPoint.class.getClassLoader());
	}

	public String getName() {
		return this.name;
	}

	public List<StopPoint> getStopPoints() {
		return stopPoints;
	}

	public int getNumberOfStopPoints() {
		return stopPoints.size();
	}

	public StopPoint getStopPoint(int position) {
		return stopPoints.get(position);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeList(stopPoints);
	}

}

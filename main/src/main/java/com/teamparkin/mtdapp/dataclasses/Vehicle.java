package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Vehicle extends IdBasedData implements Parcelable {
	private static final String TAG = Vehicle.class.getSimpleName();

	Trip trip;
	LatLng latLng;
	String previousStopId;
	String nextStopId;
	String originStopId;
	String destinationStopId;
	String lastUpdated;

	public Vehicle(String id, Trip trip, LatLng latLng, String previousStopId,
			String nextStopId, String originStopId, String destinationStopId,
			String lastUpdated) {
		super(id);
		this.trip = trip;
		this.latLng = latLng;
		this.previousStopId = previousStopId;
		this.nextStopId = nextStopId;
		this.originStopId = originStopId;
		this.destinationStopId = destinationStopId;
		this.lastUpdated = lastUpdated;
	}
	
	public Vehicle(Parcel source) {
		super(source.readString()); // reads Id
		this.trip = source.readParcelable(Trip.class.getClassLoader());
		this.latLng = source.readParcelable(LatLng.class.getClassLoader());
		this.previousStopId = source.readString();
		this.nextStopId = source.readString();
		this.originStopId = source.readString();
		this.destinationStopId = source.readString();
		this.lastUpdated = source.readString();
	}
	
	public static final Parcelable.Creator<Vehicle> CREATOR = new Parcelable.Creator<Vehicle>() {
		@Override
		public Vehicle createFromParcel(Parcel source) {
			return new Vehicle(source);
		}

		@Override
		public Vehicle[] newArray(int size) {
			return new Vehicle[size];
		}
	};
	

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeParcelable(trip, 0);
		dest.writeParcelable(latLng, 0);
		dest.writeString(previousStopId);
		dest.writeString(nextStopId);
		dest.writeString(originStopId);
		dest.writeString(destinationStopId);
		dest.writeString(lastUpdated);
	}
	
	

}

package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Departure extends IdBasedData implements Parcelable {
	@SuppressWarnings("unused")
	private static final String TAG = Departure.class.getSimpleName();

	private String headsign;
	private String vehicle_id;
	private Route route;
	private Trip trip;
	private LatLng latLng;
	private int expected_mins;
	private String scheduled;
	private boolean isIstop; // is this a stop where people can board without
								// fare

	public static final Parcelable.Creator<Departure> CREATOR = new Parcelable.Creator<Departure>() {
		@Override
		public Departure createFromParcel(Parcel source) {
			return new Departure(source);
		}

		@Override
		public Departure[] newArray(int size) {
			return new Departure[size];
		}
	};

	public Departure(Parcel source) {
		super(source); // reads ID
		headsign = source.readString();
		vehicle_id = source.readString();
		route = source.readParcelable(Route.class.getClassLoader());
		trip = source.readParcelable(Trip.class.getClassLoader());
		latLng = source.readParcelable(LatLng.class.getClassLoader());
		expected_mins = source.readInt();
		scheduled = source.readString();
		isIstop = source.readByte() == 1;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(headsign);
		dest.writeString(vehicle_id);
		dest.writeParcelable(route, 0);
		dest.writeParcelable(trip, 0);
		dest.writeParcelable(latLng, 0);
		dest.writeInt(expected_mins);
		dest.writeString(scheduled);
		dest.writeByte((byte) (isIstop ? 1 : 0));
	}

	public Departure(String stop_id, String headsign, String vehicle_id,
			LatLng latLng, int expected_mins, Route route, Trip trip,
			String scheduled, boolean isIstop) {
		super(stop_id);
		this.headsign = headsign;
		this.latLng = latLng;
		this.expected_mins = expected_mins;
		this.vehicle_id = vehicle_id;
		this.route = route;
		this.trip = trip;
		this.scheduled = scheduled;
		this.isIstop = isIstop;
	}

	public String getHeadsign() {
		return headsign;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public int getExpected_mins() {
		return expected_mins;
	}

	public String getVehicle_id() {
		return vehicle_id;
	}

	public Route getRoute() {
		return route;
	}

	public Trip getTrip() {
		return trip;
	}

	public String getScheduled() {
		return scheduled;
	}

	public boolean isIstop() {
		return isIstop;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Departure) {
			Departure dep = (Departure) obj;
			return this.vehicle_id.equals(dep.getVehicle_id());
		}
		return false;
	}

}

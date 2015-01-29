package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

public class Trip extends IdBasedData implements Parcelable {
	@SuppressWarnings("unused")
	private static final String TAG = Trip.class.getSimpleName();

	private String block_id;
	private String direction;
	private String route_id;
	private String service_id;
	private String shape_id;
	private String trip_headsign;

	public Trip(String block_id, String direction, String route_id, String service_id,
			String shape_id, String trip_headsign, String trip_id) {
		super(trip_id);
		this.setBlock_id(block_id);
		this.direction = direction;
		this.route_id = route_id;
		this.service_id = service_id;
		this.shape_id = shape_id;
		this.trip_headsign = trip_headsign;
	}

	public Trip(Parcel source) {
		super(source); // reads ID
		block_id = source.readString();
		direction = source.readString();
		route_id = source.readString();
		service_id = source.readString();
		shape_id = source.readString();
		trip_headsign = source.readString();
	}

	public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
		@Override
		public Trip createFromParcel(Parcel source) {
			return new Trip(source);
		}

		@Override
		public Trip[] newArray(int size) {
			return new Trip[size];
		}
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(block_id);
		dest.writeString(direction);
		dest.writeString(route_id);
		dest.writeString(service_id);
		dest.writeString(shape_id);
		dest.writeString(trip_headsign);
	}

	public String getBlock_id() {
		return block_id;
	}

	public void setBlock_id(String block_id) {
		this.block_id = block_id;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getRoute_id() {
		return route_id;
	}

	public void setRoute_id(String route_id) {
		this.route_id = route_id;
	}

	public String getService_id() {
		return service_id;
	}

	public void setService_id(String service_id) {
		this.service_id = service_id;
	}

	public String getShape_id() {
		return shape_id;
	}

	public void setShape_id(String shape_id) {
		this.shape_id = shape_id;
	}

	public String getTrip_headsign() {
		return trip_headsign;
	}

	public void setTrip_headsign(String trip_headsign) {
		this.trip_headsign = trip_headsign;
	}

}

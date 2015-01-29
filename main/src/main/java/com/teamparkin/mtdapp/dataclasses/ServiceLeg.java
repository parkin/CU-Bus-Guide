package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

import com.teamparkin.mtdapp.R;

public class ServiceLeg extends Leg implements Parcelable {
	@SuppressWarnings("unused")
	private static final String TAG = ServiceLeg.class.getSimpleName();

	public static final int IMAGE_RESOURCE = R.drawable.ic_action_bus;

	private static final String TYPE = "Service";

	private Route route;
	private Trip trip;

	public static final Parcelable.Creator<ServiceLeg> CREATOR = new Parcelable.Creator<ServiceLeg>() {
		@Override
		public ServiceLeg createFromParcel(Parcel source) {
			return new ServiceLeg(source);
		}

		@Override
		public ServiceLeg[] newArray(int size) {
			return new ServiceLeg[size];
		}
	};

	public ServiceLeg(Parcel source) {
		super(source);
		route = source.readParcelable(Route.class.getClassLoader());
		trip = source.readParcelable(Trip.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeParcelable(route, 0);
		dest.writeParcelable(trip, 0);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public ServiceLeg(LegEndpoint begin, LegEndpoint end, Route route, Trip trip) {
		super(begin, end);
		this.route = route;
		this.trip = trip;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public Route getRoute() {
		return this.route;
	}

	public Trip getTrip() {
		return this.trip;
	}

	@Override
	public int getImageResourceId() {
		return IMAGE_RESOURCE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((route == null) ? 0 : route.hashCode());
		result = prime * result + ((trip == null) ? 0 : trip.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceLeg other = (ServiceLeg) obj;
		if (route == null) {
			if (other.route != null)
				return false;
		} else if (!route.equals(other.route))
			return false;
		if (trip == null) {
			if (other.trip != null)
				return false;
		} else if (!trip.equals(other.trip))
			return false;
		return true;
	}

	@Override
	public String getMainInfo() {
		String ret = "";
		// add route short name if not null
		ret += (route.getRoute_short_name() != null && !route
				.getRoute_short_name().equals("null")) ? (route
				.getRoute_short_name()) : "";
		// add a space if needed
		ret += (ret.length() > 0) ? " " : "";
		// add route long name if not null
		ret += (route.getRoute_long_name() != null && !route
				.getRoute_long_name().equals("null")) ? (route
				.getRoute_long_name()) : "";
		// add a space if needed
		ret += (ret.length() > 0) ? " " : "";
		// add "towards " + headsign if headsign not null
		ret += (trip.getTrip_headsign() != null && !trip.getTrip_headsign()
				.equals("null")) ? ("toward " + trip.getTrip_headsign()) : "";
		return ret;
	}

	@Override
	public String getMainColor() {
		return route.getRoute_color();
	}

}

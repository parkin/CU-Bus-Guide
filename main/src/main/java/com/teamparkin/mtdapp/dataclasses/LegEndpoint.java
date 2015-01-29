package com.teamparkin.mtdapp.dataclasses;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.teamparkin.mtdapp.util.Util;

public class LegEndpoint implements Parcelable {
	@SuppressWarnings("unused")
	private static final String TAG = LegEndpoint.class.getSimpleName();

	private LatLng latLng;
	private String name;
	private String stop_id;
	private Calendar calendar;

	public static final Parcelable.Creator<LegEndpoint> CREATOR = new Parcelable.Creator<LegEndpoint>() {
		@Override
		public LegEndpoint createFromParcel(Parcel source) {
			return new LegEndpoint(source);
		}

		@Override
		public LegEndpoint[] newArray(int size) {
			return new LegEndpoint[size];
		}
	};

	public LegEndpoint(Parcel source) {
		latLng = source.readParcelable(LatLng.class.getClassLoader());
		name = source.readString();
		stop_id = source.readString();
		calendar = (Calendar) source.readSerializable();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(latLng, flags);
		dest.writeString(name);
		dest.writeString(stop_id);
		dest.writeSerializable(calendar);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public LegEndpoint(double lat, double lon, String name, String stop_id,
			String time) {
		this.latLng = new LatLng(lat, lon);
		this.name = name;
		this.stop_id = stop_id;
		this.calendar = Util.convertMTDTimeString(time);
	}

	/**
	 * Returns true if the LegEndpoint is a stop and therefore has a stop_id.
	 * 
	 * @return
	 */
	public boolean isStop() {
		return stop_id != null;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	/**
	 * Returns the name.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the stop_id if it exists. Returns null otherwise.
	 * 
	 * @return
	 */
	public String getStopId() {
		return this.stop_id;
	}

	/**
	 * Returns the calendar for this LegEndpoint.
	 * 
	 * @return
	 */
	public Calendar getCalendar() {
		return this.calendar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((calendar == null) ? 0 : calendar.hashCode());
		result = prime * result + ((latLng == null) ? 0 : latLng.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((stop_id == null) ? 0 : stop_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LegEndpoint other = (LegEndpoint) obj;
		if (calendar == null) {
			if (other.calendar != null)
				return false;
		} else if (!calendar.equals(other.calendar))
			return false;
		if (latLng == null) {
			if (other.latLng != null)
				return false;
		} else if (!latLng.equals(other.latLng))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (stop_id == null) {
			if (other.stop_id != null)
				return false;
		} else if (!stop_id.equals(other.stop_id))
			return false;
		return true;
	}

	/**
	 * Returns a formatted string of the calendar.
	 * 
	 * @return
	 */
	public String getTimeString() {
		return Util.getTimeText(calendar);
	}

}
package com.teamparkin.mtdapp.dataclasses;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.teamparkin.mtdapp.util.Util;

public class Itinerary implements Parcelable {
	@SuppressWarnings("unused")
	private static final String TAG = Itinerary.class.getSimpleName();

	private Calendar startCalendar;
	private Calendar endCalendar;
	private int travel_time;
	private List<Leg> legs = new ArrayList<Leg>();

	public static final Parcelable.Creator<Itinerary> CREATOR = new Parcelable.Creator<Itinerary>() {
		@Override
		public Itinerary createFromParcel(Parcel source) {
			return new Itinerary(source);
		}

		@Override
		public Itinerary[] newArray(int size) {
			return new Itinerary[size];
		}
	};

	public Itinerary(Parcel source) {
		startCalendar = (Calendar) source.readSerializable();
		endCalendar = (Calendar) source.readSerializable();
		travel_time = source.readInt();
		legs = new ArrayList<Leg>();
		source.readList(legs, Leg.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(startCalendar);
		dest.writeSerializable(endCalendar);
		dest.writeInt(travel_time);
		dest.writeList(legs);
	}

	/**
	 * Creates an itinerary. Make sure that start_time and end_time parameters
	 * are passed with the strings the MTD server returns. They are then parsed
	 * in to dates and times.
	 * 
	 * @param start_time
	 * @param end_time
	 * @param travel_time
	 */
	public Itinerary(String start_time, String end_time, int travel_time) {
		startCalendar = Util.convertMTDTimeString(start_time);
		endCalendar = Util.convertMTDTimeString(end_time);
		this.travel_time = travel_time;
	}

	/**
	 * Add a new leg to the itinerary.
	 * 
	 * @param leg
	 */
	public void addLeg(Leg leg) {
		legs.add(leg);
	}

	/**
	 * Returns the legs of the itinerary.
	 * 
	 * @return
	 */
	public List<Leg> getLegs() {
		return legs;
	}

	/**
	 * Returns the number of legs in the itinerary.
	 * 
	 * @return
	 */
	public int size() {
		return legs.size();
	}

	/**
	 * Returns the Leg at the specific location in the itinerary. Throws an
	 * IndexOutOfBoundsException if location < 0 || >= size().
	 * 
	 * @param location
	 * @return
	 */
	public Leg getLeg(int location) {
		return legs.get(location);
	}

	/**
	 * Returns the start time.
	 * 
	 * @return
	 */
	public String getStartTime() {
		return Util.getTimeText(startCalendar);
	}

	/**
	 * Returns the end time.
	 */
	public String getEndTime() {
		return Util.getTimeText(endCalendar);
	}

	/**
	 * Returns the travel time in minutes.
	 * 
	 * @return
	 */
	public int getTravelTime() {
		return this.travel_time;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((endCalendar == null) ? 0 : endCalendar.hashCode());
		result = prime * result + ((legs == null) ? 0 : legs.hashCode());
		result = prime * result
				+ ((startCalendar == null) ? 0 : startCalendar.hashCode());
		result = prime * result + travel_time;
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
		Itinerary other = (Itinerary) obj;
		if (endCalendar == null) {
			if (other.endCalendar != null)
				return false;
		} else if (!endCalendar.equals(other.endCalendar))
			return false;
		if (legs == null) {
			if (other.legs != null)
				return false;
		} else if (!legs.equals(other.legs))
			return false;
		if (startCalendar == null) {
			if (other.startCalendar != null)
				return false;
		} else if (!startCalendar.equals(other.startCalendar))
			return false;
		if (travel_time != other.travel_time)
			return false;
		return true;
	}

}

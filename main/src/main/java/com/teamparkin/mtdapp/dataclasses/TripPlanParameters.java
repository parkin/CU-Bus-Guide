package com.teamparkin.mtdapp.dataclasses;

import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data class for holding the parameters for a planned trip.
 * 
 * @author will
 * 
 */
public class TripPlanParameters implements Parcelable {
	@SuppressWarnings("unused")
	private static final String TAG = TripPlanParameters.class.getSimpleName();

	private static String[] walkArray;
	private static String[] minimizeArray;
	private static String[] depArrArray;

	public MyLocation origin = null;
	public MyLocation destination = null;
	private int walkArrPos = 1;
	private int minimizeArrPos = 0;
	private int depArrPos = 0;
	public Calendar calendar = Calendar.getInstance();

	private int walkArrayId;
	private int minArrayId;
	private int depArrayId;

	public TripPlanParameters(Context context, int walkArrayId, int minArrayId,
			int depArrayId) {
		this.walkArrayId = walkArrayId;
		this.minArrayId = minArrayId;
		this.depArrayId = depArrayId;
		if (walkArray == null)
			walkArray = context.getResources().getStringArray(walkArrayId);
		if (minimizeArray == null)
			minimizeArray = context.getResources().getStringArray(minArrayId);
		if (depArrArray == null)
			depArrArray = context.getResources().getStringArray(depArrayId);
	}

	/**
	 * Must override and call super to ensure things are in order!
	 * 
	 * @param source
	 */
	public TripPlanParameters(Parcel source) {
		origin = source.readParcelable(MyLocation.class.getClassLoader());
		destination = source.readParcelable(MyLocation.class.getClassLoader());

		walkArrPos = source.readInt();
		minimizeArrPos = source.readInt();
		depArrPos = source.readInt();

		walkArrayId = source.readInt();
		minArrayId = source.readInt();
		depArrayId = source.readInt();

		calendar = (Calendar) source.readSerializable();
	}

	public static final Parcelable.Creator<TripPlanParameters> CREATOR = new Parcelable.Creator<TripPlanParameters>() {
		public TripPlanParameters createFromParcel(Parcel in) {
			return new TripPlanParameters(in);
		}

		public TripPlanParameters[] newArray(int size) {
			return new TripPlanParameters[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(origin, flags);
		dest.writeParcelable(destination, flags);

		dest.writeInt(walkArrPos);
		dest.writeInt(minimizeArrPos);
		dest.writeInt(depArrPos);

		dest.writeInt(walkArrayId);
		dest.writeInt(minArrayId);
		dest.writeInt(depArrayId);

		dest.writeSerializable(calendar);
	}

	public String getWalkString() {
		if (walkArray != null && walkArrPos < walkArray.length)
			return walkArray[walkArrPos];
		return null;
	}

	public double getWalkFormattedDouble() {
		if (walkArray != null && walkArrPos < walkArray.length) {
			String walkS = walkArray[walkArrPos];
			double walking = 0.0;
			if (walkS.contains("1/4"))
				walking = 0.25;
			else if (walkS.contains("1/2"))
				walking = 0.5;
			else if (walkS.contains("3/4"))
				walking = 0.75;
			else
				walking = 1.0;
			return walking;
		}
		return 1.;
	}

	public String getMinimizeString() {
		if (minimizeArray != null && minimizeArrPos < minimizeArray.length)
			return minimizeArray[minimizeArrPos];
		return null;
	}

	public String getMinimizeFormattedString() {
		if (minimizeArray != null && minimizeArrPos < minimizeArray.length) {
			return minimizeArray[minimizeArrPos].toLowerCase(Locale.US);
		}
		return null;
	}

	public String getDepArrString() {
		if (depArrArray != null && depArrPos < depArrArray.length)
			return depArrArray[depArrPos];
		return null;
	}

	public String getDepArrFormattedString() {
		if (depArrArray != null && depArrPos < depArrArray.length) {
			return depArrArray[depArrPos].split(" ")[0].toLowerCase(Locale.US);
		}
		return null;
	}

	public int getWalkArrPos() {
		return walkArrPos;
	}

	public void setWalkArrPos(int walkArrPos) {
		this.walkArrPos = walkArrPos;
	}

	public int getMinimizeArrPos() {
		return minimizeArrPos;
	}

	public void setMinimizeArrPos(int minimizeArrPos) {
		this.minimizeArrPos = minimizeArrPos;
	}

	public int getDepArrPos() {
		return depArrPos;
	}

	public void setDepArrPos(int depArrPos) {
		this.depArrPos = depArrPos;
	}

	public boolean hasAllParameters() {
		return origin != null && destination != null && walkArray != null
				&& minimizeArray != null && depArrArray != null;
	}

	public int getWalkArrayId() {
		return walkArrayId;
	}

	public int getMinArrayId() {
		return minArrayId;
	}

	public int getDepArrayId() {
		return depArrayId;
	}

	public String[] getWalkArray() {
		return walkArray;
	}

	public String[] getMinimizeArray() {
		return minimizeArray;
	}

	public String[] getDepArrArray() {
		return depArrArray;
	}

	public String getFormattedDate() {
		// Calendar.JANUARY = 0, so need to add 1.
		String month = "" + (calendar.get(Calendar.MONTH) + 1);
		String day = "" + calendar.get(Calendar.DATE);
		if (month.length() < 2)
			month = "0" + month;
		if (day.length() < 2)
			day = "0" + day;
		String date = calendar.get(Calendar.YEAR) + "-" + month + "-" + day;
		return date;
	}

	public String getFormattedTime() {
		String hour = "" + calendar.get(Calendar.HOUR_OF_DAY);
		String minute = "" + calendar.get(Calendar.MINUTE);
		if (hour.length() < 2)
			hour = "0" + hour;
		if (minute.length() < 2)
			minute = "0" + minute;
		String time = hour + ":" + minute;
		return time;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "Origin: " + ((origin == null) ? "null" : origin.getName());
		s += " latLng: "
				+ ((origin != null && origin.latLng != null) ? origin
						.getLatLng().toString() : "null");
		s += ", destination: "
				+ ((destination == null) ? "null" : destination.getName());
		s += " latLng: "
				+ ((destination != null && destination.latLng != null) ? destination
						.getLatLng().toString() : "null");
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((calendar == null) ? 0 : calendar.hashCode());
		result = prime * result + depArrPos;
		result = prime * result + depArrayId;
		result = prime * result
				+ ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + minArrayId;
		result = prime * result + minimizeArrPos;
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + walkArrPos;
		result = prime * result + walkArrayId;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null)
			return false;
		if (!(obj instanceof TripPlanParameters))
			return false;
		TripPlanParameters other = (TripPlanParameters) obj;
		if (calendar == null) {
			if (other.calendar != null)
				return false;
		} else if (!calendar.equals(other.calendar))
			return false;
		if (depArrPos != other.depArrPos)
			return false;
		if (depArrayId != other.depArrayId)
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else {
			if (!destination.equals(other.destination))
				return false;
		}
		if (minArrayId != other.minArrayId)
			return false;
		if (minimizeArrPos != other.minimizeArrPos)
			return false;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		if (walkArrPos != other.walkArrPos)
			return false;
		if (walkArrayId != other.walkArrayId)
			return false;
		return true;
	}

}
package com.teamparkin.mtdapp.dataclasses;

import com.teamparkin.mtdapp.util.Util;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class Leg implements Parcelable {
	@SuppressWarnings("unused")
	private static final String TAG = Leg.class.getSimpleName();

	protected LegEndpoint begin;
	protected LegEndpoint end;

	public Leg(LegEndpoint begin, LegEndpoint end) {
		this.begin = begin;
		this.end = end;
	}

	public static final Parcelable.Creator<Leg> CREATOR = new Parcelable.Creator<Leg>() {
		@Override
		public Leg createFromParcel(Parcel source) {
			return new Leg(source) {

				@Override
				public String getType() {
					return null;
				}

				@Override
				public int getImageResourceId() {
					return 0;
				}

				@Override
				public String getMainInfo() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getMainColor() {
					// TODO Auto-generated method stub
					return null;
				}

			};
		}

		@Override
		public Leg[] newArray(int size) {
			return new Leg[size];
		}
	};

	public Leg(Parcel source) {
		begin = source.readParcelable(LegEndpoint.class.getClassLoader());
		end = source.readParcelable(LegEndpoint.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(begin, 0);
		dest.writeParcelable(end, 0);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public abstract String getMainInfo();

	/**
	 * Get minor info activity_about this leg. By default it returns
	 * 
	 * "(x min)" where x is the duration of the leg.
	 * 
	 * @return
	 */
	public String getMinorInfo() {
		return "(" + getDuration() + " min)";
	}

	/**
	 * Gets the duration in minutes of the leg.
	 * 
	 * @return
	 */
	public long getDuration() {
		return Util.getMinutesBetween(begin.getCalendar(), end.getCalendar());
	}

	/**
	 * returns the TYPE of the leg. ("Walk" or "Services")
	 */
	public abstract String getType();

	/**
	 * return the beginning of this leg.
	 * 
	 * @return
	 */
	public LegEndpoint getBegin() {
		return begin;
	}

	/**
	 * Returns the end of the leg.
	 * 
	 * @return
	 */
	public LegEndpoint getEnd() {
		return this.end;
	}

	public abstract int getImageResourceId();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((begin == null) ? 0 : begin.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
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
		Leg other = (Leg) obj;
		if (begin == null) {
			if (other.begin != null)
				return false;
		} else if (!begin.equals(other.begin))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		return true;
	}

	/**
	 * Return a color in format "xxxxxx"
	 * 
	 * @return
	 */
	public abstract String getMainColor();

}

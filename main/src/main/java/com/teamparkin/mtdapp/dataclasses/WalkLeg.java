package com.teamparkin.mtdapp.dataclasses;

import java.text.DecimalFormat;

import android.os.Parcel;
import android.os.Parcelable;

import com.teamparkin.mtdapp.R;

public class WalkLeg extends Leg implements Parcelable {
	@SuppressWarnings("unused")
	private static final String TAG = WalkLeg.class.getSimpleName();

	public static final int IMAGE_RESOURCE = R.drawable.ic_action_walking;

	private static final String TYPE = "Walk";

	private String direction;
	private double distance;

	public static final Parcelable.Creator<WalkLeg> CREATOR = new Parcelable.Creator<WalkLeg>() {
		@Override
		public WalkLeg createFromParcel(Parcel source) {
			return new WalkLeg(source);
		}

		@Override
		public WalkLeg[] newArray(int size) {
			return new WalkLeg[size];
		}
	};

	public WalkLeg(Parcel source) {
		super(source);
		direction = source.readString();
		distance = source.readDouble();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(direction);
		dest.writeDouble(distance);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public WalkLeg(LegEndpoint begin, LegEndpoint end, String direction,
			double distance) {
		super(begin, end);
		this.direction = direction;
		this.distance = distance;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * Returns the direction of the walking leg. (North, South, East, or West)
	 * 
	 * @return
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * Returns the distance of the walking leg in miles.
	 * 
	 * @return
	 */
	public double getDistance() {
		return this.distance;
	}

	@Override
	public int getImageResourceId() {
		return IMAGE_RESOURCE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((direction == null) ? 0 : direction.hashCode());
		long temp;
		temp = Double.doubleToLongBits(distance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		WalkLeg other = (WalkLeg) obj;
		if (direction == null) {
			if (other.direction != null)
				return false;
		} else if (!direction.equals(other.direction))
			return false;
		if (Double.doubleToLongBits(distance) != Double
				.doubleToLongBits(other.distance))
			return false;
		return true;
	}

	@Override
	public String getMainInfo() {
		// make it "Walk East" for example.
		return "Walk"
				+ ((direction != null && !direction.equals("null")) ? (" " + direction)
						: "");
	}

	@Override
	public String getMinorInfo() {
		String s = super.getMinorInfo();
		DecimalFormat df = new DecimalFormat("#.##");
		return "" + df.format(distance) + " miles " + s;
	}

	@Override
	public String getMainColor() {
		return "0e0e0e";
	}

}

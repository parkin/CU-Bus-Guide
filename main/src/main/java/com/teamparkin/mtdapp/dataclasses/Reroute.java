package com.teamparkin.mtdapp.dataclasses;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Reroute implements Parcelable {

	private String startDate;
	private String endDate;
	private String message;
	private String description;
	private List<Route> affectedRoutes;

	public Reroute(String startDate, String endDate, String message,
			String description, List<Route> affectedRoutes) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.message = message;
		this.description = description;
		this.affectedRoutes = affectedRoutes;
	}

	public Reroute(Parcel source) {
		this.startDate = source.readString();
		this.endDate = source.readString();
		this.message = source.readString();
		this.description = source.readString();
		source.readList(affectedRoutes,
				Route.class.getClassLoader());
	}

	public static final Parcelable.Creator<Reroute> CREATOR = new Parcelable.Creator<Reroute>() {
		@Override
		public Reroute createFromParcel(Parcel source) {
			return new Reroute(source);
		}

		@Override
		public Reroute[] newArray(int size) {
			return new Reroute[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(startDate);
		dest.writeString(endDate);
		dest.writeString(message);
		dest.writeString(description);
		dest.writeList(affectedRoutes);
	}

}

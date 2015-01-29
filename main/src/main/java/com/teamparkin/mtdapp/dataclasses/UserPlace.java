package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class UserPlace extends Place {

	private String mComment;

	public UserPlace(String name, String id, LatLng latLng, String comment,
			long rowId) {
		super(name, id, latLng, rowId);
		mComment = comment;
	}

	public UserPlace(String name, String id, double lat, double lon,
			String comment, long rowId) {
		this(name, id, new LatLng(lat, lon), comment, rowId);
	}

	public UserPlace(Parcel source) {
		super(source);
		this.mComment = source.readString();
	}

	public static final Parcelable.Creator<UserPlace> CREATOR = new Parcelable.Creator<UserPlace>() {
		@Override
		public UserPlace createFromParcel(Parcel source) {
			return new UserPlace(source);
		}

		@Override
		public UserPlace[] newArray(int size) {
			return new UserPlace[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mComment);
	}

	public String getComment() {
		return mComment;
	}

	@Override
	public String getSnippet() {
		return mComment;
	}

}

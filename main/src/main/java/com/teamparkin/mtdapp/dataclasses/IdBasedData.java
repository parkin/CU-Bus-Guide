package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class IdBasedData implements Parcelable {

	protected String id;

	protected IdBasedData(String id) {
		this.id = id;
	}

	/**
	 * Must override and call super to ensure things are in order!
	 * 
	 * @param source
	 */
	public IdBasedData(Parcel source) {
		this.id = source.readString();
	}

	/**
	 * Must override and call super to ensure things are in order!
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
	}

	public String getId() {
		return id;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IdBasedData))
			return false;
		IdBasedData other = (IdBasedData) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}

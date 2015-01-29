package com.teamparkin.mtdapp.dataclasses;

import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.teamparkin.mtdapp.R;

public abstract class MyLocation extends IdBasedData implements Parcelable {

	private static final int ICON_ID = R.drawable.powered_by_google_on_white;
	private static final int MAP_ICON_ID = R.drawable.mylocation_pin;
	private static final float[] ANCHOR = new float[] { 0.5f, 0.5f };

	/**
	 * The rowId in the database.
	 */
	protected long rowId;

	protected LatLng latLng;

	protected String name;

	protected MyLocation(String id, String name, LatLng latLng, long rowId) {
		super(id);
		this.name = name;
		this.latLng = latLng;
		this.rowId = rowId;
	}

	/**
	 * Must override and call super to ensure things are in order!
	 * 
	 * @param source
	 */
	protected MyLocation(Parcel source) {
		super(source);
		this.name = source.readString();
		this.latLng = source.readParcelable(LatLng.class.getClassLoader());
		this.rowId = source.readLong();
	}

	/**
	 * Must override and call super to ensure things are in order!
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(name);
		dest.writeParcelable(latLng, flags);
		dest.writeLong(rowId);
	}

	/**
	 * Params must have a String "id", a String "name", a LatLng "latlng"
	 * 
	 * @param params
	 */
	protected MyLocation(Map<String, Object> params) {
		this((String) params.get("id"), (String) params.get("name"),
				(LatLng) params.get("latlng"), (Long) params.get("rowId"));
	}

	public long getRowId() {
		return rowId;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public String getName() {
		return name;
	}

	/**
	 * Returns a little extra info (eg stop Id) for subtext to a name.
	 * 
	 * @return
	 */
	public String getSnippet() {
		return this.id;
	}

	public int getMapIconId(boolean favorite) {
		return MAP_ICON_ID;
	}

	public float[] getAnchorPosition(boolean favorite) {
		return ANCHOR;
	}

	public int getImageId() {
		return ICON_ID;
	}

	public float getMarkerHue() {
		return BitmapDescriptorFactory.HUE_RED;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((latLng == null) ? 0 : latLng.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		MyLocation other = (MyLocation) obj;
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
		return true;
	}

}

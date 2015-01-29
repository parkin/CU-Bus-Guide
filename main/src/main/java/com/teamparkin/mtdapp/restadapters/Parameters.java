package com.teamparkin.mtdapp.restadapters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

class Parameters implements Parcelable {
	Map<String, String> map = new HashMap<String, String>();

	public static final Parcelable.Creator<Parameters> CREATOR = new Parcelable.Creator<Parameters>() {
		@Override
		public Parameters createFromParcel(Parcel source) {
			return new Parameters(source);
		}

		@Override
		public Parameters[] newArray(int size) {
			return new Parameters[size];
		}
	};

	public Parameters(Parcel source) {
		this();
		ArrayList<String> keys = new ArrayList<String>();
		source.readList(keys, String.class.getClassLoader());

		ArrayList<String> values = new ArrayList<String>();
		source.readList(values, String.class.getClassLoader());

		for (int i = 0; i < keys.size() && i < values.size(); i++) {
			map.put(keys.get(i), values.get(i));
		}
	}

	public Parameters() {
		map = new HashMap<String, String>();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		ArrayList<String> keys = new ArrayList<String>(map.keySet());
		ArrayList<String> values = new ArrayList<String>();
		for (String key : keys)
			values.add(map.get(key));
		dest.writeList(keys);
		dest.writeList(values);
	}

	/**
	 * Adds parameter. Passing null in to value causes value to be set to "".
	 * 
	 * @param parameter
	 * @param value
	 */
	protected void put(String parameter, String value) {
		if (parameter != null) {
			if (value == null)
				map.put(parameter, "");
			else
				map.put(parameter, value);
		}
	}

	protected void put(String parameter, double value) {
		DecimalFormat formatter = new DecimalFormat("###.########");
		put(parameter, "" + formatter.format(value));
	}

	protected void put(String parameter, int value) {
		put(parameter, "" + value);
	}

	protected void put(String parameter, boolean b) {
		put(parameter, "" + b);
	}

	public String get(String string) {
		return map.get(string);
	}

	/**
	 * returns a the keyset sorted in alphabetical order.
	 * 
	 * @return
	 */
	public List<String> keySet() {
		List<String> sorted = new ArrayList<String>(map.keySet());
		java.util.Collections.sort(sorted);
		return sorted;
	}

	public Map<String, String> getMap() {
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
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
		Parameters other = (Parameters) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}
}
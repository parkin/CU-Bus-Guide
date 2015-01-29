package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

public class Route extends IdBasedData implements Parcelable {
	@SuppressWarnings("unused")
	private static final String TAG = Route.class.getSimpleName();

	public static final String DEFUALT_TEXT_COLOR = "ffffff";
	public static final String DEFAULT_ROUTE_COLOR = "000000";

	private String route_color;
	private String route_long_name;
	private String route_short_name;
	private String route_text_color;

	public Route(String route_color, String route_id, String route_long_name,
			String route_short_name, String route_text_color) {
		super(route_id);
		this.route_color = route_color;
		this.route_long_name = route_long_name;
		this.route_short_name = route_short_name;
		this.route_text_color = route_text_color;
	}

	public Route(Parcel source) {
		super(source); // reads id
		this.route_color = source.readString();
		this.route_long_name = source.readString();
		this.route_short_name = source.readString();
		this.route_text_color = source.readString();
	}

	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
		@Override
		public Route createFromParcel(Parcel source) {
			return new Route(source);
		}

		@Override
		public Route[] newArray(int size) {
			return new Route[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(route_color);
		dest.writeString(route_long_name);
		dest.writeString(route_short_name);
		dest.writeString(route_text_color);
	}

	/**
	 * Returns the route_color, or "000000" if route_color is null.
	 * 
	 * @return
	 */
	public String getRoute_color() {
		return (route_color != null && !route_color.equals("null")) ? route_color
				: DEFAULT_ROUTE_COLOR;
	}

	public void setRoute_color(String route_color) {
		this.route_color = route_color;
	}

	public String getRoute_long_name() {
		return route_long_name;
	}

	public void setRoute_long_name(String route_long_name) {
		this.route_long_name = route_long_name;
	}

	public String getRoute_short_name() {
		return route_short_name;
	}

	public void setRoute_short_name(String route_short_name) {
		this.route_short_name = route_short_name;
	}

	/**
	 * Returns the route_text_color, or "ffffff" if route_text_color is null.
	 * 
	 * @return
	 */
	public String getRoute_text_color() {
		return (route_text_color != null && !route_text_color.equals("null")) ? route_text_color
				: DEFUALT_TEXT_COLOR;
	}

	public void setRoute_text_color(String route_text_color) {
		this.route_text_color = route_text_color;
	}
}

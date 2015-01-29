package com.teamparkin.mtdapp.dataclasses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class GooglePlace extends Place {

	private String events;
	private String formattedAddress;
	private String icon;
	private String openingHours;
	private String photos;
	private int priceLevel;
	private double rating;
	private String reference;
	private String types;
	private String vicinity;

	public GooglePlace(String name, String id, LatLng latLng, long rowId,
			String events, String formattedAddress, String icon,
			String openingHours, String photos, int priceLevel, double rating,
			String reference, String types, String vicinity) {
		super(name, id, latLng, rowId);
		this.events = events;
		this.formattedAddress = formattedAddress;
		this.icon = icon;
		this.openingHours = openingHours;
		this.photos = photos;
		this.priceLevel = priceLevel;
		this.rating = rating;
		this.reference = reference;
		this.types = types;
		this.vicinity = vicinity;
	}

	public GooglePlace(String name, String id, double lat, double lon,
			long rowId, String events, String formattedAddress, String icon,
			String openingHours, String photos, int priceLevel, double rating,
			String reference, String types, String vicinity) {
		this(name, id, new LatLng(lat, lon), rowId, events, formattedAddress,
				icon, openingHours, photos, priceLevel, rating, reference,
				types, vicinity);
	}

	public GooglePlace(Parcel source) {
		super(source);
		events = source.readString();
		formattedAddress = source.readString();
		icon = source.readString();
		openingHours = source.readString();
		photos = source.readString();
		priceLevel = source.readInt();
		rating = source.readDouble();
		reference = source.readString();
		types = source.readString();
		vicinity = source.readString();
	}

	public GooglePlace(Builder builder) {
		this(builder.name, builder.id, builder.latLng, builder.rowId,
				builder.events, builder.formattedAddress, builder.icon,
				builder.openingHours, builder.photos, builder.priceLevel,
				builder.rating, builder.reference, builder.types,
				builder.vicinity);
	}

	public static final Parcelable.Creator<GooglePlace> CREATOR = new Parcelable.Creator<GooglePlace>() {
		@Override
		public GooglePlace createFromParcel(Parcel source) {
			return new GooglePlace(source);
		}

		@Override
		public GooglePlace[] newArray(int size) {
			return new GooglePlace[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(events);
		dest.writeString(formattedAddress);
		dest.writeString(icon);
		dest.writeString(openingHours);
		dest.writeString(photos);
		dest.writeInt(priceLevel);
		dest.writeDouble(rating);
		dest.writeString(reference);
		dest.writeString(types);
		dest.writeString(vicinity);
	}

	public String getEvents() {
		return events;
	}

	public String getFormattedAddress() {
		return formattedAddress;
	}

	public String getIcon() {
		return icon;
	}

	public String getOpeningHours() {
		return openingHours;
	}

	public String getPhotos() {
		return photos;
	}

	public int getPriceLevel() {
		return priceLevel;
	}

	public double getRating() {
		return rating;
	}

	public String getReference() {
		return reference;
	}

	public String getTypes() {
		return types;
	}

	public String getVicinity() {
		return vicinity;
	}

	@Override
	public String getSnippet() {
		return vicinity;
	}

	public static class Builder {
		// My location stuff
		private String name;
		private String id;
		private LatLng latLng;
		private long rowId = -1;

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setLatLng(LatLng latLng) {
			this.latLng = latLng;
			return this;
		}

		public Builder setRowId(long rowId) {
			this.rowId = rowId;
			return this;
		}

		private String events;
		private String formattedAddress;
		private String icon;
		private String openingHours;
		private String photos;
		private int priceLevel;
		private double rating;
		private String reference;
		private String types;
		private String vicinity;

		public Builder setEvents(String eventsJson) {
			this.events = eventsJson;
			return this;
		}

		public Builder setFormattedAddress(String formattedAddress) {
			this.formattedAddress = formattedAddress;
			return this;
		}

		public Builder setIcon(String icon) {
			this.icon = icon;
			return this;
		}

		public Builder setOpeningHours(String openingHours) {
			this.openingHours = openingHours;
			return this;
		}

		public Builder setPhotos(String photos) {
			this.photos = photos;
			return this;
		}

		public Builder setPriceLevel(int priceLevel) {
			this.priceLevel = priceLevel;
			return this;
		}

		public Builder setRating(double rating) {
			this.rating = rating;
			return this;
		}

		public Builder setReference(String reference) {
			this.reference = reference;
			return this;
		}

		public Builder setTypes(String types) {
			this.types = types;
			return this;
		}

		public Builder setVicinity(String vicinity) {
			this.vicinity = vicinity;
			return this;
		}

		public GooglePlace build() {
			if (this.name == null || this.id == null || this.latLng == null
					|| this.rowId == -1)
				throw new IllegalStateException(
						"cannot build GooglePlace without name, id, and latLng set");
			return new GooglePlace(this);
		}
	}

}

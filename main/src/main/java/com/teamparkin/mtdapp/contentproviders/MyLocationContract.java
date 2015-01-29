package com.teamparkin.mtdapp.contentproviders;

import com.teamparkin.mtdapp.R;

import android.graphics.Color;
import android.net.Uri;

public final class MyLocationContract {

	private MyLocationContract() {
	}

	public static final String AUTHORITY = "com.teamparkin.mtdapp.contentprovider.mylocation";

	static final String BASE_PATH_FAVORITES = "favorites";
	static final String BASE_PATH_LOCATIONS = "mylocations";
	static final String BASE_PATH_ABSTRACT_STOPS = BASE_PATH_LOCATIONS
			+ "/abstractstops";
	static final String BASE_PATH_STOPS = BASE_PATH_ABSTRACT_STOPS + "/stops";
	static final String BASE_PATH_STOPPOINTS = BASE_PATH_ABSTRACT_STOPS
			+ "/stoppoints";
	static final String BASE_PATH_PLACES = BASE_PATH_LOCATIONS + "/places";
	static final String BASE_PATH_USER_PLACES = BASE_PATH_PLACES
			+ "/user_places";
	static final String BASE_PATH_GOOGLE_PLACES = BASE_PATH_PLACES
			+ "/google_places";
    static final String BASE_PATH_GOOGLE_PLACES_COUNT = BASE_PATH_GOOGLE_PLACES + "/count";

	/**
	 * MyLocation type codes for the types contained in the mylocation table.
	 * 
	 * @author will
	 * 
	 */
	public interface LocationTypeCode {

		public static final int STOP = 1;
		public static final int STOPPOINT = 2;
		public static final int GOOGLE_PLACE = 3;
		public static final int USER_PLACE = 4;

	}

	/**
	 * Returns the resource id of the drawable designated for the given type.
	 * 
	 * @param type
	 * @return
	 */
	public static int getLocationTypeDrawableResId(int type) {
		switch (type) {
		case LocationTypeCode.STOP:
		case LocationTypeCode.STOPPOINT:
			return R.drawable.bus;
		case LocationTypeCode.GOOGLE_PLACE:
		case LocationTypeCode.USER_PLACE:
			return R.drawable.flag;
		}
		return -1;
	}

	/**
	 * Returns the resource id of the drawable designated for the given type.
	 * 
	 * @param type
	 * @return
	 */
	public static int getLocationTypeBackgroundColor(int type) {
		switch (type) {
		case LocationTypeCode.STOP:
		case LocationTypeCode.STOPPOINT:
			return Color.parseColor("#0099CC");
		case LocationTypeCode.GOOGLE_PLACE:
		case LocationTypeCode.USER_PLACE:
			return Color.parseColor("#FF8800");
		}
		return -1;
	}

	/**
	 * Returns the resource id of the map icon drawable designated for the given
	 * type.
	 * 
	 * @param type
	 * @param favorite
	 * @return
	 */
	public static int getLocationTypeMapDrawableResId(int type, boolean favorite) {
		switch (type) {
		case LocationTypeCode.STOP:
		case LocationTypeCode.STOPPOINT:
			return favorite ? R.drawable.ic_circle_blue_star
					: R.drawable.ic_circle_blue;
		case LocationTypeCode.GOOGLE_PLACE:
		case LocationTypeCode.USER_PLACE:
			return favorite ? R.drawable.ic_circle_orange_star
					: R.drawable.ic_circle_orange;
		}
		return -1;
	}

	protected interface FavoritesColumns {
		/**
		 * The row id of the location in the mylocations table.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String _ID = "_id";
		/**
		 * The string id of the location.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String ID = "location_id";
		/**
		 * Whether it is a favorite or not. 1=favorite.
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String VALUE = "value";
	}

	protected interface BaseColumns {
		/**
		 * Row id of the data.
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String DATA_ID = "_id";
	}

	/**
	 * @see MyLocation
	 * @author will
	 * 
	 */
	protected interface MyLocationColumns extends BaseColumns {

		/**
		 * The string id of the location.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String ID = "location_id";

		/**
		 * The latitude of the location.
		 * <P>
		 * Type: REAL
		 * </P>
		 */
		public static final String LAT = "lat";

		/**
		 * The longitude of the location.
		 * <P>
		 * Type: REAL
		 * </P>
		 */
		public static final String LON = "lon";

		/**
		 * Name of the location.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String NAME = "name";

		/**
		 * Whether the location is a favorite (1 = favorite).
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String FAVORITE = "favorite";

		/**
		 * Text snippet activity_about the location. Note this column cannot be used in a
		 * where clause.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String TYPE = "type";

		public static final String SNIPPET = "snippet";

		/**
		 * Timestamp of the last time the location was updated.
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String TIMESTAMP = "timestamp";
	}

	/**
	 * @see AbstractStops
	 * @author will
	 * 
	 */
	protected interface AbstractStopColumns extends BaseColumns {
		/**
		 * The code of the stop.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String CODE = "code";
	}

	protected interface StopColumns extends BaseColumns {
	}

	protected interface StopPointColumns extends BaseColumns {
	}

	protected interface PlaceColumns extends BaseColumns {
	}

	protected interface GooglePlaceColumns extends BaseColumns {
		/**
		 * Contains text of a JSON array of the events.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String EVENTS = "events";

		/**
		 * Formatted address of the place. Not guaranteed to be non-null.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String FORMATTED_ADDRESS = "formatted_address";

		/**
		 * The url to a suggested icon.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String ICON = "icon";

		/**
		 * JSON array containing opening_hours info.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String OPENING_HOURS = "opening_hours";
		/**
		 * Text. JSON array.
		 */
		public static final String PHOTOS = "photos";
		/**
		 * 0 — Free 1 — Inexpensive 2 — Moderate 3 — Expensive 4 — Very
		 * Expensive
		 */
		public static final String PRICE_LEVEL = "price_level";
		public static final String RATING = "rating";
		public static final String REFERENCE = "reference";
		public static final String TYPES = "types";
		public static final String VICINITY = "vicinity";
	}

	protected interface UserPlaceColumns extends BaseColumns {
		/**
		 * A comment on the place made by the user.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String COMMENT = "comment";
	}

	/**
	 * This is basically an outer join of the mylocation table and the
	 * favorite_locations table.
	 * 
	 * @author will
	 * 
	 */
	public static final class MyLocation implements MyLocationColumns {

		/**
		 * No constructor since this is a utility class.
		 */
		private MyLocation() {
		}

		/**
		 * The content:// style Uri for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + BASE_PATH_LOCATIONS);
	}

	public static final class AbstractStops implements MyLocationColumns,
			AbstractStopColumns {

		/**
		 * No constructor since this is a utility class.
		 */
		private AbstractStops() {
		}

		/**
		 * The content:// style Uri for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + BASE_PATH_ABSTRACT_STOPS);
	}

	public static final class Stops implements MyLocationColumns,
			AbstractStopColumns, StopColumns {

		/**
		 * No constructor since this is a utility class.
		 */
		private Stops() {
		}

		/**
		 * The content:// style Uri for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + BASE_PATH_STOPS);
	}

	public static final class StopPoints implements MyLocationColumns,
			AbstractStopColumns, StopPointColumns {

		/**
		 * No constructor since this is a utility class.
		 */
		private StopPoints() {
		}

		/**
		 * The content:// style Uri for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + BASE_PATH_STOPPOINTS);
	}

	public static final class Places implements MyLocationColumns, PlaceColumns {

		/**
		 * No constructor since this is a utility class.
		 */
		private Places() {
		}

		/**
		 * The content:// style Uri for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + BASE_PATH_PLACES);

	}

	public static final class UserPlaces implements MyLocationColumns,
			PlaceColumns, UserPlaceColumns {

		/**
		 * No constructor since this is a utility class.
		 */
		private UserPlaces() {
		}

		/**
		 * The content:// style Uri for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + BASE_PATH_USER_PLACES);
	}

	/**
	 * This table is an outer join of the mylocation, places, google_places, and
	 * google_places_details tables.
	 * 
	 * @author will
	 * 
	 */
	public static final class GooglePlaces implements MyLocationColumns,
			PlaceColumns, GooglePlaceColumns {

		/**
		 * No constructor since this is a utility class.
		 */
		private GooglePlaces() {
		}

		/**
		 * The GooglePlace might have an associated row in the
		 * google_places_details table. It also might not.
		 * 
		 * @author will
		 * 
		 */
		public interface DetailsColumns extends BaseColumns {
			/**
			 * JSON array containing address_components.
			 * <P>
			 * Type: TEXT
			 * </P>
			 */
			public static final String ADDRESS_COMPONENTS = "address_components";
			public static final String FORMATTED_PHONE_NUMBER = "formatted_phone_number";
			public static final String INTERNATIONAL_PHONE_NUMBER = "international_phone_number";
			/**
			 * JSON
			 */
			public static final String REVIEWS = "reviews";
			/**
			 * JSON
			 */
			public static final String TYPES = "types";
			public static final String UTD_OFFSET = "utc_offset";
			public static final String WEBSITE = "website";
		}

		/**
		 * The content:// style Uri for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + BASE_PATH_GOOGLE_PLACES);

        /**
         * Ignores the 'projection' clause you pass in, does 'Count(*)' instead to just count
         * whatever your want to select.
         *
         * int count = ((Cursor) c).getLong(0);
         */
        public static final Uri COUNT_URI = Uri.parse("content://" + AUTHORITY + "/"
                + BASE_PATH_GOOGLE_PLACES_COUNT);

		// TODO add a /# to look up the specific id
		// TODO add a /#/details to just get the specific details info
	}

	public static final class Favorites implements FavoritesColumns {
		/**
		 * The content:// style Uri for this table.
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + BASE_PATH_FAVORITES);
	}

}

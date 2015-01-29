package com.teamparkin.mtdapp.restadapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.teamparkin.mtdapp.MTDAppActivity;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract.StopPoints;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract.Stops;
import com.teamparkin.mtdapp.databases.DatabaseAdapter;
import com.teamparkin.mtdapp.databases.RoutesDatabase;
import com.teamparkin.mtdapp.dataclasses.Departure;
import com.teamparkin.mtdapp.dataclasses.Itinerary;
import com.teamparkin.mtdapp.dataclasses.LegEndpoint;
import com.teamparkin.mtdapp.dataclasses.Reroute;
import com.teamparkin.mtdapp.dataclasses.Route;
import com.teamparkin.mtdapp.dataclasses.RouteShape;
import com.teamparkin.mtdapp.dataclasses.RouteShapePoint;
import com.teamparkin.mtdapp.dataclasses.ServiceLeg;
import com.teamparkin.mtdapp.dataclasses.Trip;
import com.teamparkin.mtdapp.dataclasses.TripPlanParameters;
import com.teamparkin.mtdapp.dataclasses.Vehicle;
import com.teamparkin.mtdapp.dataclasses.WalkLeg;
import com.teamparkin.mtdapp.util.Util;

public class MTDAPIAdapter extends RESTJSONAdapter {
	private static final String TAG = MTDAPIAdapter.class.getSimpleName();

	private static final String MTD_SITE = "https://developer.cumtd.com/api/";
	private static final String MTD_API_VERSION = "v2.2";
	private static String MTD_API_KEY = "";

	private static final String GET_DEPARTURES_BY_STOP = "GetDeparturesByStop";
	private static final String GET_STOPS_BY_LAT_LON = "GetStopsByLatLon";
	private static final String GET_STOPS_BY_SEARCH = "GetStopsBySearch";
	private static final String GET_SHAPE = "GetShape";
	private static final String GET_SHAPE_BETWEEN_STOPS = "GetShapeBetweenStops";
	private static final String GET_TRIPS_BY_ROUTE = "GetTripsByRoute";
	private static final String GET_PLANNED_TRIPS_BY_LAT_LON = "GetPlannedTripsByLatLon";
	private static final String GET_PLANNED_TRIPS_BY_STOPS = "GetPlannedTripsByStops";
	private static final String GET_STOPS = "GetStops";
	private static final String GET_ROUTES = "GetRoutes";
	private static final String GET_LAST_FEED_UPDATE = "GetLastFeedUpdate";
	private static final String GET_VEHICLE = "GetVehicle";
	private static final String GET_VEHICLES = "GetVehicles";
	private static final String GET_VEHICLES_BY_ROUTE = "GetVehiclesByRoute";
	private static final String GET_REROUTES = "GetReroutes";
	private static final String GET_REROUTES_BY_ROUTE = "GetReroutesByRoute";

	private static final int STOPS_NOTIFICATION_ID = 1;
	private static final int ROUTE_NOTIFICATION_ID = 2;

	private DatabaseAdapter mDatabase;

	private static MTDAPIAdapter mSingleton;

	private MTDAPIAdapter(Context context) {
		mDatabase = DatabaseAdapter.getInstance(context);
		this.mContext = context;
	}

	public static synchronized MTDAPIAdapter getInstance(Context context) {
		if (mSingleton == null) {
			MTD_API_KEY = context.getResources()
					.getString(R.string.api_key_mtd);
			mSingleton = new MTDAPIAdapter(context);
		}
		return mSingleton;
	}

	@Override
	protected String setupCommand(String command) {
		String urlQuery = "" + MTD_SITE + MTD_API_VERSION + "/json/" + command;
		return urlQuery;
	}

	public String getLastFeedUpdate() {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		JSONObject jobj = httpGet(GET_LAST_FEED_UPDATE, parameters);
		if (hasError())
			return null;
		checkStatusCode(jobj);
		if (hasError())
			return null;
		String lastUpdateTime = null;
		try {
			lastUpdateTime = jobj.getString("last_updated");
		} catch (JSONException e) {
			error = new RESTJSONAdapterError(e);
			e.printStackTrace();
			return null;
		}
		return lastUpdateTime;
	}

	public String getStopsByLatLonUrl(double lat, double lon) {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		parameters.put("lat", lat);
		parameters.put("lon", lon);

		return getHttpGetUrl(GET_STOPS_BY_LAT_LON, parameters);
	}

	private void checkStatusCode(JSONObject jobj) {
		JSONObject jo;
		try {
			jo = jobj.getJSONObject("status");
			int code = jo.getInt("code");
			if (code >= 300) {
				String msg = jo.getString("msg");
				error = new RESTJSONAdapterError(msg);
				Log.w(TAG, msg);
			}
		} catch (JSONException e) {
			error = new RESTJSONAdapterError(e);
		}
	}

	/**
	 * Returns an ordered map of the StopPoints ids at a stop to a list of the
	 * Departures from that StopPoint
	 * 
	 * @param stopId
	 * @return
	 */
	public Map<String, ArrayList<Departure>> getDeparturesByStop(String stopId) {
		String url = getDeparturesByStopUrl(stopId);

		JSONObject jobj = httpGet(url);
		if (hasError())
			return null;
		checkStatusCode(jobj);
		if (hasError())
			return null;
		return parseDeparturesByStopFromJSON(jobj);

	}

	/**
	 * Parses the json object to a map of departure list by stop point id.
	 * 
	 * @param jobj
	 * @return
	 */
	public LinkedHashMap<String, ArrayList<Departure>> parseDeparturesByStopFromJSON(
			JSONObject jobj) {
		JSONArray jarray;
		List<Departure> departures = new ArrayList<Departure>();
		try {
			jarray = jobj.getJSONArray("departures");

			for (int i = 0; i < jarray.length(); i++) {
				JSONObject ob = jarray.getJSONObject(i);
				JSONObject locarray = ob.getJSONObject("location");
				JSONObject jroute = ob.getJSONObject("route");
				JSONObject jtrip = null;
				// sometimes there are no trips included.... so we must check
				if (ob.has("trip")) {
					jtrip = ob.getJSONObject("trip");
				} else {
					Log.w(TAG, "No value for trip.");
				}

				Route route = new Route(jroute.getString("route_color"),
						jroute.getString("route_id"),
						jroute.getString("route_long_name"),
						jroute.getString("route_short_name"),
						jroute.getString("route_text_color"));
				Trip trip = null;
				if (jtrip != null)
					trip = new Trip(jtrip.getString("block_id"),
							jtrip.getString("direction"),
							jtrip.getString("route_id"),
							jtrip.getString("service_id"),
							jtrip.getString("shape_id"),
							jtrip.getString("trip_headsign"),
							jtrip.getString("trip_id"));

				LatLng latLng = new LatLng(locarray.getDouble("lat"),
						locarray.getDouble("lon"));

				Departure departure = new Departure(ob.getString("stop_id"),
						ob.getString("headsign"), ob.getString("vehicle_id"),
						latLng, ob.getInt("expected_mins"), route, trip,
						ob.getString("scheduled"), ob.getBoolean("is_istop"));

				departures.add(departure);

			}
		} catch (JSONException e) {
			Log.w(TAG, "getDeparturesByStop " + e.getMessage());
			error = new RESTJSONAdapterError(e);
			return null;
		}

		Collections.sort(departures, new DepartureComparator());
		LinkedHashMap<String, ArrayList<Departure>> map = new LinkedHashMap<String, ArrayList<Departure>>();
		Log.i(TAG, "departures size: " + departures.size());
		if (departures.size() > 0) {
			List<Departure> tempDepartures = new ArrayList<Departure>();
			String spString = "";
			for (Departure departure : departures) {
				// Log.d(TAG, "sp: " + departure.getId() + " hs: " +
				// departure.getHeadsign());
				if (!departure.getId().equals(spString)) {
					// we have finished filling departures for a stop point
					if (!spString.equals("")) {
						map.put(spString, new ArrayList<Departure>(
								tempDepartures));
					}
					spString = departure.getId();
					tempDepartures = new ArrayList<Departure>();
				}
				tempDepartures.add(departure);
			}
			map.put(spString, new ArrayList<Departure>(tempDepartures));
		}

		return map;
	}

	/**
	 * Returns the url for getting deparutes by stop for stopId.
	 * 
	 * @param stopId
	 * @return
	 */
	public String getDeparturesByStopUrl(String stopId) {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		parameters.put("stop_id", stopId);

		String url = getHttpGetUrl(GET_DEPARTURES_BY_STOP, parameters);
		return url;
	}

	private class DepartureComparator implements Comparator<Departure> {

		@Override
		public int compare(Departure lhs, Departure rhs) {
			if (lhs.getId().compareTo(rhs.getId()) < 0) {
				return -1;
			} else if (lhs.getId().compareTo(rhs.getId()) > 0) {
				return 1;
			} else {
				return (Integer.valueOf(lhs.getExpected_mins()))
						.compareTo(Integer.valueOf(rhs.getExpected_mins()));
			}
		}

	}

	public RouteShape getShapeBetweenStops(String begin_stop_id,
			String end_stop_id, String shape_id) {
		Log.i(TAG, "begin_stop_id: " + begin_stop_id + ", end_stop_id: "
				+ end_stop_id);
		Parameters parameters = new Parameters();
		parameters.put("begin_stop_id", begin_stop_id);
		parameters.put("end_stop_id", end_stop_id);

		return getShapeHelper(shape_id, parameters, GET_SHAPE_BETWEEN_STOPS);
	}

	public RouteShape getShape(String shape_id) {
		Parameters parameters = new Parameters();
		return getShapeHelper(shape_id, parameters, GET_SHAPE);
	}

	private RouteShape getShapeHelper(String shape_id, Parameters parameters,
			String apiMethod) {
		Log.i(TAG, "shape_id: " + shape_id);
		parameters.put("shape_id", shape_id);
		String urlCheck = apiMethod + "?" + formatHttpGetParameters(parameters);
		String jsonData = mDatabase.checkMTDAPICache(urlCheck);

		Parameters paramsCopy = (Parameters) Util.copyParcelable(parameters);

		boolean isCached = false;
		JSONObject jobj = null;
		if (jsonData != null) {
			try {
				jobj = new JSONObject(jsonData);
				Log.i(TAG, "cache hit!!!");
				// check changeset_id
				parameters.put("changeset_id", jobj.getString("changeset_id"));
				parameters.put("key", MTD_API_KEY);
				JSONObject jobjCheck = httpGet(apiMethod, parameters);
				if (jobjCheck != null && jobjCheck.getBoolean("new_changeset")) {
					jobj = jobjCheck;
				} else {
					isCached = true;
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				Log.e(TAG, "getShapeHelper error parsing json from cache", e1);
				jobj = null;
			}
		} else {
			parameters.put("key", MTD_API_KEY);
			jobj = httpGet(apiMethod, parameters);
			if (hasError())
				return null;
			checkStatusCode(jobj);
			if (hasError())
				return null;
		}
		JSONArray jarray;
		RouteShape routeShape = null;
		try {
			jarray = jobj.getJSONArray("shapes");

			if (jarray.length() > 0) {
				routeShape = new RouteShape(shape_id);
				for (int i = 0; i < jarray.length(); i++) {
					JSONObject ob = jarray.getJSONObject(i);
					double shape_dist_traveled = ob
							.getDouble("shape_dist_traveled");
					double shape_pt_lat = ob.getDouble("shape_pt_lat");
					double shape_pt_lon = ob.getDouble("shape_pt_lon");
					int shape_pt_sequence = ob.getInt("shape_pt_sequence");
					String stop_id;
					if (ob.has("stop_id"))
						stop_id = ob.getString("stop_id");
					else
						stop_id = "";

					LatLng latLng = new LatLng(shape_pt_lat, shape_pt_lon);
					routeShape.add(new RouteShapePoint(shape_dist_traveled,
							latLng, shape_pt_sequence, stop_id));

				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "getShapeHelper " + e.getMessage());
			error = new RESTJSONAdapterError(e);
			return null;
		}
		if (!isCached && routeShape != null) {
			// use paramsCopy so we don't put a link in there with a
			// key
			paramsCopy.put("shape_id", shape_id);

			String query = apiMethod + "?"
					+ formatHttpGetParameters(paramsCopy);
			mDatabase.cacheQueryResult(query, jobj.toString());
		}
		return routeShape;
	}

	// TODO debug me!
	public List<Trip> getTripsByRoute(String route_id,
			boolean keepDuplicateShapeIds) {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		parameters.put("route_id", route_id);

		JSONObject jobj = httpGet(GET_TRIPS_BY_ROUTE, parameters);
		if (hasError())
			return null;
		checkStatusCode(jobj);
		if (hasError())
			return null;
		JSONArray jarray;
		List<Trip> list = new ArrayList<Trip>();
		List<String> shapeIds = new ArrayList<String>(); // check to make sure
															// we don't add
															// trips with same
															// id if desired
		try {
			jarray = jobj.getJSONArray("trips");
			JSONObject ji;
			for (int i = 0; i < jarray.length(); i++) {
				ji = jarray.getJSONObject(i);
				if (!shapeIds.contains(ji.getString("shape_id"))) {
					list.add(getTripFromJsonTripObject(ji));
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "getTripsByRoute " + e.getMessage());
			error = new RESTJSONAdapterError(e);
			return null;
		}
		return list;
	}

	private Trip getTripFromJsonTripObject(JSONObject trip)
			throws JSONException {
		return new Trip(trip.getString("block_id"),
				trip.getString("direction"), trip.getString("route_id"),
				trip.getString("service_id"), trip.getString("shape_id"),
				trip.getString("trip_headsign"), trip.getString("trip_id"));
	}

	public String getPlannedTripsByStopsUrl(TripPlanParameters params) {
		return getPlannedTripsByStopsUrl(params.origin.getId(),
				params.destination.getId(), params.getFormattedDate(),
				params.getFormattedTime(), params.getWalkFormattedDouble(),
				params.getMinimizeFormattedString(),
				params.getDepArrFormattedString());
	}

	public ArrayList<Itinerary> getPlannedTripsByStops(TripPlanParameters params) {
		return getPlannedTripsByStops(params.origin.getId(),
				params.destination.getId(), params.getFormattedDate(),
				params.getFormattedTime(), params.getWalkFormattedDouble(),
				params.getMinimizeFormattedString(),
				params.getDepArrFormattedString());
	}

	/**
	 * Returns a list of itineraries for planned trips between stops
	 * 
	 * @param deparr
	 * @param least
	 * @param walk
	 * @param time
	 * @param date
	 */
	public ArrayList<Itinerary> getPlannedTripsByStops(String origin_stop_id,
			String destination_stop_id, String date, String time, double walk,
			String least, String deparr) {
		String url = getPlannedTripsByStopsUrl(origin_stop_id,
				destination_stop_id, date, time, walk, least, deparr);

		JSONObject jobj = httpGet(url);
		return parseItinerariesFromJson(jobj);
	}

	public String getPlannedTripsByStopsUrl(String origin_stop_id,
			String destination_stop_id, String date, String time, double walk,
			String least, String deparr) {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		parameters.put("origin_stop_id", origin_stop_id);
		parameters.put("destination_stop_id", destination_stop_id);
		parameters.put("date", date);
		parameters.put("time", time);
		parameters.put("minimize", least);
		parameters.put("max_walk", walk <= 1.0 && walk >= 0.0 ? walk : 0.5);
		parameters.put("arrive_depart", deparr);

		String url = getHttpGetUrl(GET_PLANNED_TRIPS_BY_STOPS, parameters);
		return url;
	}

	public String getPlannedTripsByLatLonUrl(TripPlanParameters tripParams) {
		LatLng orll = tripParams.origin.getLatLng();
		LatLng dell = tripParams.destination.getLatLng();

		return getPlannedTripsByLatLonUrl(orll.latitude, orll.longitude,
				dell.latitude, dell.longitude, tripParams.getFormattedDate(),
				tripParams.getFormattedTime(),
				tripParams.getWalkFormattedDouble(),
				tripParams.getMinimizeFormattedString(),
				tripParams.getDepArrFormattedString());
	}

	public ArrayList<Itinerary> getPlannedTripsByLatLon(
			TripPlanParameters tripParams) {
		LatLng orll = tripParams.origin.getLatLng();
		LatLng dell = tripParams.destination.getLatLng();

		return getPlannedTripsByLatLon(orll.latitude, orll.longitude,
				dell.latitude, dell.longitude, tripParams.getFormattedDate(),
				tripParams.getFormattedTime(),
				tripParams.getWalkFormattedDouble(),
				tripParams.getMinimizeFormattedString(),
				tripParams.getDepArrFormattedString());
	}

	/**
	 * Returns a list of itineraries for planned trips between an original
	 * lat/lon and a destination lat/lon.
	 * 
	 * @param origin_lat
	 * @param origin_lon
	 * @param destination_lat
	 * @param destination_lon
	 * @return
	 */
	public List<Itinerary> getPlannedTripsByLatLon(double origin_lat,
			double origin_lon, double destination_lat, double destination_lon) {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		parameters.put("origin_lat", origin_lat);
		parameters.put("origin_lon", origin_lon);
		parameters.put("destination_lat", destination_lat);
		parameters.put("destination_lon", destination_lon);

		JSONObject jobj = httpGet(GET_PLANNED_TRIPS_BY_LAT_LON, parameters);
		return parseItinerariesFromJson(jobj);
	}

	public ArrayList<Itinerary> getPlannedTripsByLatLon(double origin_lat,
			double origin_lon, double destination_lat, double destination_lon,
			String date, String time, double maxWalk, String minimize,
			String arrive_depart) {
		String url = getPlannedTripsByLatLonUrl(origin_lat, origin_lon,
				destination_lat, destination_lon, date, time, maxWalk,
				minimize, arrive_depart);

		JSONObject jobj = httpGet(url);
		return parseItinerariesFromJson(jobj);
	}

	public String getPlannedTripsByLatLonUrl(double origin_lat,
			double origin_lon, double destination_lat, double destination_lon,
			String date, String time, double maxWalk, String minimize,
			String arrive_depart) {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		parameters.put("origin_lat", origin_lat);
		parameters.put("origin_lon", origin_lon);
		parameters.put("destination_lat", destination_lat);
		parameters.put("destination_lon", destination_lon);
		parameters.put("date", date);
		parameters.put("time", time);
		parameters.put("max_walk", maxWalk <= 1.0 && maxWalk >= 0.0 ? maxWalk
				: 0.5);
		parameters.put("minimize", minimize);
		parameters.put("arrive_depart", arrive_depart);

		String url = getHttpGetUrl(GET_PLANNED_TRIPS_BY_LAT_LON, parameters);
		return url;
	}

	/**
	 * 
	 * @param vehicleId
	 * @return a Vehicle object or null if not available.
	 */
	public Vehicle getVehicle(String vehicleId) {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		parameters.put("vehicle_id", vehicleId);
		JSONObject jobj = httpGet(GET_VEHICLE, parameters);
		if (hasError())
			return null;
		checkStatusCode(jobj);
		if (hasError())
			return null;
		Vehicle vehicle = null;
		try {
			JSONArray vehicleArray = jobj.getJSONArray("vehicles");
			if (vehicleArray.length() > 0) {
				JSONObject vehicleObj = vehicleArray.getJSONObject(0);
				LatLng latLng = new LatLng(vehicleObj.getJSONObject("location")
						.getDouble("lat"), vehicleObj.getJSONObject("location")
						.getDouble("lon"));

				vehicle = new Vehicle(vehicleObj.getString("vehicle_id"),
						getTripFromJsonTripObject(vehicleObj
								.getJSONObject("trip")), latLng,
						vehicleObj.getString("previous_stip_id"),
						vehicleObj.getString("next_stop_id"),
						vehicleObj.getString("origin_stop_id"),
						vehicleObj.getString("destination_stop_id"),
						vehicleObj.getString("last_updated"));
			}
		} catch (JSONException e) {
			Log.e(TAG, "getVehicle " + e.getMessage());
			error = new RESTJSONAdapterError(e);
			return null;
		}

		return vehicle;
	}

	/**
	 * Get information for all currently tracked vehicles.
	 * 
	 * @return
	 */
	public List<Vehicle> getVehicles() {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		JSONObject jobj = httpGet(GET_VEHICLES, parameters);
		if (hasError())
			return null;
		checkStatusCode(jobj);
		if (hasError())
			return null;
		List<Vehicle> vehicles = getVehiclesFromJSONObject(jobj);
		return vehicles;
	}

	/**
	 * Get a vehicle's real-time location by route_id.
	 * 
	 * @param routeId
	 *            - ID of the route, or list of route_id's separated by a ';'
	 *            semicolon.
	 * @return
	 */
	public List<Vehicle> getVehiclesByRoute(String routeId) {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		parameters.put("route_id", routeId);
		JSONObject jobj = httpGet(GET_VEHICLES_BY_ROUTE, parameters);
		if (hasError())
			return null;
		checkStatusCode(jobj);
		if (hasError())
			return null;
		List<Vehicle> vehicles = getVehiclesFromJSONObject(jobj);
		return vehicles;
	}

	public List<Vehicle> getVehiclesByRoute(Route route) {
		return getVehiclesByRoute(route.getId());
	}

	public List<Vehicle> getVehiclesByRoute(List<Route> routes) {
		String s = "";
		for (Route route : routes) {
			s = s + route.getId() + ";";
		}
		// Remove trailing ;
		if (s.length() > 0) {
			s = s.substring(0, s.length() - 1);
		}
		return getVehiclesByRoute(s);
	}

	private List<Vehicle> getVehiclesFromJSONObject(JSONObject jobj) {
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		try {
			JSONArray vehicleArray = jobj.getJSONArray("vehicles");
			for (int i = 0; i < vehicleArray.length(); i++) {
				JSONObject vehicleObj = vehicleArray.getJSONObject(i);
				LatLng latLng = new LatLng(vehicleObj.getJSONObject("location")
						.getDouble("lat"), vehicleObj.getJSONObject("location")
						.getDouble("lon"));

				vehicles.add(new Vehicle(vehicleObj.getString("vehicle_id"),
						getTripFromJsonTripObject(vehicleObj
								.getJSONObject("trip")), latLng, vehicleObj
								.getString("previous_stip_id"), vehicleObj
								.getString("next_stop_id"), vehicleObj
								.getString("origin_stop_id"), vehicleObj
								.getString("destination_stop_id"), vehicleObj
								.getString("last_updated")));
			}
		} catch (JSONException e) {
			Log.e(TAG, "getVehicles " + e.getMessage());
			error = new RESTJSONAdapterError(e);
			return null;
		}
		return vehicles;
	}

	/**
	 * Get all currently active reroutes.
	 * 
	 * @return
	 */
	public List<Reroute> getReroutes() {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		JSONObject jobj = httpGet(GET_REROUTES, parameters);
		if (hasError())
			return null;
		checkStatusCode(jobj);
		if (hasError())
			return null;
		return getReroutesFromJson(jobj);
	}

	/**
	 * Get all currently active reroutes that affect the specified route.
	 * 
	 * @param route_id
	 * @return
	 */
	public List<Reroute> getReroutesByRoute(String route_id) {
		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		parameters.put("route_id", route_id);
		JSONObject jobj = httpGet(GET_REROUTES_BY_ROUTE, parameters);
		if (hasError())
			return null;
		checkStatusCode(jobj);
		if (hasError())
			return null;
		return getReroutesFromJson(jobj);
	}

	public List<Reroute> getReroutesByRoute(Route route) {
		return getReroutesByRoute(route.getId());
	}

	public List<Reroute> getReroutesByRoute(List<Route> routes) {
		String routeIds = "";
		for (Route route : routes) {
			routeIds = routeIds + route.getId() + ";";
		}
		// Remove trailing ';'.
		if (routeIds.length() > 0) {
			routeIds = routeIds.substring(0, routeIds.length());
		}
		return getReroutesByRoute(routeIds);
	}

	/**
	 * 
	 * @param jobj
	 * @return
	 */
	private List<Reroute> getReroutesFromJson(JSONObject jobj) {
		List<Reroute> reroutes = new ArrayList<Reroute>();
		try {
			JSONArray reArray = jobj.getJSONArray("reroutes");
			for (int i = 0; i < reArray.length(); i++) {
				JSONObject jreroute = reArray.getJSONObject(i);
				JSONArray jAffected = jreroute.getJSONArray("affected_routes");
				List<Route> affectedRoutes = new ArrayList<Route>();
				for (int j = 0; j < jAffected.length(); j++) {
					JSONObject jroute = jAffected.getJSONObject(j);
					affectedRoutes.add(new Route(jroute
							.getString("route_color"), jroute
							.getString("route_id"), jroute
							.getString("route_long_name"), jroute
							.getString("route_short_name"), jroute
							.getString("route_text_color")));
				}
				reroutes.add(new Reroute(jreroute.getString("start_date"),
						jreroute.getString("end_date"), jreroute
								.getString("message"), jreroute
								.getString("description"), affectedRoutes));
			}
		} catch (JSONException e) {
			Log.e(TAG, "getReroutesFromJson " + e.getMessage());
			error = new RESTJSONAdapterError(e);
			return null;
		}
		return reroutes;
	}

	/**
	 * Parses itineraries from JSON format.
	 * 
	 * @param jobj
	 * @return
	 */
	public ArrayList<Itinerary> parseItinerariesFromJson(JSONObject jobj) {
		if (hasError())
			return null;
		checkStatusCode(jobj);
		if (hasError())
			return null;
		try {
			if (jobj.getJSONObject("status").getString("msg")
					.contains("trivial")) {
				error = new RESTJSONAdapterError(jobj.getJSONObject("status")
						.getString("msg"));
				return null;
			}
		} catch (JSONException e1) {
		}
		JSONArray jarray;
		ArrayList<Itinerary> itineraries = new ArrayList<Itinerary>();

		try {
			// grab the itineraries
			jarray = jobj.getJSONArray("itineraries");
			JSONObject jitinerary;
			// iterate over the itineraries
			for (int i = 0; i < jarray.length(); i++) {
				jitinerary = jarray.getJSONObject(i);
				Itinerary itinerary = new Itinerary(
						jitinerary.getString("start_time"),
						jitinerary.getString("end_time"),
						jitinerary.getInt("travel_time"));
				JSONArray jlegs = jitinerary.getJSONArray("legs");
				// iterate over the legs in the itinerary
				for (int j = 0; j < jlegs.length(); j++) {
					JSONObject jleg = jlegs.getJSONObject(j);
					String type = jleg.getString("type");
					// TODO Finish this!
					if (type.equals("Walk")) {
						JSONObject jwalk = jleg.getJSONObject("walk");
						String direction = jwalk.getString("direction");
						double distance = jwalk.getDouble("distance");
						JSONObject jbegin = jwalk.getJSONObject("begin");
						JSONObject jend = jwalk.getJSONObject("end");
						String stop_id = null;
						// some walk legs don't have stop_ids, so we need to
						// check first to avoid exception.
						if (jbegin.has("stop_id"))
							stop_id = jbegin.getString("stop_id");
						LegEndpoint begin = new LegEndpoint(
								jbegin.getDouble("lat"),
								jbegin.getDouble("lon"),
								jbegin.getString("name"), stop_id,
								jbegin.getString("time"));
						stop_id = null;
						if (jend.has("stop_id"))
							stop_id = jend.getString("stop_id");
						LegEndpoint end = new LegEndpoint(
								jend.getDouble("lat"), jend.getDouble("lon"),
								jend.getString("name"), stop_id,
								jend.getString("time"));
						WalkLeg walkLeg = new WalkLeg(begin, end, direction,
								distance);
						itinerary.addLeg(walkLeg);
					} else if (type.equals("Service")) {
						JSONArray jservices = jleg.getJSONArray("services");
						// iterate over the services
						JSONObject jservice = null;
						for (int s = 0; s < jservices.length(); s++) {
							jservice = jservices.getJSONObject(s);
							JSONObject jbegin = jservice.getJSONObject("begin");
							JSONObject jend = jservice.getJSONObject("end");
							JSONObject jroute = jservice.getJSONObject("route");
							JSONObject jtrip = jservice.getJSONObject("trip");
							LegEndpoint begin = new LegEndpoint(
									jbegin.getDouble("lat"),
									jbegin.getDouble("lon"),
									jbegin.getString("name"),
									jbegin.getString("stop_id"),
									jbegin.getString("time"));
							LegEndpoint end = new LegEndpoint(
									jend.getDouble("lat"),
									jend.getDouble("lon"),
									jend.getString("name"),
									jend.getString("stop_id"),
									jend.getString("time"));
							Route route = new Route(
									jroute.getString("route_color"),
									jroute.getString("route_id"),
									jroute.getString("route_long_name"),
									jroute.getString("route_short_name"),
									jroute.getString("route_text_color"));
							Trip trip = new Trip(jtrip.getString("block_id"),
									jtrip.getString("direction"),
									jtrip.getString("route_id"),
									jtrip.getString("service_id"),
									jtrip.getString("shape_id"),
									jtrip.getString("trip_headsign"),
									jtrip.getString("trip_id"));
							ServiceLeg serviceLeg = new ServiceLeg(begin, end,
									route, trip);
							itinerary.addLeg(serviceLeg);
						}
					} else {
						error = new RESTJSONAdapterError(
								"type not found or incorrect : " + type);
						return null;
					}
				}

				itineraries.add(itinerary);
			}
		} catch (JSONException e) {
			Log.e(TAG, "getPlannedTripsByLatLon " + e.getMessage());
			e.printStackTrace();
			error = new RESTJSONAdapterError(e);
			return null;
		}

		return itineraries;
	}

	public boolean downloadStops(final Context context) {
		Notification notification;
		PendingIntent contentIntent;
		NotificationManager mNotificationManager;

		SharedPreferences settings = context.getSharedPreferences(
				DatabaseAdapter.DbOk, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(DatabaseAdapter.DbOkStops, DatabaseAdapter.IS_WORKING);
		editor.commit();
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) context
				.getSystemService(ns);
		CharSequence tickerText = "Downloading databases";
		long when = System.currentTimeMillis();

		notification = new Notification(R.drawable.ic_launcher, tickerText,
				when);

		CharSequence contentTitle = "Downloading stops...";
		CharSequence contentText = "";
		Intent notificationIntent = new Intent(context, MTDAppActivity.class);
		contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		mNotificationManager.notify(STOPS_NOTIFICATION_ID, notification);

		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		notification.setLatestEventInfo(context, "Initializing stops database",
				"Downloading Stops...", contentIntent);
		mNotificationManager.notify(STOPS_NOTIFICATION_ID, notification);
		JSONObject jobj = httpGet(GET_STOPS, parameters);

		if (hasError())
			return false;

		String timeDownloaded;
		try {
			timeDownloaded = jobj.getString("time");
		} catch (JSONException e) {
			timeDownloaded = "2012-01-24T16:07:11-06:00";
			Log.e(TAG, "Time downloaded error.");
			e.printStackTrace();
		}

		if (hasError())
			return false;

		notification.setLatestEventInfo(context, "Initializing stops database",
				"Finished Downloading Stops!", contentIntent);
		mNotificationManager.notify(STOPS_NOTIFICATION_ID, notification);

		if (hasError())
			return false;
		checkStatusCode(jobj);
		if (hasError())
			return false;

		// TODO fix
		// // Change the stoppoints database table to a temp one.
		// StopPointsTable dbhelp = new StopPointsTable(context);
		// dbhelp.putCurrentTableInTempTableAndCreateNew();
		// StopsTable dbS = new StopsTable(context);
		// dbS.putCurrentTableInTempTableAndCreateNew();

		String ret = addStopsFromJson(context, jobj, notification,
				mNotificationManager, contentIntent, STOPS_NOTIFICATION_ID);

		// dbhelp.retainFavorites();
		// dbS.retainFavorites();
		// dbhelp.dropUpdateTable();
		// dbS.dropUpdateTable();

		if (ret.length() > 0)
			error = new RESTJSONAdapterError(ret);

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		String s = "Stops database finished!";
		String sub = "";
		if (hasError()) {
			s = "Error downloading stops.";
			sub = getErrorMessage();
			editor.putInt(DatabaseAdapter.DbOkStops, DatabaseAdapter.IS_BAD);
			return false;
		} else {
			editor.putInt(DatabaseAdapter.DbOkStops, DatabaseAdapter.IS_OK);
			editor.putString(DatabaseAdapter.DbStopsUpdateTime, timeDownloaded);
		}
		editor.commit();
		notification.setLatestEventInfo(context, s, sub, contentIntent);
		mNotificationManager.notify(STOPS_NOTIFICATION_ID, notification);
		return true;
	}

	public boolean downloadRoutes(final RoutesDatabase database,
			final Context context) {
		Notification notification;
		PendingIntent contentIntent;
		NotificationManager mNotificationManager;

		SharedPreferences settings = context.getSharedPreferences(
				DatabaseAdapter.DbOk, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(DatabaseAdapter.DbOkRoutes, DatabaseAdapter.IS_WORKING);
		editor.commit();
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) context
				.getSystemService(ns);
		CharSequence tickerText = "Downloading routes databases";
		long when = System.currentTimeMillis();

		notification = new Notification(R.drawable.ic_launcher, tickerText,
				when);

		CharSequence contentTitle = "Downloading routes...";
		CharSequence contentText = "";
		Intent notificationIntent = new Intent(context, MTDAppActivity.class);
		contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		mNotificationManager.notify(ROUTE_NOTIFICATION_ID, notification);

		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		notification.setLatestEventInfo(context,
				"Initializing routes database", "Downloading Routes...",
				contentIntent);
		mNotificationManager.notify(ROUTE_NOTIFICATION_ID, notification);
		JSONObject jobj = httpGet(GET_ROUTES, parameters);
		notification.setLatestEventInfo(context,
				"Initializing routes database", "Finished downloading!",
				contentIntent);
		mNotificationManager.notify(ROUTE_NOTIFICATION_ID, notification);

		if (hasError())
			return false;
		checkStatusCode(jobj);
		if (hasError())
			return false;

		String timeDownloaded;
		try {
			timeDownloaded = jobj.getString("time");
		} catch (JSONException e) {
			timeDownloaded = "2012-01-24T16:07:11-06:00";
			Log.e(TAG, "Time downloaded error.");
			e.printStackTrace();
		}

		String ret = database.addRoutesFromJson(context, jobj, notification,
				mNotificationManager, ROUTE_NOTIFICATION_ID, contentIntent);

		if (ret.length() > 0) {
			error = new RESTJSONAdapterError(ret);
		}

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		String s = "Routes database finished!";
		String sub = "";
		if (hasError()) {
			s = "Error downloading routes.";
			sub = getErrorMessage();
			editor.putInt(DatabaseAdapter.DbOkRoutes, DatabaseAdapter.IS_BAD);
		} else {
			editor.putInt(DatabaseAdapter.DbOkRoutes, DatabaseAdapter.IS_OK);
			editor.putString(DatabaseAdapter.DbRoutesUpdateTime, timeDownloaded);
		}
		editor.commit();
		notification.setLatestEventInfo(context, s, sub, contentIntent);
		mNotificationManager.notify(ROUTE_NOTIFICATION_ID, notification);
		return true;
	}

	public String addStopsFromJson(Context context, JSONObject jobj,
			Notification notification,
			NotificationManager mNotificationManager,
			PendingIntent contentIntent, int STOPS_NOTIFICATION_ID) {
		String ret = "";
		JSONArray jarray;
		ArrayList<ContentProviderOperation> stopOperations = new ArrayList<ContentProviderOperation>();
		ArrayList<ContentProviderOperation> stopPointOperations = new ArrayList<ContentProviderOperation>();
		try {
			jarray = jobj.getJSONArray("stops");
			JSONObject jstop;
			JSONArray jspointsarray;
			JSONObject jstoppoint;

			String code;

			for (int i = 0; i < jarray.length(); i++) {
				jstop = jarray.getJSONObject(i);
				jspointsarray = jstop.getJSONArray("stop_points");
				double lat = 0.0;
				double lon = 0.0;
				double latAverage = 0.0;
				double lonAverage = 0.0;
				int numStopPoints = jspointsarray.length();
				for (int j = 0; j < numStopPoints; j++) {
					jstoppoint = jspointsarray.getJSONObject(j);
					lat = jstoppoint.getDouble("stop_lat");
					lon = jstoppoint.getDouble("stop_lon");
					latAverage += lat;
					lonAverage += lon;

					code = jstoppoint.getString("code");

					stopPointOperations
							.add(ContentProviderOperation
									.newInsert(StopPoints.CONTENT_URI)
									.withValue(
											MyLocationContract.StopPoints.CODE,
											code)
									// TODO fix
									// .withValue(StopPointsTable.KEY_FAVORITE,
									// 0)
									.withValue(
											MyLocationContract.StopPoints.ID,
											jstoppoint.getString("stop_id"))
									.withValue(
											MyLocationContract.StopPoints.LAT,
											lat)
									.withValue(
											MyLocationContract.StopPoints.LON,
											lon)
									.withValue(
											MyLocationContract.StopPoints.NAME,
											jstoppoint.getString("stop_name"))
									.withValue(
											MyLocationContract.StopPoints.SNIPPET,
											code).build());
				}
				// Put the stop at the average lat/lon of its stoppoints
				if (numStopPoints > 0) {
					latAverage = latAverage / (1.0 * numStopPoints);
					lonAverage = lonAverage / (1.0 * numStopPoints);
				}
				code = jstop.getString("code");
				stopOperations.add(ContentProviderOperation
						.newInsert(Stops.CONTENT_URI)
						.withValue(MyLocationContract.Stops.CODE, code)
						// TODO fix
						// .withValue(StopsTable.KEY_FAVORITE, 0)
						.withValue(MyLocationContract.Stops.ID,
								jstop.getString("stop_id"))
						.withValue(MyLocationContract.Stops.LAT, latAverage)
						.withValue(MyLocationContract.Stops.LON, lonAverage)
						.withValue(MyLocationContract.Stops.NAME,
								jstop.getString("stop_name"))
						.withValue(MyLocationContract.Stops.SNIPPET, code)
						.build());

				// publish progress
				if (i % 100 == 0) {
					String s = "Stop(" + i + "/" + jarray.length() + "): "
							+ jstop.getString("stop_name");
					notification.setLatestEventInfo(context,
							"Initializing stops database", s, contentIntent);
					mNotificationManager.notify(STOPS_NOTIFICATION_ID,
							notification);
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "GetStops" + " " + e.getMessage());
			e.printStackTrace();
			ret = e.getMessage();
		}
		// end the StopPointsDatabase operation
		try {
			// add the stops first so we dont get a foreign key mismatch.
			context.getContentResolver().applyBatch(
					MyLocationContract.AUTHORITY, stopOperations);
			context.getContentResolver().applyBatch(
					MyLocationContract.AUTHORITY, stopPointOperations);
		} catch (RemoteException e) {
			Log.e(TAG, "err apply batch: ", e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			Log.e(TAG, "err apply batch: ", e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public boolean updateStopsFromGTFS(Context context) {
		Notification notification;
		PendingIntent contentIntent;
		NotificationManager mNotificationManager;

		SharedPreferences settings = context.getSharedPreferences(
				DatabaseAdapter.DbOk, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(DatabaseAdapter.DbOkStops, DatabaseAdapter.IS_UPDATING);
		editor.commit();
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) context
				.getSystemService(ns);
		CharSequence tickerText = "Updating databases";
		long when = System.currentTimeMillis();

		notification = new Notification(R.drawable.ic_launcher, tickerText,
				when);

		CharSequence contentTitle = "Updating stops...";
		CharSequence contentText = "";
		Intent notificationIntent = new Intent(context, MTDAppActivity.class);
		contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		mNotificationManager.notify(STOPS_NOTIFICATION_ID, notification);

		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		notification.setLatestEventInfo(context, "Updating stops database",
				"Downloading Stops...", contentIntent);
		mNotificationManager.notify(STOPS_NOTIFICATION_ID, notification);
		JSONObject jobj = httpGet(GET_STOPS, parameters);

		if (hasError())
			return false;

		String timeDownloaded;
		try {
			timeDownloaded = jobj.getString("time");
		} catch (JSONException e) {
			timeDownloaded = "2012-01-24T16:07:11-06:00";
			Log.e(TAG, "Time downloaded error.");
			e.printStackTrace();
		}

		if (hasError())
			return false;

		notification.setLatestEventInfo(context, "Updating stops database",
				"Finished Downloading Stops!", contentIntent);
		mNotificationManager.notify(STOPS_NOTIFICATION_ID, notification);

		if (hasError())
			return false;
		checkStatusCode(jobj);
		if (hasError())
			return false;

		// TODO fix
		// Change the stoppoints database table to a temp one.
		// StopPointsTable dbhelp = new StopPointsTable(context);
		// dbhelp.putCurrentTableInTempTableAndCreateNew();
		// StopsTable dbS = new StopsTable(context);
		// dbS.putCurrentTableInTempTableAndCreateNew();

		String ret = addStopsFromJson(context, jobj, notification,
				mNotificationManager, contentIntent, STOPS_NOTIFICATION_ID);

		// dbhelp.retainFavorites();
		// dbS.retainFavorites();
		// dbhelp.dropUpdateTable();
		// dbS.dropUpdateTable();

		if (ret.length() > 0)
			error = new RESTJSONAdapterError(ret);

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		String s = "Stops database update finished!";
		String sub = "";
		if (hasError()) {
			Log.d(TAG, "no error in updating stops!");
			s = "Error updating stops.";
			sub = getErrorMessage();
			// editor.putInt(DatabaseAdapter.DbOkStops, DatabaseAdapter.IS_BAD);
			return false;
		} else {
			Log.d(TAG, "no error in updating stops!");
			editor.putInt(DatabaseAdapter.DbOkStops, DatabaseAdapter.IS_OK);
			editor.putString(DatabaseAdapter.DbStopsUpdateTime, timeDownloaded);
		}
		editor.commit();
		notification.setLatestEventInfo(context, s, sub, contentIntent);
		mNotificationManager.notify(STOPS_NOTIFICATION_ID, notification);
		return true;
	}

	public boolean updateRoutesFromGTFS(Context context) {
		Notification notification;
		PendingIntent contentIntent;
		NotificationManager mNotificationManager;

		SharedPreferences settings = context.getSharedPreferences(
				DatabaseAdapter.DbOk, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(DatabaseAdapter.DbOkRoutes, DatabaseAdapter.IS_WORKING);
		editor.commit();
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) context
				.getSystemService(ns);
		CharSequence tickerText = "Updating routes databases";
		long when = System.currentTimeMillis();

		notification = new Notification(R.drawable.ic_launcher, tickerText,
				when);

		CharSequence contentTitle = "Updating routes...";
		CharSequence contentText = "";
		Intent notificationIntent = new Intent(context, MTDAppActivity.class);
		contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		mNotificationManager.notify(ROUTE_NOTIFICATION_ID, notification);

		Parameters parameters = new Parameters();
		parameters.put("key", MTD_API_KEY);
		notification.setLatestEventInfo(context, "Updating routes database",
				"Downloading Routes...", contentIntent);
		mNotificationManager.notify(ROUTE_NOTIFICATION_ID, notification);
		JSONObject jobj = httpGet(GET_ROUTES, parameters);
		notification.setLatestEventInfo(context, "Updating routes database",
				"Finished downloading!", contentIntent);
		mNotificationManager.notify(ROUTE_NOTIFICATION_ID, notification);

		if (hasError())
			return false;
		checkStatusCode(jobj);
		if (hasError())
			return false;

		String timeDownloaded;
		try {
			timeDownloaded = jobj.getString("time");
		} catch (JSONException e) {
			timeDownloaded = "2012-01-24T16:07:11-06:00";
			Log.e(TAG, "Time downloaded error.");
			e.printStackTrace();
		}

		String ret = RoutesDatabase.getInstance(context).updateRoutesFromJson(
				context, jobj, notification, mNotificationManager,
				ROUTE_NOTIFICATION_ID, contentIntent);
		if (ret.length() > 0) {
			error = new RESTJSONAdapterError(ret);
		}

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		String s = "Routes database finished!";
		String sub = "";
		if (hasError()) {
			s = "Error updating routes.";
			sub = getErrorMessage();
			editor.putInt(DatabaseAdapter.DbOkRoutes, DatabaseAdapter.IS_BAD);
		} else {
			editor.putInt(DatabaseAdapter.DbOkRoutes, DatabaseAdapter.IS_OK);
			editor.putString(DatabaseAdapter.DbRoutesUpdateTime, timeDownloaded);
		}
		editor.commit();
		notification.setLatestEventInfo(context, s, sub, contentIntent);
		mNotificationManager.notify(ROUTE_NOTIFICATION_ID, notification);
		return true;
	}

	@Override
	/**
	 * The default URL encoder puts a '+' sign in for spaces, but the MTD website doesn't like this. Need to replace '+' back with space code '%20'
	 */
	protected String forURL(String aURLFragment) {
		String url = super.forURL(aURLFragment);
		// replace + with %20
		return url.replace("+", "%20");
	}

}
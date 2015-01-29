package com.teamparkin.mtdapp.dataclasses;

import com.google.android.gms.maps.model.LatLng;


public class RouteShapePoint {

	private double shape_dist_traveled;
	private LatLng latLng;
	private int shape_pt_sequence;
	private String stop_id;
	
	public RouteShapePoint(RouteShapePoint rsp){
		this(rsp.getShape_dist_traveled(), rsp.getLatLng(), rsp.getShape_pt_sequence(), rsp.getStop_id());
	}

	public RouteShapePoint(double shape_dist_traveled, LatLng latLng,
			int shape_pt_sequence, String stop_id) {
		this.shape_dist_traveled = shape_dist_traveled;
		this.latLng = latLng;
		this.shape_pt_sequence = shape_pt_sequence;
		this.stop_id = stop_id;
	}

	public double getShape_dist_traveled() {
		return shape_dist_traveled;
	}

	public void setShape_dist_traveled(double shape_dist_traveled) {
		this.shape_dist_traveled = shape_dist_traveled;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public void setGeoPoint(LatLng geoPoint) {
		this.latLng = geoPoint;
	}

	public int getShape_pt_sequence() {
		return shape_pt_sequence;
	}

	public void setShape_pt_sequence(int shape_pt_sequence) {
		this.shape_pt_sequence = shape_pt_sequence;
	}

	public String getStop_id() {
		return stop_id;
	}

	public void setStop_id(String stop_id) {
		this.stop_id = stop_id;
	}
}

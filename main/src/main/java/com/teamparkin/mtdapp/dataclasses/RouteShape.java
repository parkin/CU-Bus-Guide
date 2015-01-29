package com.teamparkin.mtdapp.dataclasses;

import java.util.ArrayList;
import java.util.List;

public class RouteShape {

	private List<RouteShapePoint> shapePoints = new ArrayList<RouteShapePoint>();
	private String shape_id;
	
	public RouteShape(String shape_id){
		this.shape_id = shape_id;
	}

	public void add(RouteShapePoint routeShapePoint) {
		shapePoints.add(routeShapePoint);
	}
	
	public int size(){
		return shapePoints.size();
	}

	public RouteShapePoint getShapePoint(int i){
		return shapePoints.get(i);
	}
	
	public List<RouteShapePoint> getShapePoints(){
		return shapePoints;
	}
	
	
}

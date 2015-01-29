package com.teamparkin.mtdapp.listeners;

import java.util.ArrayList;
import java.util.Map;

import com.teamparkin.mtdapp.dataclasses.Departure;

public interface MyDepartureFragmentListener {

	public void onDepartureUpdated(
			Map<String, ArrayList<Departure>> stopPointsToDepartures);

}

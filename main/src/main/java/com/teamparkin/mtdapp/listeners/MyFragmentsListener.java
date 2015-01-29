package com.teamparkin.mtdapp.listeners;

import android.net.Uri;
import android.os.Bundle;

import com.teamparkin.mtdapp.dataclasses.AbstractStop;
import com.teamparkin.mtdapp.dataclasses.Stop;

public interface MyFragmentsListener {

	public interface OnLocationSelectedListener {
		/**
		 * Called when a location is selected.
		 * 
		 * @param location
		 */
		public void onLocationSelected(Uri location);
	}

	public interface OnDeparturesRequestedListener {
		public void onDeparturesRequested(AbstractStop stop);
	}

	public interface OnBusTrackerStartedListener {
		public void onBusTrackerStarted(Stop stop,
				MyDepartureFragmentListener updateListener);
	}

	public interface OnCancelBusTrackerListener {
		public void onCancelBusTracker();
	}

	public interface OnTripPlanRequestedListener {
		/**
		 * Called when the Trip plan Viewer is requested.
		 * 
		 * @param optionsBundle
		 *            - must include an "origin" location, a "destination"
		 *            location, "deparr" as "depart" or "arrive", "walking" as
		 *            max walking distance, "date" YYYY-MM-DD, "time" HH:MM, and
		 *            "least" as "walking" "transfers" or "time".
		 */
		public void onTripPlanRequested(Bundle optionsBundle);
	}

	/**
	 * Listen for something requesting the actionBar be visible/hidden.
	 * 
	 * @author will
	 * 
	 */
	public interface OnRequestActionBarVisibleListener {
		/**
		 * Requests the action bar visibility be = visible.
		 * 
		 * @param visible
		 */
		public void onRequestActionBarVisible(boolean visible);
	}
}

package com.teamparkin.mtdapp.listeners;

import com.teamparkin.mtdapp.dataclasses.MyLocation;

public interface OnOriginDestinationChangedListener {
	/**
	 * Called when the user sets an origin, either by selecting from the
	 * suggestions or the dialog.
	 * 
	 * @param origin
	 */
	public void onOriginSet(MyLocation origin);

	/**
	 * Called when the user sets a destination, either by selecting from the
	 * suggestions or the dialog.
	 * 
	 * @param origin
	 */
	public void onDestinationSet(MyLocation destination);

	public void onOriginRemoved();

	public void onDestinationRemoved();

}
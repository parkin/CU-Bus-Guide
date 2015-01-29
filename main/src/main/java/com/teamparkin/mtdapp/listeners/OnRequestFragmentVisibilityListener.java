package com.teamparkin.mtdapp.listeners;

public interface OnRequestFragmentVisibilityListener {
	/**
	 * You should return false when you don't want the location to be updated.
	 * 
	 * @return
	 */
	public boolean onRequestShouldUpdateLocation();
}
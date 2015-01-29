package com.teamparkin.mtdapp.listeners;

import android.net.Uri;

/**
 * Listener for requests activity_about the slideup panel.
 * 
 * @author will
 * 
 */
public interface OnRequestPanelInfoListener {

	/**
	 * You should return the panel's height.
	 * 
	 * @return
	 */
	public int onRequestPanelHeight();

	/**
	 * You should return the panel's location if it has one.
	 * 
	 * @return
	 */
	public Uri onRequestPanelLocation();

	/**
	 * You should return true if the panel is open false otherwise.
	 * 
	 * @return
	 */
	public boolean onRequestPanelIsOpen();
}
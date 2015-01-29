package com.teamparkin.mtdapp.listeners;

import android.view.View;

public interface OnExpandRequestedListener {

	/**
	 * Called when the view is expanded.
	 * 
	 * Return a view to have it animated as well, afterwards.
	 * 
	 * @param contentView
	 * @return
	 */
	public View onExpandRequested(View contentView);
	
	/**
	 * Called when the view is expanded.
	 * 
	 * Return a view to have it animated as well, afterwards.
	 * 
	 * @param contentView
	 * @return
	 */
	public View onCollapseRequested(View contentView);

}

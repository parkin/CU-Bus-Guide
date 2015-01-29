package com.teamparkin.mtdapp.views;

import android.content.Context;
import android.util.AttributeSet;

public class StreetViewTouchImageView extends TouchImageView {

	private String url = "";

	public StreetViewTouchImageView(Context context) {
		super(context);
	}

	public StreetViewTouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public StreetViewTouchImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}

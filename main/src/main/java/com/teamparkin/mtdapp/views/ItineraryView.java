package com.teamparkin.mtdapp.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.dataclasses.Itinerary;
import com.teamparkin.mtdapp.dataclasses.Leg;
import com.teamparkin.mtdapp.dataclasses.LegEndpoint;
import com.teamparkin.mtdapp.dataclasses.Route;
import com.teamparkin.mtdapp.dataclasses.ServiceLeg;
import com.teamparkin.mtdapp.util.Util;

public class ItineraryView extends LinearLayout {
	@SuppressWarnings("unused")
	private static final String TAG = ItineraryView.class.getSimpleName();

	private Itinerary mItinerary;

	public ItineraryView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public ItineraryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.itinerary, this, true);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public ItineraryView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setItinerary(Itinerary itinerary) {
		mItinerary = (Itinerary) Util.copyParcelable(itinerary);

		TextView timeSpan = (TextView) findViewById(R.id.itinerary_time_span);
		String timeSpanString = mItinerary.getStartTime() + " - "
				+ mItinerary.getEndTime();
		timeSpan.setText(timeSpanString);

		TextView estimatedTime = (TextView) findViewById(R.id.itinerary_estimatedTime);
		estimatedTime.setText("" + mItinerary.getTravelTime() + " min");

		ViewGroup legsParent = (ViewGroup) findViewById(R.id.itinerary_piclayout);
		legsParent.removeAllViews();

		float scale = getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (6 * scale + 0.5f);

		int i = 0;
		int count = itinerary.getLegs().size();
		boolean setDepartsAt = false;
		for (Leg leg : itinerary.getLegs()) {

			LayoutParams layoutParams = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.gravity = Gravity.CENTER;

			// If we have a service leg, add a text view with the route color
			// and name
			if (leg instanceof ServiceLeg) {
				TextView routeTV = new TextView(getContext());
				ServiceLeg sleg = ((ServiceLeg) leg);
				Route route = sleg.getRoute();
				routeTV.setText(route.getRoute_short_name());
				routeTV.setTextColor(Color.parseColor("#"
						+ route.getRoute_text_color()));
				routeTV.setBackgroundColor(Color.parseColor("#"
						+ route.getRoute_color()));
				LayoutParams layoutParams2 = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
				layoutParams2.gravity = Gravity.CENTER;
				layoutParams2.setMargins((int) (3 * scale + 0.5f),
						(int) (3 * scale + 0.5f), 0, (int) (3 * scale + 0.5f));
				routeTV.setLayoutParams(layoutParams2);
				routeTV.setGravity(Gravity.CENTER);
				routeTV.setPadding(dpAsPixels, dpAsPixels, dpAsPixels,
						dpAsPixels);
				routeTV.setTypeface(null, Typeface.BOLD);
				legsParent.addView(routeTV);

				if (!setDepartsAt) {
					// set the departs at text to the first stop where we take a
					// bus.
					LegEndpoint begin = leg.getBegin();
					String depString = "departs from " + begin.getName() + " ("
							+ begin.getTimeString() + ")";
					TextView depAt = (TextView) findViewById(R.id.itinerary_depart_from_text);
					depAt.setText(depString);
					setDepartsAt = true;
				}
			}

			ImageView iv = new ImageView(getContext());
			iv.setImageResource(leg.getImageResourceId());
			iv.setLayoutParams(layoutParams);
			legsParent.addView(iv);

			// Add the right arrow separator if not the last leg.
			if (i < count - 1) {
				TextView tv = new TextView(getContext());
				tv.setText(">");
				tv.setLayoutParams(layoutParams);
				tv.setGravity(Gravity.CENTER);
				tv.setTypeface(null, Typeface.BOLD);
				legsParent.addView(tv);
			}

			i++;

		}

	}
}

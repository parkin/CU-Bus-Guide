package com.teamparkin.mtdapp.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.teamparkin.mtdapp.R;
import com.teamparkin.mtdapp.adapters.MyExpandableLocationCursorAdapter;
import com.teamparkin.mtdapp.contentproviders.MyLocationContract;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener;
import com.teamparkin.mtdapp.listeners.MyFragmentsListener.OnLocationSelectedListener;

public class FavoritesFragment extends MyFragment implements
        LoaderCallbacks<Cursor> {
    @SuppressWarnings("unused")
    private static final String TAG = FavoritesFragment.class.getSimpleName();

    private static final int LOADER_FAVS_ID = 37293;

    private ListView mListView;

    private View mView;

    private OnLocationSelectedListener mLocationSelectedListener;

    private MyExpandableLocationCursorAdapter mCursorAdapter;

    public FavoritesFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        // Put this call in onResume so onLoadFinished doesn't get called twice
        // for some reason.
        getLoaderManager().initLoader(LOADER_FAVS_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.favorites, container, false);

        mCursorAdapter = new MyExpandableLocationCursorAdapter(getActivity(),
                null, 0, R.layout.mylocationlistitem_card,
                R.id.mylocationlistitem_parent, R.id.mylocationlistitem_child);
        mCursorAdapter
                .setActionViewResId(R.id.mylocationlistitem_overflow_button);

        mCursorAdapter
                .setOnLocationSelectedListener(new MyFragmentsListener.OnLocationSelectedListener() {
                    @Override
                    public void onLocationSelected(Uri location) {
                        dispatchOnLocationSelected(location);
                    }
                });

        mListView = (ListView) mView.findViewById(R.id.stoplist_list2);
        setViewForMarginChange(mListView);

        if (savedInstanceState == null) {
            // If no saved state, animate the entrance of the favorites list.
            SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
                    mCursorAdapter);
            swingBottomInAnimationAdapter.setAbsListView(mListView);
            //swingBottomInAnimationAdapter.setAnimationDurationMillis(150);
            mListView.setAdapter(swingBottomInAnimationAdapter);
        } else {
            // Do not animate when a saved state exists.
            mListView.setAdapter(mCursorAdapter);
        }
        return mView;
    }

    private void setNoStopTextVisibility(boolean b) {
        int visibility = (b) ? View.VISIBLE : View.GONE;
        TextView tv = (TextView) mView.findViewById(R.id.stoplist_no_stops);
        tv.setVisibility(visibility);

        int inverse = (b) ? View.GONE : View.VISIBLE;
        mListView.setVisibility(inverse);
    }

    public void setOnLocationSelectedListener(
            MyFragmentsListener.OnLocationSelectedListener listener) {
        mLocationSelectedListener = listener;
    }

    void dispatchOnLocationSelected(Uri location) {
        if (mLocationSelectedListener != null)
            mLocationSelectedListener.onLocationSelected(location);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        switch (id) {
            case LOADER_FAVS_ID:
                // This is called when a new Loader needs to be created. This
                // sample only has one Loader, so we don't care activity_about the ID.
                // First, pick the base URI to use depending on whether we are
                // currently filtering.
                Uri baseUri = MyLocationContract.MyLocation.CONTENT_URI;

			/*
             * Note for some reason you can't query integers using
			 * selectionArgs...
			 */
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        baseUri, null, MyLocationContract.MyLocation.FAVORITE
                        + "=1", null, MyLocationContract.MyLocation.TYPE
                        + " ASC, " + MyLocationContract.MyLocation.NAME
                        + " ASC");
                return cursorLoader;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_FAVS_ID:
                int size = cursor.getCount();
                setNoStopTextVisibility(size < 1);
                mCursorAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_FAVS_ID:
                mCursorAdapter.swapCursor(null);
                break;
        }
    }

}

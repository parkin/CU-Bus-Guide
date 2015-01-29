package com.teamparkin.mtdapp;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SettingsActivity extends PreferenceActivity {
    public static final String KEY_PREF_DATA_STORAGE_PLACE_CACHE_MAX_SIZE =
            "pref_data_storage_place_cache_max_size";
    public static final String KEY_PREF_UI_MAP_HIDE_NAV_DRAWER_ICON = "pref_ui_map_hide_nav_drawer_icon";
    public static final String KEY_PREF_UI_CATEGORY_SLIDE_UP_DEFAULT_HEIGHT =
            "pref_ui_category_slide_up_default_height";
    public static final String KEY_PREF_UI_NAVIGATION_HOME_SCREEN_ENABLED =
            "pref_ui_navigation_home_screen_enabled";
    public static final String KEY_PREF_UI_NAVIGATION_HOME_SCREEN = "pref_ui_navigation_home_screen";
    public static final String ACTION_ABOUT = "com.teamparkin.mtdapp.ABOUT";
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the SettingsFragment
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                    new SettingsFragment()).commit();
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Settings");
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getAction() != null && intent.getAction().equals
                (ACTION_ABOUT)) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return (true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SettingsFragment extends PreferenceFragment {

        /**
         * Sets up the action bar for an {@link PreferenceScreen}.
         * The home button doesn't do anything on subcategories, so we need
         * this ugly hack from
         * http://stackoverflow.com/questions/16374820/action-bar-home-button-not-functional-with-nested-preferencescreen
         *
         * @param preferenceScreen
         */
        public static void initializeActionBar(PreferenceScreen preferenceScreen) {
            final Dialog dialog = preferenceScreen.getDialog();

            if (dialog != null) {
                // Initalize the action bar
                dialog.getActionBar().setDisplayHomeAsUpEnabled(true);

                // Apply custom home button area click listener
                // to close the PreferenceScreen because PreferenceScreens are dialogs which swallow
                // events instead of passing to the activity
                // Related Issue: https://code.google.com/p/android/issues/detail?id=4611
                View homeBtn = dialog.findViewById(android.R.id.home);

                if (homeBtn != null) {
                    View.OnClickListener dismissDialogClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    };

                    // Prepare yourselves for some hacky programming
                    ViewParent homeBtnContainer = homeBtn.getParent();

                    // The home button is an ImageView inside a FrameLayout
                    if (homeBtnContainer instanceof FrameLayout) {
                        ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

                        if (containerParent instanceof LinearLayout) {
                            // This view also contains the title text, set the whole view as clickable
                            ((LinearLayout) containerParent).setOnClickListener(dismissDialogClickListener);
                        } else {
                            // Just set it on the home button
                            ((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
                        }
                    } else {
                        // The 'If all else fails' default case
                        homeBtn.setOnClickListener(dismissDialogClickListener);
                    }
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);

            // If the user has clicked on a preference screen, set up the action bar
            if (preference instanceof PreferenceScreen) {
                initializeActionBar((PreferenceScreen) preference);
            }

            return false;
        }
    }

}
package com.teamparkin.mtdapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class AboutActivity extends ActionBarActivity {

    private int mAppIconTapCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String versionName = "Version: ";
        int versionCode = -1;
        try {
            versionName += getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView versionTV = (TextView) findViewById(R.id.about_version);
        final String versionText = versionName + " (" + versionCode + ")";
        versionTV.setText(versionText);

        Button sendFeedbackButton = (Button) findViewById(R.id.about_send_feedback_button);
        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "teamparkinandroid@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on CU Bus Guide " + versionText);
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        Button donateButton = (Button) findViewById(R.id.about_donate_button);
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent donateIntent = new Intent(AboutActivity.this, DonateActivity.class);
                startActivity(donateIntent);
            }
        });

        Button joinGPlusCommunityButton = (Button) findViewById(R.id
                .about_join_gplus_community_button);
        joinGPlusCommunityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("https://plus.google.com/communities/115880256511565836752");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);
            }
        });

        Button becomeBetaTesterButton = (Button) findViewById(R.id.about_become_beta_tester_button);
        becomeBetaTesterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("https://play.google.com/apps/testing/com.teamparkin.mtdapp");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);
            }
        });

        Button termsOfUseButton = (Button) findViewById(R.id.about_terms_of_use_button);
        termsOfUseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHtmlActivity(R.raw.terms_of_use, true, "Terms of Use");
            }
        });

        Button privacyPolicyButton = (Button) findViewById(R.id.about_privacy_policy_button);
        privacyPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHtmlActivity(R.raw.privacy_policy, true, "Privacy Policy");
            }
        });

        // Add simple Easter Egg for users
        ImageButton appIconButton = (ImageButton) findViewById(R.id.about_launcher_icon_button);
        appIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAppIconTapCount++;
                if ((mAppIconTapCount % 10) == 0) {
                    Toast.makeText(AboutActivity.this, "...I-N-I!!", Toast.LENGTH_SHORT).show();
                } else if ((mAppIconTapCount % 5) == 0) {
                    Toast.makeText(AboutActivity.this, "I-L-L...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void startHtmlActivity(int resId, boolean isHtml, String title) {
        Intent intent = new Intent(this, DisplayTextFileActivity.class);
        intent.putExtra(DisplayTextFileActivity.KEY_RES_ID, resId);
        intent.putExtra(DisplayTextFileActivity.KEY_IS_HTML, isHtml);
        intent.putExtra(DisplayTextFileActivity.KEY_ACTIONBAR_TITLE, title);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

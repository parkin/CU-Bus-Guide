package com.teamparkin.mtdapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DisplayTextFileActivity extends ActionBarActivity {
    public static final String KEY_RES_ID = "resId";
    public static final String KEY_IS_HTML = "isHtml";
    public static final String KEY_ACTIONBAR_TITLE = "actionBarTitle";
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = DisplayTextFileActivity.class.getSimpleName();

    public static String readRawTextFile(Context context, int id) {
        InputStream inputStream = context.getResources().openRawResource(id);


        InputStreamReader in = new InputStreamReader(inputStream);
        BufferedReader buf = new BufferedReader(in);
        String line;


        StringBuilder text = new StringBuilder();
        try {
            while ((line = buf.readLine()) != null) text.append(line);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return text.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_text_file);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("File");

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(KEY_RES_ID)) {
            int resId = extras.getInt(KEY_RES_ID);
            boolean isHtml = extras.getBoolean(KEY_IS_HTML, false);

            String s = readRawTextFile(this, resId);

            TextView textView = (TextView) findViewById(R.id.activity_display_text_file_textView);

            if (isHtml) {
                textView.setText(Html.fromHtml(s));
                Linkify.addLinks(textView, Linkify.ALL);
            } else {
                textView.setText(s);
            }

            if (extras.containsKey(KEY_ACTIONBAR_TITLE)) {
                getSupportActionBar().setTitle(extras.getString(KEY_ACTIONBAR_TITLE));
            }
        }
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

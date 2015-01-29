package com.teamparkin.mtdapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DonateActivity extends ActionBarActivity {
    @SuppressWarnings("unused")
    private static final String TAG = DonateActivity.class.getSimpleName();
    private Spinner mSpinner;

    private ArrayAdapter<CharSequence> mSpinnerAdapter;

    private String[] mSkuArray;
    private IInAppBillingService mService;
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            // supportInvalidateOptionsMenu();

            new Thread() {

                @Override
                public void run() {
                    // Try consuming the purchases so the user will be able to
                    // donate again.
                    try {
                        Bundle ownedItems = mService.getPurchases(3,
                                getPackageName(), "inapp", null);

                        int response = ownedItems.getInt("RESPONSE_CODE");
                        if (response == 0) {
                            ArrayList<String> purchaseDataList = ownedItems
                                    .getStringArrayList("INAPP_PURCHASE_DATA_LIST");

                            if (purchaseDataList != null) {
                                for (int i = 0; i < purchaseDataList.size(); ++i) {
                                    String purchaseData = purchaseDataList
                                            .get(i);
                                    JSONObject json = new JSONObject(
                                            purchaseData);
                                    mService.consumePurchase(3,
                                            getPackageName(),
                                            json.getString("purchaseToken"));
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donate_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("Donate");

        // if (android.os.Build.VERSION.SDK_INT >=
        // android.os.Build.VERSION_CODES.HONEYCOMB) {
        // this.setFinishOnTouchOutside(false);
        // }
        Button openPlayStore = (Button) findViewById(R.id.donate_open_play_store);
        openPlayStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri
                        .parse("market://details?id=com.teamparkin.mtdapp"));
                startActivity(intent);
            }
        });

        Button mBuyButton = (Button) findViewById(R.id.donate_buy_button);
        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buy(mSkuArray[mSpinner.getSelectedItemPosition()]);
                // buy("android.test.purchased");
            }
        });

        mSpinner = (Spinner) findViewById(R.id.donate_spinner);
        mSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.donate_amounts, android.R.layout.simple_spinner_item);
        mSkuArray = getResources().getStringArray(R.array.donate_product_ids);
        // Specify the layout to use when the list of choices appears
        mSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setSelection(2);

        // start in-app billing stuff
        bindService(new Intent(
                        "com.android.vending.billing.InAppBillingService.BIND"),
                mServiceConn, Context.BIND_AUTO_CREATE
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Thank you for your support!",
                    Toast.LENGTH_LONG).show();

            new Thread() {

                @Override
                public void run() {
                    // Try consuming the purchases we've made so the user can
                    // donate again.
                    try {
                        JSONObject json = new JSONObject(
                                data.getStringExtra("INAPP_PURCHASE_DATA"));
                        mService.consumePurchase(3, getPackageName(),
                                json.getString("purchaseToken"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }.start();
        }
    }

    private void buy(String sku) {
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                    sku, "inapp", "bGoa+V7g/ysDXvKwqq+JTFn4uQZbPiQJo4pf9RzJ");
            PendingIntent pendingIntent = buyIntentBundle
                    .getParcelable("BUY_INTENT");
            if (pendingIntent != null) {
                startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), Integer.valueOf(0),
                        Integer.valueOf(0), Integer.valueOf(0));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (SendIntentException e) {
            e.printStackTrace();
        }
    }

}

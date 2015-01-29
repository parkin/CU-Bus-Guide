package com.teamparkin.mtdapp;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.teamparkin.mtdapp.databases.RoutesDatabase;
import com.teamparkin.mtdapp.restadapters.MTDAPIAdapter;

public class MtdGtfsService extends IntentService {
	private static final String TAG = MtdGtfsService.class.getSimpleName();

	private MTDAPIAdapter mMtdApiAdapter;

	public MtdGtfsService() {
		super("MtdGtfsService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mMtdApiAdapter = MTDAPIAdapter.getInstance(getApplicationContext());
		Bundle extras = intent.getExtras();
		Messenger messenger = null;
		String operation = "";
		if (extras != null) {
			messenger = (Messenger) extras.get("MESSENGER");
			operation = extras.getString("operation");
		} else {
			Log.e(TAG, "no extras passed to service");
			return;
		}
		boolean success = false;
		// Grab the operation we want the service to do
		if (operation.equals("initializeStops")) {
			success = initializeStops();
		} else if (operation.equals("initializeRoutes")) {
			success = initializeRoutes();
		} else if (operation.equals("updateStopsData")) {
			success = updateStopsData();
		} else if (operation.equals("updateRoutesData")) {
			success = updateRoutesData();
		} else {
			return;
		}
		Message msg = Message.obtain();
		msg.arg1 = Activity.RESULT_OK;
		if (success) {
			msg.obj = "success!";
		} else {
			msg.obj = "failed!";
		}
		try {
			messenger.send(msg);
		} catch (RemoteException e) {
			Log.e(TAG, "error sending message");
			e.printStackTrace();
		}
	}

	private boolean updateStopsData() {
		boolean success = mMtdApiAdapter
				.updateStopsFromGTFS(getApplicationContext());
		return success;
	}

	private boolean updateRoutesData() {
		boolean success = mMtdApiAdapter
				.updateRoutesFromGTFS(getApplicationContext());
		return success;
	}

	private boolean initializeStops() {
		boolean success = mMtdApiAdapter.downloadStops(getApplicationContext());
		return success;
	}

	private boolean initializeRoutes() {
		boolean success = mMtdApiAdapter.downloadRoutes(
				RoutesDatabase.getInstance(getApplicationContext()),
				getApplicationContext());
		return success;
	}
}

package com.emergency.button;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class Emergency {

	static Location location = null;
	static Locator locator = null;

	static long buttonPressedTime = 0;
	static long messageSentTime = 0;

	static String phoneNo;
	static String emailAddress;
	static String message;

	public static final String PREFS_NAME = "EmergencyPrefsFile";

	public static void restore(Context context) {
		// Restore preferences
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		Emergency.emailAddress = settings.getString("emailAddress", "");
		Emergency.phoneNo = settings.getString("phoneNo", "");
		Emergency.message = settings.getString("message", "");
	}

	public static void save(Context context) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("emailAddress", Emergency.emailAddress);
		editor.putString("message", Emergency.message);
		editor.putString("phoneNo", Emergency.phoneNo);

		// Commit the edits!
		editor.commit();

	}
}

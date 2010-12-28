package com.emergency.button;


import android.content.Context;
import android.content.SharedPreferences;

public class EmergencyData {
	String phoneNo;
	String emailAddress;
	String message;

	public static final String PREFS_NAME = "EmergencyPrefsFile";

	public EmergencyData(Context context) {
		// Restore preferences
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		this.emailAddress = settings.getString("emailAddress", "");
		this.phoneNo = settings.getString("phoneNo", "");
		this.message = settings.getString("message", "");
	}

	public void save(Context context) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("emailAddress", this.emailAddress);
		editor.putString("message", this.message);
		editor.putString("phoneNo", this.phoneNo);

		// Commit the edits!
		editor.commit();

	}
}

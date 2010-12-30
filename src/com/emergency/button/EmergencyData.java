package com.emergency.button;


import android.content.Context;
import android.content.SharedPreferences;

public class EmergencyData {
	
	Context context;
	SharedPreferences settings;

	private static final String PREFS_NAME = "EmergencyPrefsFile";
	private static final String EMAIL = "emailAddress";
	private static final String PHONE = "phoneNo";
	private static final String MESSAGE = "message";
	private static final String SEND_EMERGENCY = "sendEmergency";

	public EmergencyData(Context context) {
		this.context = context;
		this.settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	public String getEmail() {
		return settings.getString(EMAIL, "");
	}
	public String getPhone() {
		return settings.getString(PHONE, "");
	}
	public String getMessage() {
		return settings.getString(MESSAGE, "");
	}
	public boolean getArmEmergency() {
		return settings.getBoolean(SEND_EMERGENCY, false);
	}
	
	private void commitString(String id, String value) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(id, value);
		editor.commit();
	}		
	
	public void setEmail(String email) {
		commitString(EMAIL, email);
	}
	public void setPhone(String phone) {
		commitString(PHONE, phone);
	}
	public void setMessage(String message) {
		commitString(MESSAGE, message);
	}
	
	public void setArmEmergency(boolean sendMessage) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(SEND_EMERGENCY, sendMessage);
		editor.commit();
	}		
}

package com.emergency.button;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public class EmergencyData {
	
	Context context;
	SharedPreferences settings;

	public static final String PREFS_NAME = "EmergencyPrefsFile";
	public static final String EMAIL = "emailAddress";
	public static final String PHONE = "phoneNo";
	public static final String MESSAGE = "message";
	public static final String SEND_EMERGENCY = "sendEmergency";

	public EmergencyData(Context context) {
		this.context = context;
		this.settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	private List<String> getList(String id) {
		List<String> stringsList = new ArrayList<String>();
		String lastString;
		lastString = settings.getString(id, "");
		int i = 1;
		while (lastString != "") {
			stringsList.add(lastString);
			// NOTE: the first item is just "id" and then next is "id1"
			//		this is for backwards compatibility, oh well...
			lastString = settings.getString(id + i, "");
			i += 1;
		}
		
		return stringsList;
	}
	
	private void commitList(String id, List<String> stringList) {
		SharedPreferences.Editor editor = settings.edit();
		for (int i = 0; i < stringList.size(); i++) {
			String idName;
			if (i > 0) {
				idName = id + i;
			} else {
				idName = id;
			}
			editor.putString(idName, stringList.get(i));
		}
		
		if (stringList.size() > 0) {
			editor.commit();
		}
	}
	
	public List<String> getEmails() {
		return getList(EMAIL);
	}
	public List<String> getPhones() {
		return getList(PHONE);
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
	
	public void setEmails(List<String> emails) {
		commitList(EMAIL, emails);
	}
	public void setPhones(List<String> phones) {
		commitList(PHONE, phones);
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

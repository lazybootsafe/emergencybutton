package com.emergency.button;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public class EmergencyData {
	
	Context mContext;
	SharedPreferences mSettings;

	public static final String PREFS_NAME = "EmergencyPrefsFile";
	public static final String EMAIL = "emailAddress";
	public static final String PHONE = "phoneNo";
	public static final String MESSAGE = "message";
	public static final String SEND_EMERGENCY = "sendEmergency";
	
	private static final String COUNT_SUFFIX = "Count";

	public EmergencyData(Context context) {
		this.mContext = context;
		this.mSettings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	private int getCount(String id) {
		return mSettings.getInt(id + COUNT_SUFFIX, 1);
	}
	
	private void setCount(SharedPreferences.Editor editor, String id, int count) {
		editor.putInt(id + COUNT_SUFFIX, count);
	}
	
	private List<String> getList(String id) {
		// NOTE: this function should always return a list with at least one
		//		string.
		List<String> stringsList = new ArrayList<String>();
		String lastString;
		lastString = mSettings.getString(id, "");
		stringsList.add(lastString);
		
		int stringsCount = getCount(id);
		for(int i = 1; i < stringsCount; i++) {
			// NOTE: the first item is just "id" and then next is "id1"
			//		this is for backwards compatibility, oh well...
			lastString = mSettings.getString(id + i, "");
			stringsList.add(lastString);
		}
		
		return stringsList;
	}
	
	private void commitList(String id, List<String> stringList) {
		SharedPreferences.Editor editor = mSettings.edit();
		int previousCount = getCount(id);
		int i = 0;
		for (; i < stringList.size(); i++) {
			String idName;
			if (i > 0) {
				idName = id + i;
			} else {
				idName = id;
			}
			editor.putString(idName, stringList.get(i));
		}
		
		for(; i < previousCount; i++) {
			// remove older irrelevant strings
			editor.remove(id + i);
		}
		
		setCount(editor, id, stringList.size());
		editor.commit();
	}
	
	public List<String> getEmails() {
		return getList(EMAIL);
	}
	public List<String> getPhones() {
		return getList(PHONE);
	}
	public String getMessage() {
		return mSettings.getString(MESSAGE, "");
	}
	public boolean getArmEmergency() {
		return mSettings.getBoolean(SEND_EMERGENCY, false);
	}
	
	private void commitString(String id, String value) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences.Editor editor = mSettings.edit();
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
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putBoolean(SEND_EMERGENCY, sendMessage);
		editor.commit();
	}		
}

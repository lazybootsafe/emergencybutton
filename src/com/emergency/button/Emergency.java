package com.emergency.button;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
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
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		Emergency.emailAddress = settings.getString("emailAddress", "");
		Emergency.phoneNo = settings.getString("phoneNo", "");
		Emergency.message = settings.getString("message", "");
	}

	public static void save(Context context) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("emailAddress", Emergency.emailAddress);
		editor.putString("message", Emergency.message);
		editor.putString("phoneNo", Emergency.phoneNo);

		// Commit the edits!
		editor.commit();

	}

	private static void sendMessages(final Context context) {

		// Toast.makeText(context, "123", Toast.LENGTH_SHORT).show();
		Toast.makeText(context, "Sending Emergency Message", Toast.LENGTH_SHORT)
				.show();

		// // TODO: maybe still send the distress signal after a while without a
		// // location?
		// if (Emergency.location == null) {
		// Toast.makeText(context, "waiting for location update",
		// Toast.LENGTH_SHORT).show();
		// return false;
		// } else {
		// Toast.makeText(context, "sending distress signal",
		// Toast.LENGTH_SHORT).show();
		// }

		// these operations are going to block a bit so run them on another
		// thread that won't interfere with the GUI. That way the user
		// can see the toasts. (this was a really hard bug to find btw)
		Thread t = new Thread() {
			public void run() {
				// mResults = doSomethingExpensive();
				// mHandler.post(mUpdateResults);
				// Do it, send all the stuff!

				String locString = Double.toString(Emergency.location
						.getLatitude())
						+ ","
						+ Double.toString(Emergency.location.getLongitude());
				String fullMessage = Emergency.message + " " + locString;
				String mapsUrl = "http://maps.google.com/maps?q=" + locString;

				if (Emergency.phoneNo.length() > 0) {
					// SMSSender.sendSMS(EmergencyButton.this, phoneNo,
					// message);
					SMSSender.safeSendSMS(context, Emergency.phoneNo,
							fullMessage);
				}

				if (Emergency.emailAddress.length() > 0) {
					boolean success = EmailSender.send(Emergency.emailAddress, fullMessage + " "
							+ mapsUrl);
					if(!success) {
						Toast.makeText(context, "Failed sending email",	Toast.LENGTH_SHORT).show();
					}
				}
			}
		};
		t.start();

	}

	public static void emergencyNow(final Context context) {
		Log.v("Emergency", "emergencyNow");

		// TODO: This buttonPressedTime / messageSentTime is a bad way of going
		// about
		// concurrency. Maybe do this some other way or with a lock?

		// The button was pressed now.
		Emergency.buttonPressedTime = SystemClock.elapsedRealtime();

		Toast.makeText(context, "waiting for location update",
				Toast.LENGTH_SHORT).show();

		if (Emergency.locator != null) {
			// no need to reinitialize the locator, note there's a race
			// condition here
			// TODO: lock or not?
			return;
		}

		Emergency.locator = new Locator(context,
				new Locator.BetterLocationListener() {
					public void onBetterLocation(Location location) {
						Log.v("Emergency", "got a location");
						Emergency.location = location;

						// messageSentTime is either 0 or the last time a
						// message was sent.
						// so if the button was pressed more recently, fire a
						// message.
						if (Emergency.messageSentTime < Emergency.buttonPressedTime) {
							Emergency.messageSentTime = SystemClock
									.elapsedRealtime();
							Emergency.sendMessages(context);
							Emergency.locator.unregister();
							Emergency.locator = null;
						}
					}
				});

	}
}

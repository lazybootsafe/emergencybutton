package com.emergency.button;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.View;

public class EmergencyButton extends Activity {

	Location location = null;
	Boolean buttonPressed = false;

	String phoneNo;
	String emailAddress;
	String message;

	public static final String PREFS_NAME = "EmergencyPrefsFile";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Locator.register(this, new Locator.BetterLocationListener() {
			public void onBetterLocation(Location location) {
				EmergencyButton.this.location = location;
				if (EmergencyButton.this.buttonPressed) {
					EmergencyButton.this.sendEmergencyMessage();
				}
			}
		});

		// TextView tv = new TextView(this);
		// tv.setText("Hello, Android");

		setContentView(R.layout.main);
		// setContentView(tv);

		this.restoreTextEdits();

		ImageButton btnEmergency = (ImageButton) findViewById(R.id.btnEmergency);
		;
		btnEmergency.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EmergencyButton.this.buttonPressed = true;

				// try sending the message:
				EmergencyButton.this.sendEmergencyMessage();
			}
		});

	}

	public void restoreTextEdits() {
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		this.emailAddress = settings.getString("emailAddress", "");
		this.phoneNo = settings.getString("phoneNo", "");
		this.message = settings.getString("message", "");

		txtPhoneNo.setText(this.phoneNo);
		txtEmail.setText(this.emailAddress);
		txtMessage.setText(this.message);
	}

	public void updateTextEdits() {
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		this.phoneNo = txtPhoneNo.getText().toString();
		this.emailAddress = txtEmail.getText().toString();
		this.message = txtMessage.getText().toString();
	}

	public void sendEmergencyMessage() {
		this.updateTextEdits();

		if ((phoneNo.length() == 0) && (emailAddress.length() == 0)) {
			Toast.makeText(getBaseContext(), "Enter a phone number or email.",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// TODO: maybe still send the distress signal after a while without a
		// location?
		if (location == null) {
			Toast.makeText(getBaseContext(), "waiting for location update",
					Toast.LENGTH_SHORT).show();
			return;
		} else {
			Toast.makeText(getBaseContext(), "sending message",
					Toast.LENGTH_SHORT).show();
		}

		// Do it, send all the stuff!

		// TODO: This buttonPressed flag is a bad way of going about the
		// concurrency.
		this.buttonPressed = false;

		String locString = Double.toString(this.location.getLatitude()) + ","
				+ Double.toString(this.location.getLongitude());
		String fullMessage = message + " " + locString;
		String mapsUrl = "http://maps.google.com/maps?q=" + locString;

		if (phoneNo.length() > 0) {
			// SMSSender.sendSMS(EmergencyButton.this, phoneNo, message);
			SMSSender.safeSendSMS(EmergencyButton.this, phoneNo, fullMessage);
		}

		if (emailAddress.length() > 0) {
			EmailSender.send(emailAddress, fullMessage + " " + mapsUrl);
		}

	}

	public void saveTextEdits() {
		updateTextEdits();

		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("emailAddress", this.emailAddress);
		editor.putString("message", this.message);
		editor.putString("phoneNo", this.phoneNo);

		// Commit the edits!
		editor.commit();
	}

	@Override
	protected void onStop() {
		super.onStop();

		this.saveTextEdits();

		// stop getting location updates
		Locator.unregister();
	}
}

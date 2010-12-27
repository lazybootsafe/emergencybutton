package com.emergency.button;

import com.emergency.button.SMSSender.SMSListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EmergencyActivity extends Activity {

	Location location = null;
	Locator locator = null;
	
	public long buttonPressedTime = 0;
	public long messageSentTime = 0;
	
	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		setContentView(R.layout.emergency_activity_layout);

		Button btnOk = (Button) findViewById(R.id.btnOkDone);
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EmergencyActivity.this.finish();
			}
		});
		
		Button btnConfigure = (Button) findViewById(R.id.btnConfigure);
		btnConfigure.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(EmergencyActivity.this, EmergencyButton.class);
				EmergencyActivity.this.startActivity(myIntent);
			}
		});

		this.setLocationState("Waiting For Location");
		this.setEmailState("...");
		this.setSMSState("...");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		this.emergencyNow();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
	}

	protected void setLocationState(String state) {
		TextView txt = (TextView)findViewById(R.id.txtLocation);
		txt.setText(state);			
	}

	protected void setSMSState(String state) {
		TextView txt = (TextView)findViewById(R.id.txtSMS);
		txt.setText(state);
	}

	protected void setEmailState(String state) {
		TextView txt = (TextView)findViewById(R.id.txtEmail);
		txt.setText(state);
	}

	public void emergencyNow() {
		Log.v("Emergency", "emergencyNow");
		
		final Context context = this;

		// TODO: This buttonPressedTime and messageSentTime is a bad way of going
		// about concurrency. Maybe do this some other way or with a lock?

		// The button was pressed now.
		this.buttonPressedTime = SystemClock.elapsedRealtime();

		if (this.locator != null) {
			// no need to reinitialize the locator, note there's a race
			// condition here
			// TODO: lock or not?
			return;
		}

		// TODO: maybe still send the distress signal after a while without a
		// location?
		this.locator = new Locator(context,	new EmergencyLocator());
	}
	
	private class EmergencyLocator implements Locator.BetterLocationListener {
		public void onBetterLocation(Location location) {
			Log.v("Emergency", "got a location");
			EmergencyActivity.this.setLocationState("Location found");
			EmergencyActivity.this.location = location;

			// messageSentTime is either 0 or the last time a
			// message was sent.
			// so if the button was pressed more recently, fire a
			// message.
			if (EmergencyActivity.this.messageSentTime < EmergencyActivity.this.buttonPressedTime) {
				try {
					EmergencyActivity.this.messageSentTime = SystemClock.elapsedRealtime();
					EmergencyActivity.this.sendMessages();
					EmergencyActivity.this.locator.unregister();
				} finally {
					EmergencyActivity.this.locator = null;
				}
			}
		}
	}
	
	private void sendMessages() {

		// these operations are going to block a bit so run them on another
		// thread that won't interfere with the GUI. That way the user
		// can see the toasts. (this was a really hard bug to find btw)
		Thread t = new EmergencyThread(this.handler);
		t.start();
	}

	private class EmergencyThread extends Thread {
		Handler handler;
		
		EmergencyThread(Handler handler) {
			EmergencyThread.this.handler = handler;
		}
		
		public void run() {
			final Context context = EmergencyActivity.this;
			// make sure all the fields are fresh and not null
			Emergency emergency = new Emergency(context);

			
			// mResults = doSomethingExpensive();
			// mHandler.post(mUpdateResults);
			// Do it, send all the stuff!

			String locString = Double
					.toString(EmergencyActivity.this.location.getLatitude())
					+ ","
					+ Double.toString(EmergencyActivity.this.location.getLongitude());
			
			String textMessage = emergency.message + " " + locString;
			String mapsUrl = "http://maps.google.com/maps?q=" + locString;
			String emailMessage = emergency.message + "\n" + mapsUrl;

			if (emergency.phoneNo.length() > 0) {
				this.setSMSState("sending sms");
				// SMSSender.sendSMS(EmergencyButton.this, phoneNo,
				// message);
				SMSListener smsListener = new SMSListener() {
					public void onStatusUpdate(int resultCode,
							String resultString) {
						EmergencyThread.this.setSMSState(resultString);
					}
				};
				SMSSender.safeSendSMS(context, emergency.phoneNo, textMessage,
						smsListener);
			} else {
				this.setSMSState("No phone number configured, not sending SMS.");
			}

			// TODO: maybe this is null?
			if (emergency.emailAddress.length() > 0) {
				this.setEmailState("Sending email");
				
				boolean success = EmailSender.send(emergency.emailAddress,
						emailMessage);
				if (success) {
					setEmailState("Email sent");
				} else {
					// Toast.makeText(context, "Failed sending email",
					// Toast.LENGTH_SHORT).show();
					setEmailState("Failed sending email");
				}
			} else {
				this.setEmailState("No email configured, not sending email.");
			}
		}
		
		protected void setSMSState(String state) {
			//TextView txt = (TextView)findViewById(R.id.txtSMS);
			//txt.setText(state);
			sendToHandler(R.id.txtSMS, state);
		}

		protected void setEmailState(String state) {
			sendToHandler(R.id.txtEmail, state);
		}
		
		protected void sendToHandler(int fieldId, String state) {
			Message msg = this.handler.obtainMessage();
	        Bundle b = new Bundle();
	        b.putInt("fieldId", fieldId);
	        b.putString("state", state);
	        msg.setData(b);
	        this.handler.sendMessage(msg);
		}		
	}
	
	// Define the Handler that receives messages from the thread and update the progress
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String state = msg.getData().getString("state");
			int fieldId = msg.getData().getInt("fieldId");
			TextView txt = (TextView) findViewById(fieldId);
			txt.setText(state);
		}
	};
}

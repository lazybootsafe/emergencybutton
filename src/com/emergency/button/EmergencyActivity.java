package com.emergency.button;

import java.util.concurrent.atomic.AtomicBoolean;

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
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EmergencyActivity extends Activity {

	Location location = null;
	Locator locator = null;
	
	public long buttonPressedTime = 0;
	public long messageSentTime = 0;
	
	private final static int STATE_X_OR_V = 0;
	private final static int STATE_V = 1;
	private final static int STATE_X = 2;
	
	private AtomicBoolean isSending = new AtomicBoolean(false);
	
	String locationString = "";
	String smsString = "";
	String emailString = "";
	
	int locationState = STATE_X_OR_V;
	int smsState = STATE_X_OR_V;
	int emailState = STATE_X_OR_V;
	
	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //setContentView(R.layout.progressbar_4);
        
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

		this.resetState();
		updateGUI();
	}
	
	private void updateTextField(int id, String text) {
		TextView txt = (TextView)findViewById(id);
		txt.setText(text);
	}
	
	private void updateXV(int id, int state) {
		ImageView img = (ImageView)findViewById(id);
		switch (state) {
		case STATE_X:
			img.setImageResource(R.drawable.x);
			break;
		case STATE_V:
			img.setImageResource(R.drawable.v);
			break;
		case STATE_X_OR_V:
			img.setImageResource(R.drawable.x_or_v);
			break;
		}
	}
	
	protected void updateGUI() {
		runOnUiThread(new Runnable() {
		    public void run() {
				EmergencyActivity emthis = EmergencyActivity.this;
		    	updateTextField(R.id.txtLocation, emthis.locationString);
				updateTextField(R.id.txtSMS, emthis.smsString);
				updateTextField(R.id.txtEmail, emthis.emailString);
				
				// TODO: rename these variables for consistency
				updateXV(R.id.imgLocation, emthis.locationState);
				updateXV(R.id.imgSMS, emthis.smsState);
				updateXV(R.id.imgEmail, emthis.emailState);

				emthis.setProgressBarIndeterminateVisibility( emthis.isSending.get());
		    }
		});
	}
	
	protected void resetState() {
		this.locationString = "Waiting For Location";
		this.locationState = STATE_X_OR_V;
		this.smsString = "...";
		this.smsState = STATE_X_OR_V;
		this.emailString = "...";
		this.emailState = STATE_X_OR_V;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		this.emergencyNow();
		this.updateGUI();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	
	@Override
	protected void onDestroy() {
		Log.v("Emergency", "destroy emergency send activity " + isFinishing());
		super.onDestroy();
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
		double myDouble = savedInstanceState.getDouble("myDouble");
		int myInt = savedInstanceState.getInt("MyInt");
		String myString = savedInstanceState.getString("MyString");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		savedInstanceState.putBoolean("MyBoolean", true);
		
		super.onSaveInstanceState(savedInstanceState);
	}	

	public void emergencyNow() {
		Log.v("Emergency", "emergencyNow");
		
		final Context context = this;

		// The button was pressed now.
		this.buttonPressedTime = SystemClock.elapsedRealtime();

		// TODO: maybe still send the distress signal after a while without a
		// location?
		if (this.isSending.compareAndSet(false, true)) {
			// got the lock, send out the message!
			EmergencyActivity.this.messageSentTime = SystemClock.elapsedRealtime();
			this.resetState();
			if (this.locator != null) {
				Log.e("EmergencyActivity", "locator exists while lock is open");
			}
			this.locator = new Locator(context,	new EmergencyLocator());
		} else {
			Toast.makeText(this, "Already sending a message.",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private class EmergencyLocator implements Locator.BetterLocationListener {
		public void onGoodLocation(Location location) {
			Log.v("Emergency", "got a location");
			EmergencyActivity.this.locationString = "Location found";
			EmergencyActivity.this.locationState = STATE_V;
			EmergencyActivity.this.updateGUI();
			EmergencyActivity.this.location = location;

			// messageSentTime is either 0 or the last time a
			// message was sent.
			// so if the button was pressed more recently, fire a
			// message.
			try {
				EmergencyActivity.this.startMessagesThread();
				EmergencyActivity.this.locator.unregister();
			} finally {
				EmergencyActivity.this.locator = null;
				// let the messages thread do this: EmergencyActivity.this.isSending.set(false);
			}
		}
	}
	
	private void startMessagesThread() {

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
			try {
				this.sendMessages();
			} finally {
				EmergencyActivity.this.isSending.set(false);
				//sendUpdateGui();
				updateGUI();
			}
		}
		
		private void sendSMS(Context context, String phoneNo, String textMessage) {
			if (phoneNo.length() > 0) {
				this.setSMSState("Sending sms");
				// SMSSender.sendSMS(EmergencyButton.this, phoneNo,
				// message);
				SMSListener smsListener = new SMSListener() {
					public void onStatusUpdate(int resultCode,
							String resultString) {
						
						EmergencyActivity.this.smsString = resultString;
						if (resultCode != Activity.RESULT_OK) {
							EmergencyActivity.this.smsState = STATE_X;
						}
						if (resultString.equals("SMS delivered")) {
							EmergencyActivity.this.smsState = STATE_V;
						}
						//sendUpdateGui();
						updateGUI();
					}
				};
				SMSSender.safeSendSMS(context, phoneNo, textMessage,
						smsListener);
			} else {
				this.setSMSState("No phone number configured, not sending SMS.");
				EmergencyActivity.this.smsState = STATE_X;
			}
			//sendUpdateGui();
			updateGUI();
			
		}
		
		private void sendEmail(String emailAddress, String emailMessage) {
			if (emailAddress.length() > 0) {
				this.setEmailState("Sending email");
				//sendUpdateGui();
				updateGUI();
				
				boolean success = EmailSender.send(emailAddress,
						emailMessage);
				if (success) {
					setEmailState("Email sent");
					EmergencyActivity.this.emailState = STATE_V;
				} else {
					setEmailState("Failed sending email");
					EmergencyActivity.this.emailState = STATE_X;
				}
			} else {
				this.setEmailState("No email configured, not sending email.");
				EmergencyActivity.this.emailState = STATE_X;
			}
			
			//sendUpdateGui();
			updateGUI();
			
		}
		
		private void sendMessages() {
			
			final Context context = EmergencyActivity.this;
			// make sure all the fields are fresh and not null
			EmergencyData emergency = new EmergencyData(context);

			
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

			this.sendSMS(context, emergency.phoneNo, textMessage);
			this.sendEmail(emergency.emailAddress, emailMessage);
		}
		
		protected void sendUpdateGui() {
			sendToHandler(0, "");
		}
		
		protected void setSMSState(String state) {
			//TextView txt = (TextView)findViewById(R.id.txtSMS);
			//txt.setText(state);
			//sendToHandler(R.id.txtSMS, state);
			EmergencyActivity.this.smsString = state;
		}

		protected void setEmailState(String state) {
			//sendToHandler(R.id.txtEmail, state);
			EmergencyActivity.this.emailString = state;
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
			if (state.equals("") && (fieldId == 0)) {
				EmergencyActivity.this.updateGUI();
				return;
			}
			TextView txt = (TextView) findViewById(fieldId);
			txt.setText(state);
		}
	};
}

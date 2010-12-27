package com.emergency.button;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.View;

public class EmergencyButton extends Activity {
	static final int EMERGENCY_DIALOG = 0;

	Emergency emergency;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TextView tv = new TextView(this);
		// tv.setText("Hello, Android");

		setContentView(R.layout.main);
		// setContentView(tv);

		this.restoreTextEdits();

		ImageButton btnEmergency = (ImageButton) findViewById(R.id.btnEmergency);
		
		btnEmergency.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// try sending the message:
				EmergencyButton.this.redButtonPressed();
			}
		});

	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();

		this.saveTextEdits();
		//this.finish();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}	

	public void restoreTextEdits() {
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		this.emergency = new Emergency(this);
		
		txtPhoneNo.setText(emergency.phoneNo);
		txtEmail.setText(emergency.emailAddress);
		txtMessage.setText(emergency.message);
	}

	public void saveTextEdits() {
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		emergency.phoneNo = txtPhoneNo.getText().toString();
		emergency.emailAddress = txtEmail.getText().toString();
		emergency.message = txtMessage.getText().toString();
		
		emergency.save(this);
	}

	public void redButtonPressed() {
		this.saveTextEdits();

		// TODO: maybe this is null?
		if ((this.emergency.phoneNo.length() == 0) && (this.emergency.emailAddress.length() == 0)) {
			Toast.makeText(this, "Enter a phone number or email.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		//Emergency.emergencyNow(this);
		//ProgressDialog dialog = ProgressDialog.show(this, "Sending Emergency Message", "Loading. Please wait...", true);
		Intent myIntent = new Intent(EmergencyButton.this, EmergencyActivity.class);
		EmergencyButton.this.startActivity(myIntent);
	}
    	
}

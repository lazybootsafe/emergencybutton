package com.emergency.button;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;
import android.view.View;

public class EmergencyButton extends Activity {
	static final int EMERGENCY_DIALOG = 0;


	
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

		this.updateTextEdits();
		this.finish();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}	

	public void restoreTextEdits() {
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		Emergency.restore(this);
		
		txtPhoneNo.setText(Emergency.phoneNo);
		txtEmail.setText(Emergency.emailAddress);
		txtMessage.setText(Emergency.message);
	}

	public void updateTextEdits() {
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		Emergency.phoneNo = txtPhoneNo.getText().toString();
		Emergency.emailAddress = txtEmail.getText().toString();
		Emergency.message = txtMessage.getText().toString();
		
		Emergency.save(this);
	}

	public void redButtonPressed() {
		this.updateTextEdits();

		// TODO: maybe this is null?
		if ((Emergency.phoneNo.length() == 0) && (Emergency.emailAddress.length() == 0)) {
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

package com.emergency.button;

import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.View;

public class EmergencyButton extends Activity {

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ExceptionHandler.register(this, new StackMailer());
		
		setContentView(R.layout.main);

		this.restoreTextEdits();

		ImageButton btnEmergency = (ImageButton) findViewById(R.id.btnEmergency);
		
		btnEmergency.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// try sending the message:
				EmergencyButton.this.redButtonPressed();
			}
		});

		ImageButton btnHelp = (ImageButton) findViewById(R.id.btnHelp);
		
		btnHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				IntroActivity.open(EmergencyButton.this);
			}
		});

	}
	
	private class StackMailer implements ExceptionHandler.StackTraceHandler {
		public void onStackTrace(String stackTrace) {
			EmailSender.send("admin@andluck.com", "EmergencyButtonError\n" + stackTrace);
		}
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	}


	@Override
	protected void onResume()
	{
		super.onResume();
		IntroActivity.openOnceAfterInstallation(this);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();

		this.saveTextEdits();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}	

	public void restoreTextEdits() {
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		EmergencyData emergency = new EmergencyData(this);
		
		txtPhoneNo.setText(emergency.getPhone());
		txtEmail.setText(emergency.getEmail());
		txtMessage.setText(emergency.getMessage());
	}

	public void saveTextEdits() {
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		EmergencyData emergency = new EmergencyData(this);
		emergency.setPhone(txtPhoneNo.getText().toString());
		emergency.setEmail(txtEmail.getText().toString());
		emergency.setMessage(txtMessage.getText().toString());
	}

	public void redButtonPressed() {

		this.saveTextEdits();
		EmergencyData emergency = new EmergencyData(this);

		// TODO: maybe this is null?
		if ((emergency.getPhone().length() == 0) && (emergency.getEmail().length() == 0)) {
			Toast.makeText(this, "Enter a phone number or email.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		EmergencyActivity.armEmergencyActivity(this);
		Intent myIntent = new Intent(EmergencyButton.this, EmergencyActivity.class);
		EmergencyButton.this.startActivity(myIntent);
	}
    	
}

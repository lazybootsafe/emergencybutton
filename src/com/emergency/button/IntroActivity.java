package com.emergency.button;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class IntroActivity extends Activity {
	final String messages [] = {
			"Welcome to Emergency Button, enter a phone number, email and message. They'll be saved for an emergency.",
			"When you press the emergency button, or both widget buttons within 5 seconds, the distress signal is sent.",
			"Add the widget at your home screen using:\nMenu->Add->Widgets->Emergency Button"
		};
	
	int currentSlide = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intro_activity_layout);
		
		Button btnNext = (Button) findViewById(R.id.btnNext);
		
		btnNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				IntroActivity.this.next();
			}
		});
		
		this.updateSlide();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	public void next() {
		currentSlide += 1;
		if (currentSlide >= messages.length) {
			currentSlide = 0;
			this.finish();
			return;
		}
		
		this.updateSlide();
	}
	
	public void updateSlide() {
		final TextView txt = (TextView) findViewById(R.id.txtExplain);

		txt.setText(messages[currentSlide]);
	}
	
	public static void openOnceAfterInstallation(Context context) {
		final String wasOpenedName = "wasOpened";
		final String introDbName = "introActivityState";
		SharedPreferences settings = context.getSharedPreferences(introDbName, Context.MODE_PRIVATE);
		boolean wasOpened = settings.getBoolean(wasOpenedName, false);
		
		if (wasOpened) {
			return;
		}
		
		// mark that it was opened once
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(wasOpenedName, true);
		editor.commit();
		
		IntroActivity.open(context);
	}
	
	public static void open(Context context) {
		Intent myIntent = new Intent(context, IntroActivity.class);
		context.startActivity(myIntent);
	}
}



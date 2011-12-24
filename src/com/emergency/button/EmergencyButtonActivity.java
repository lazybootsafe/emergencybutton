package com.emergency.button;

import java.util.ArrayList;
import java.util.List;

import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class EmergencyButtonActivity extends Activity {

	static private MoreEditText mPhonesMoreEditText = null;
	static private MoreEditText mEmailsMoreEditText = null;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ExceptionHandler.register(this, new StackMailer());
		
	}

	private void initUI() {
		setContentView(R.layout.main);

		this.restoreTextEdits();

		ImageButton btnEmergency = (ImageButton) findViewById(R.id.btnEmergency);
		btnEmergency.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// try sending the message:
				EmergencyButtonActivity.this.redButtonPressed();
			}
		});

		ImageButton btnHelp = (ImageButton) findViewById(R.id.btnHelp);
		btnHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				IntroActivity.open(EmergencyButtonActivity.this);
			}
		});


		/*Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);  
		startActivityForResult(intent, CONTACT_PICKER_RESULT);*/
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    initUI();
	}
	
	
	private class StackMailer implements ExceptionHandler.StackTraceHandler {
		public void onStackTrace(String stackTrace) {
			EmailSender.send("admin@andluck.com", "EmergencyButtonError", "EmergencyButtonError\n" + stackTrace);
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
		initUI();
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

	/*
	public void setPhoneNum(String phoneNumber) {
		// gui
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		txtPhoneNo.setText(phoneNumber);
		
		// save
		EmergencyData emergency = new EmergencyData(this);
		emergency.setPhone(txtPhoneNo.getText().toString());
	}*/

	private class EditTextRow {
		LinearLayout mLinlay;
		EditText mEditText;
		ImageButton mRemoveBtn;
		
		public EditTextRow(String text, EditText example) {
			mEditText = new EditText(EmergencyButtonActivity.this);
			// set weight so the button is only as big as it needs to contain the image.
			//mEditText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
			mEditText.setLayoutParams(example.getLayoutParams());
			mEditText.setText(text);
			//mEditText.setInputType(InputType.TYPE_CLASS_PHONE);
			mEditText.setInputType(example.getInputType());
			
			mRemoveBtn = new ImageButton(EmergencyButtonActivity.this);
			mRemoveBtn.setBackgroundResource(R.drawable.grey_x);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			mRemoveBtn.setLayoutParams(params);
			
			mLinlay = new LinearLayout(EmergencyButtonActivity.this);
			mLinlay.setOrientation(LinearLayout.HORIZONTAL);
			mLinlay.addView(mEditText);
			mLinlay.addView(mRemoveBtn);
		}
	}
	
	private class MoreEditText {
		private LinearLayout mContainer;
		private ArrayList<EditText> mEditTextList = null;
		
		public MoreEditText(LinearLayout container, List<String> stringsList) {
			// Create the rows from scratch, this should only happen onCreate
			
			mContainer = container;
			mEditTextList = new ArrayList<EditText>();
			//txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
			EditText edit;
			edit = getDefaultTextEdit(container);
			if(! stringsList.isEmpty()) {
				edit.setText(stringsList.get(0));
			}
			mEditTextList.add(edit);
			for (int i = 1; i < stringsList.size(); i++) {
				addRow(stringsList.get(i));
			}
		}
		
		public void restore(LinearLayout container, List<String> stringsList) {
			// Create the rows from older existing rows, this can happen on
			// changes of orientation, onResume, etc
			mContainer = container;
			
			for(int i = 0; i < mEditTextList.size(); i++) {
				EditText edit;
				if (i == 0) {
					// the first row is the default one (with the "+")
					edit = getDefaultTextEdit(container);
					mEditTextList.set(0, edit);
					if (stringsList.size() > 0) {
						edit.setText(stringsList.get(0));
					}
				} else {
					edit = mEditTextList.get(i);
					View viewRow = (LinearLayout) edit.getParent();
					((LinearLayout)viewRow.getParent()).removeView(viewRow);
					mContainer.addView(viewRow);
				}
				
			}
		}
		
		public EditText getDefaultTextEdit(LinearLayout container) {
			// TODO: turn this into something like "getEditTextChild" rather than counting on the index "0"
			return (EditText) ((LinearLayout)container.getChildAt(0)).getChildAt(0);
			
		}
		
		public void removeRow(EditText editText) {
			mContainer.removeView((View) editText.getParent());
			mEditTextList.remove(editText);
		}
		
		public void addRow(String text) {
			final EditTextRow editRow = new EditTextRow(text, mEditTextList.get(0));
			editRow.mRemoveBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					MoreEditText.this.removeRow(editRow.mEditText);
				}
			});
			
			mContainer.addView(editRow.mLinlay);
			mEditTextList.add(editRow.mEditText);
		}
		
		public List<String> GetTexts() {
			ArrayList<String> texts = new ArrayList<String>(); 
			for (int i = 0; i < mEditTextList.size(); i ++) {
				texts.add(mEditTextList.get(i).getText().toString());
			}
			
			return texts;
		}


	}
	
	private void addPhonesEmailsUI(List<String> phones, List<String> emails) {
		LinearLayout phoneNoLin = (LinearLayout)findViewById(R.id.linPhoneNo);
		LinearLayout emailLin = (LinearLayout)findViewById(R.id.linEmail);
		// NOTE: we don't always create from scratch so that empty textboxes
		//		aren't erased on changes of orientation.
		if (mPhonesMoreEditText == null) {
			mPhonesMoreEditText = new MoreEditText(phoneNoLin, phones);
			mEmailsMoreEditText = new MoreEditText(emailLin, emails);
		} else {
			mPhonesMoreEditText.restore(phoneNoLin, phones);
			mEmailsMoreEditText.restore(emailLin, emails);
		}
		
		// register the Plus buttons
		ImageButton btnPhoneNoPlus = (ImageButton) findViewById(R.id.btnPhoneNoPlus);
		btnPhoneNoPlus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mPhonesMoreEditText.addRow("");
			}
		});
		
		ImageButton btnEmailPlus = (ImageButton) findViewById(R.id.btnEmailPlus);
		btnEmailPlus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mEmailsMoreEditText.addRow("");
			}
		});
	}
	

	public void restoreTextEdits() {
		EmergencyData emergencyData = new EmergencyData(this);
		
		addPhonesEmailsUI(emergencyData.getPhones(), emergencyData.getEmails());
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		txtMessage.setText(emergencyData.getMessage());
	}

	public void saveTextEdits() {
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
		EditText txtEmail = (EditText) findViewById(R.id.txtEmail);

		EmergencyData emergencyData = new EmergencyData(this);
		emergencyData.setPhones(mPhonesMoreEditText.GetTexts());
		emergencyData.setEmails(mEmailsMoreEditText.GetTexts());
		//emergencyData(txtPhoneNo.getText().toString());
		//emergencyData(txtEmail.getText().toString());
		emergencyData.setMessage(txtMessage.getText().toString());
	}

	public void redButtonPressed() {
		this.saveTextEdits();
		EmergencyData emergency = new EmergencyData(this);

		if ((emergency.getPhones().size() == 0) && (emergency.getEmails().size() == 0)) {
			Toast.makeText(this, "Enter a phone number or email.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		EmergencyActivity.armEmergencyActivity(this);
		Intent myIntent = new Intent(EmergencyButtonActivity.this, EmergencyActivity.class);
		EmergencyButtonActivity.this.startActivity(myIntent);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.ebutton_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = new Intent(Intent.ACTION_VIEW);  
		switch (item.getItemId()) {
		case R.id.project_page:
			i.setData(Uri.parse("http://www.andluck.com/"));  
			startActivity(i);
			break;
			
		case R.id.credits:
			i.setData(Uri.parse("http://code.google.com/p/emergencybutton/source/browse/trunk/credits.txt"));  
			startActivity(i);
			break;
		//case R.id.recalibrate_noise:
		//	pd_.resetNoiseLevel();
		//	break;
		}
		return true;
	}
		
}

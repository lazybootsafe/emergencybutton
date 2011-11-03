package com.emergency.button;

import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class EmergencyButtonActivity extends Activity {

	
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
				EmergencyButtonActivity.this.redButtonPressed();
			}
		});

		ImageButton btnHelp = (ImageButton) findViewById(R.id.btnHelp);
		btnHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				IntroActivity.open(EmergencyButtonActivity.this);
			}
		});

		/*
		ImageButton btnContacts = (ImageButton) findViewById(R.id.btnContacts);
		btnContacts.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);  
				startActivityForResult(intent, CONTACT_PICKER_RESULT);
			}
		});
		*/
		

	}

	/*
	protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (reqCode) {
            case CONTACT_PICKER_RESULT:
                Cursor cursor = null;
                String number = "";
                String lastName ="";
                try {

                    Uri result = data.getData();

                    //get the id from the uri
                    String id = result.getLastPathSegment();  

                    //query
                    cursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone._ID + " = ? " , new String[] {id}, null);

//                  cursor = getContentResolver().query(Phone.CONTENT_URI,
//                          null, Phone.CONTACT_ID + "=?", new String[] { id },
//                          null);

                    int numberIdx = cursor.getColumnIndex(Phone.DATA);  

                    if(cursor.moveToFirst()) {
                        number = cursor.getString(numberIdx);
                        //lastName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                    } else {
                        //WE FAILED
                    }
                } catch (Exception e) {
                    //failed
                } finally {
                    if (cursor!=null) {
                        cursor.close();
                    }
                    //EditText numberEditText = (EditText)findViewById(R.id.number);
                    //numberEditText.setText(number);
                    setPhoneNum(number);
                    //EditText lastNameEditText = (EditText)findViewById(R.id.last_name);
                    //lastNameEditText.setText(lastName);

                }

            }
        }
    }*/
        
	private static final int CONTACT_PICKER_RESULT = 1001;
	public static void getContacts(ContentResolver cr) {
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				// read id
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				/** read names **/
				String displayName = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				/** Phone Numbers **/
				Cursor pCur = cr.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ?", new String[] { id }, null);
				while (pCur.moveToNext()) {
					String number = pCur
							.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					String typeStr = pCur
							.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
				}
				pCur.close();

			}
		}
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

	public void setPhoneNum(String phoneNumber) {
		// gui
		EditText txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		txtPhoneNo.setText(phoneNumber);
		
		// save
		EmergencyData emergency = new EmergencyData(this);
		emergency.setPhone(txtPhoneNo.getText().toString());
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

		if ((emergency.getPhone().length() == 0) && (emergency.getEmail().length() == 0)) {
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

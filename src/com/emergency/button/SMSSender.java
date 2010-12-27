package com.emergency.button;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SMSSender {
	
    public interface SMSListener {
        public void onStatusUpdate(int resultCode, String resultString);
    }	
	
	// ---sends an SMS message to another device---
	public static void sendSMS(Context context, String phoneNumber, String message) {
		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context,
				EmergencyButton.class), 0);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, pi, null);
	}

	// sends an SMS message and reports status with toasts
	public static void safeSendSMS(final Context context, String phoneNumber, String message) {
		SMSListener toastListener = new SMSListener() {
			public void onStatusUpdate(int resultCode, String resultString) {
				Toast.makeText(context, resultString,
						Toast.LENGTH_SHORT).show();
			}
		};
		
		safeSendSMS(context, phoneNumber, message, toastListener);
	}
	
	public static void safeSendSMS(final Context context, String phoneNumber,
			String message, final SMSListener listener) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(
				SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
				new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				int resCode = getResultCode();
				switch (resCode) {
				case Activity.RESULT_OK:
					listener.onStatusUpdate(resCode, "SMS sent");
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					listener.onStatusUpdate(resCode, "Generic failure");
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					listener.onStatusUpdate(resCode, "No service");
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					listener.onStatusUpdate(resCode, "Null PDU");
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					listener.onStatusUpdate(resCode, "Radio off");
					break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				int resCode = getResultCode();
				switch (resCode) {
				case Activity.RESULT_OK:
					listener.onStatusUpdate(resCode, "SMS delivered");
					break;
				case Activity.RESULT_CANCELED:
					listener.onStatusUpdate(resCode, "SMS not delivered");
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

}

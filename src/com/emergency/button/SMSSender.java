package com.emergency.button;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SMSSender {

	public static String FINAL_GOOD_RESULT = "SMS delivered";

	public interface SMSListener {
		public void onStatusUpdate(int resultCode, String resultString);
	}

	// ---sends an SMS message to another device---
	public static void sendSMS(Context context, String phoneNumber,
			String message) {
		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(
				context, EmergencyButton.class), 0);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, pi, null);
	}

	// sends an SMS message and reports status with toasts
	public static void safeSendSMS(final Context context, String phoneNumber,
			String message) {
		SMSListener toastListener = new SMSListener() {
			public void onStatusUpdate(int resultCode, String resultString) {
				Toast.makeText(context, resultString, Toast.LENGTH_SHORT)
						.show();
			}
		};

		safeSendSMS(context, phoneNumber, message, toastListener);
	}

	private static class SMSData {
		Context context;
		SMSListener listener;

		SMSData(final Context context, final SMSListener listener) {
			this.context = context;
			this.listener = listener;
		}

		private void badEvent(int resultCode, String resultString) {
			context.unregisterReceiver(sentReceiver);
			context.unregisterReceiver(deliveredReceiver);
			listener.onStatusUpdate(resultCode, resultString);
		}

		private void goodEvent(int resultCode, String resultString) {
			if (FINAL_GOOD_RESULT.equals(resultString)) {
				// done with everything
				context.unregisterReceiver(sentReceiver);
				context.unregisterReceiver(deliveredReceiver);
			}
			listener.onStatusUpdate(resultCode, resultString);
		}

		public BroadcastReceiver sentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				int resCode = getResultCode();
				switch (resCode) {
				case Activity.RESULT_OK:
					SMSData.this.goodEvent(resCode, "SMS sent");
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					SMSData.this.badEvent(resCode, "Generic failure");
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					SMSData.this.badEvent(resCode, "No service");
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					SMSData.this.badEvent(resCode, "Null PDU");
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					SMSData.this.badEvent(resCode, "Radio off");
					break;
				}
			}
		};

		public BroadcastReceiver deliveredReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				int resCode = getResultCode();
				switch (resCode) {
				case Activity.RESULT_OK:
					// NOTE: this result doesn't appear in the emulator, only
					// on real devices.
					SMSData.this.goodEvent(resCode, FINAL_GOOD_RESULT);
					break;
				case Activity.RESULT_CANCELED:
					SMSData.this.badEvent(resCode, "SMS not delivered");
					break;
				}
			}
		};
	}

	public static void safeSendSMS(final Context context, String phoneNumber,
			String message, final SMSListener listener) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		SMSData smsd = new SMSData(context, listener);

		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
				new Intent(SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
				new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		context.registerReceiver(smsd.sentReceiver, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		context.registerReceiver(smsd.deliveredReceiver, new IntentFilter(
				DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

}

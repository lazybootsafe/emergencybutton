package com.emergency.button;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
		
		int totalMessages;
		private AtomicInteger sentCount = new AtomicInteger(0);
		private AtomicInteger deliveredCount = new AtomicInteger(0);

		@SuppressWarnings("unused")
		SMSData(final Context context, final SMSListener listener) {
			this(context, listener, 1);
		}

		SMSData(final Context context, final SMSListener listener, int totalMessages) {
			this.context = context;
			this.listener = listener;
			this.totalMessages = totalMessages;
		}

		// TODO: maybe have an unregisterReceiver on a timer as well?
		private void badEvent(int resultCode, String resultString) {
			context.unregisterReceiver(sentReceiver);
			context.unregisterReceiver(deliveredReceiver);
			listener.onStatusUpdate(resultCode, resultString);
		}

		private void deliveredEvent(int resultCode, String resultString) {
			int delivered = SMSData.this.deliveredCount.incrementAndGet();
			if (delivered == this.totalMessages) {
				// done with everything
				context.unregisterReceiver(sentReceiver);
				context.unregisterReceiver(deliveredReceiver);
				listener.onStatusUpdate(resultCode, resultString);
			}
		}

		private void sentEvent(int resultCode, String resultString) {
			int sent = SMSData.this.sentCount.incrementAndGet();
			if (sent == this.totalMessages) {
				// done with sending.
				// NOTE: We don't do anything, there is a tradeoff here:
				//	1. wait for delievered to say we succeeded can cause false negatives.
				//	2. wait for sent to say we succeeded can cause false positives.
				//context.unregisterReceiver(sentReceiver);
				//context.unregisterReceiver(deliveredReceiver);
			}
			
			//listener.onStatusUpdate(resultCode, resultString);
			listener.onStatusUpdate(resultCode, "Sent " + sent + "/" + this.totalMessages + " parts");
		}

		public BroadcastReceiver sentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				
				int resCode = getResultCode();
				switch (resCode) {
				case Activity.RESULT_OK:
					SMSData.this.sentEvent(resCode, "SMS sent");
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
					SMSData.this.deliveredEvent(resCode, FINAL_GOOD_RESULT);
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
		if(numOfMessages(message) > 1) {
			safeSendLongSMS(context, phoneNumber, message, listener);
		} else {
			safeSendLongSMS(context, phoneNumber, message, listener);
			//safeSendShortSMS(context, phoneNumber, message, listener);
		}
	}
	
	/*
	// TODO: remove safeSendShortSMS completely in favor of safesendlongsms...
	public static void safeSendShortSMS(final Context context, String phoneNumber,
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
		
		try {
			sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		} catch (Exception e) {
			// We don't want the app to die because of a failed SMS.
			// Certain phone models have NullPointerException here either because
			// of android.os.Parcel.readException or android.telephony.SmsMessage$SubmitPdu.<init>
			// 
			// android.os.Parcel.readException can mean either the message is too long
			// or that the number given was invalid. Talk about
			// awkward error handling.
			EmailSender.send("admin@andluck.com", "EmergencyButtonError",
					Utils.stacktrace(e));

			// unregister and inform of failure
			smsd.badEvent(SmsManager.RESULT_ERROR_GENERIC_FAILURE, "SMS error");
		}
	}*/

	public static int numOfMessages(String message) {
		SmsManager smsMan = SmsManager.getDefault();
		return smsMan.divideMessage(message).size();
	}

	public static void safeSendLongSMS(final Context context, String phoneNumber,
			String message, final SMSListener listener) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		SmsManager smsMan = SmsManager.getDefault();
		ArrayList<String> messagesArray = smsMan.divideMessage(message);
		
		SMSData smsd = new SMSData(context, listener, messagesArray.size());

		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
				new Intent(SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
				new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		context.registerReceiver(smsd.sentReceiver, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		context.registerReceiver(smsd.deliveredReceiver, new IntentFilter(
				DELIVERED));

		ArrayList<PendingIntent> sentPIList = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> deliveredPIList = new ArrayList<PendingIntent>();
		
		for (int i = 0; i < messagesArray.size() ; i++) {
			// we don't care which failed, one failure is bad enough.
			sentPIList.add(sentPI);
			deliveredPIList.add(deliveredPI);
		}
		
		try {
			smsMan.sendMultipartTextMessage(phoneNumber, null, messagesArray, sentPIList, deliveredPIList);
		} catch (Exception e) {
			// Certain phone models have NullPointerException here either because
			// of android.os.Parcel.readException or android.telephony.SmsMessage$SubmitPdu.<init>
			// use this temporarily just to figure out the cause.
			// android.os.Parcel.readException can mean either it's too long
			// of a message or that the number given was invalid. Talk about
			// awkward error handling.
			EmailSender.send("admin@andluck.com", "EmergencyButtonError",
					Utils.packageInfo(context) + "\n" + 
					Utils.stacktrace(e));

			// unregister and inform of failure
			smsd.badEvent(SmsManager.RESULT_ERROR_GENERIC_FAILURE, "SMS error");
		}
	}
	
}

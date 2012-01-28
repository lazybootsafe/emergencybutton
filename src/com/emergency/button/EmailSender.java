package com.emergency.button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class EmailSender {
	private static String LOG_TAG = "EmailSender";
	
	public static boolean send(String to, String message)  {
		return EmailSender.sendWithEmailbyweb(to, "Emergency", message);
	}
	
	public static boolean send(String to, String subject, String message) {
		return EmailSender.sendWithEmailbyweb(to, subject, message);
	}
	
	public static boolean sendWithEmailbyweb(String to, String subject, String message) {
		// NOTE: the "from" is ignored in the php version currently.
		boolean res = postToUrl("http://toplessproductions.com/emailbyweb/", "Emergency Button <EmergencyButtonApp@gmail.com>", to, subject,message);
		if(!res) {
			res = postToUrl("https://emailbyweb.appspot.com/email", "Emergency Button <EmergencyButton@emailbyweb.appspotmail.com>", to, subject,message);
		}
		return res;
	}
	
	public static boolean postToUrl(String url, String from, String to, String subject, String message) {
		String responseBody = "";
		
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		//HttpPost httppost = new HttpPost("https://emailbyweb.appspot.com/email");
		//HttpPost httppost = new HttpPost("http://toplessproductions.com/emailbyweb/");
		HttpPost httppost = new HttpPost(url);

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("to", to));
			nameValuePairs.add(new BasicNameValuePair("from", from));
			nameValuePairs.add(new BasicNameValuePair("subject", subject));
			nameValuePairs.add(new BasicNameValuePair("message", message));
			nameValuePairs.add(new BasicNameValuePair("secret", Config.secret));
			// NOTE: UrlEncodedFormEntity has a default encoding of ISO-8859-1
			//   which is perfect if you want your unicode to silently turn into 
			//   \x1a or empty spaces.
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

			// Create a response handler
	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        responseBody = httpclient.execute(httppost, responseHandler);
	        
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		
		if ("success".equals(responseBody)) {
			Log.v(LOG_TAG, "Email sent.");
			return true;
		} else {
			Log.e(LOG_TAG, "Failed sending email: response \"" + responseBody + "\"");
			return false;
		}
	}
}

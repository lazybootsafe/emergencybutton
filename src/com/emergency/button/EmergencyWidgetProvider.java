package com.emergency.button;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Define a simple widget that shows the Wiktionary "Word of the day." To build
 * an update we spawn a background {@link Service} to perform the API queries.
 */
public class EmergencyWidgetProvider extends AppWidgetProvider {
	
	public static String ACTION_WIDGET_CLICK_1 = "ClickWidget1";
	public static String ACTION_WIDGET_CLICK_2 = "ClickWidget2";
	public static long button_clicked_time_1 = 0;
	public static long button_clicked_time_2 = 0;
	private static long SECOND_MS = 1000;
	private static long TIME_BETWEEN_PRESSES = SECOND_MS * 5;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// To prevent any ANR timeouts, we perform the update in a service
		//context.startService(new Intent(context, UpdateService.class));
		
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            
            // Create an Intent to launch ExampleActivity
            Intent intent;
            PendingIntent pendingIntent;
            intent = new Intent(context, EmergencyWidgetProvider.class);
            intent.setAction(EmergencyWidgetProvider.ACTION_WIDGET_CLICK_1);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.btnEmergencyWidget1, pendingIntent);
            
            intent = new Intent(context, EmergencyWidgetProvider.class);
            intent.setAction(EmergencyWidgetProvider.ACTION_WIDGET_CLICK_2);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.btnEmergencyWidget2, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: Make the buttons appear to stay down for 5 seconds after being pressed
		if (intent.getAction().equals(EmergencyWidgetProvider.ACTION_WIDGET_CLICK_1)) {
			EmergencyWidgetProvider.button_clicked_time_1 = SystemClock.elapsedRealtime();
		} else if (intent.getAction().equals(EmergencyWidgetProvider.ACTION_WIDGET_CLICK_2)) {
			EmergencyWidgetProvider.button_clicked_time_2 = SystemClock.elapsedRealtime();
		}

		long now = SystemClock.elapsedRealtime();
		long pressLimit = now - TIME_BETWEEN_PRESSES;
		if ((pressLimit < EmergencyWidgetProvider.button_clicked_time_1) && 
			(pressLimit < EmergencyWidgetProvider.button_clicked_time_2)) {
			
			//new Emergency().emDialog(context).show();
			Intent myIntent = new Intent(context, EmergencyActivity.class);
			
			// FLAG_ACTIVITY_NEW_TASK is needed because we're not in an activity
			// already, without it we crash.
			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(myIntent);

			//Emergency.emergencyNow(context);
		}

		super.onReceive(context, intent);
	}
		
//		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
//		Intent clickIntent = new Intent(context, EmergencyWidget.class);
//		clickIntent.setAction(ACTION_WIDGET_CLICK);
//		PendingIntent clickPendingIntent = clickIntent.getActivity(context, 0, clickIntent, 0);
//
//		remoteViews.setOnClickPendingIntent(R.id.button_one, actionPendingIntent);
//		remoteViews.setOnClickPendingIntent(R.id.button_two, configPendingIntent);
//		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//		ImageButton btnEmergencyWidget1 = (ImageButton) updateViews.findViewById(R.id.btnEmergencyWidget1);
//		ImageButton btnEmergencyWidget2 = (ImageButton) updateViews.findViewById(R.id.btnEmergencyWidget2);
//		
//		btnEmergency.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				EmergencyButton.this.buttonPressed = true;
//
//				// try sending the message:
//				EmergencyButton.this.sendEmergencyMessage();
//			}
//		});

//	public static class UpdateService extends Service {
//		@Override
//		public void onStart(Intent intent, int startId) {
//			// Build the widget update for today
//			RemoteViews updateViews = buildUpdate(this);
//
//			// Push update for this widget to the home screen
//			//ComponentName thisWidget = new ComponentName(this,
//			//		EmergencyWidget.class);
//			AppWidgetManager manager = AppWidgetManager.getInstance(this);
//			//manager.updateAppWidget(thisWidget, updateViews);
//		}
//
//		/**
//		 * Build a widget update to show the current Wiktionary
//		 * "Word of the day." Will block until the online API returns.
//		 */
//		public RemoteViews buildUpdate(Context context) {
//			// Pick out month names from resources
//			Resources res = context.getResources();
//			RemoteViews updateViews = null;
//
//			// String[] monthNames = res.getStringArray(R.array.month_names);
//			//
//			// // Find current month and day
//			// Time today = new Time();
//			// today.setToNow();
//			//
//			// // Build today's page title, like
//			// "Wiktionary:Word of the day/March 21"
//			// String pageName = res.getString(R.string.template_wotd_title,
//			// monthNames[today.month], today.monthDay);
//			// RemoteViews updateViews = null;
//			// String pageContent = "";
//			//
//			// try {
//			// // Try querying the Wiktionary API for today's word
//			// SimpleWikiHelper.prepareUserAgent(context);
//			// pageContent = SimpleWikiHelper.getPageContent(pageName, false);
//			// } catch (ApiException e) {
//			// Log.e("WordWidget", "Couldn't contact API", e);
//			// } catch (ParseException e) {
//			// Log.e("WordWidget", "Couldn't parse API response", e);
//			// }
//			//
//			// // Use a regular expression to parse out the word and its
//			// definition
//			// Pattern pattern =
//			// Pattern.compile(SimpleWikiHelper.WORD_OF_DAY_REGEX);
//			// Matcher matcher = pattern.matcher(pageContent);
//			// if (matcher.find()) {
//			// // Build an update that holds the updated widget contents
//			// updateViews = new RemoteViews(context.getPackageName(),
//			// R.layout.widget);
//			//
//			// String wordTitle = matcher.group(1);
//			// updateViews.setTextViewText(R.id.word_title, wordTitle);
//			// updateViews.setTextViewText(R.id.word_type, matcher.group(2));
//			// updateViews.setTextViewText(R.id.definition,
//			// matcher.group(3).trim());
//			//
//			// // When user clicks on widget, launch to Wiktionary definition
//			// page
//			// String definePage = res.getString(R.string.template_define_url,
//			// Uri.encode(wordTitle));
//			// Intent defineIntent = new Intent(Intent.ACTION_VIEW,
//			// Uri.parse(definePage));
//			// PendingIntent pendingIntent = PendingIntent.getActivity(context,
//			// 0 /* no requestCode */, defineIntent, 0 /* no flags */);
//			// updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
//			//
//			// }
//			// Didn't find word of day, so show error message
//			updateViews = new RemoteViews(context.getPackageName(),
//					R.layout.widget_layout);
//			CharSequence errorMessage = context.getText(R.string.widget_error);
//			// updateViews.setTextViewText(R.id.message, errorMessage);
//			return updateViews;
//		}
//
//		@Override
//		public IBinder onBind(Intent intent) {
//			// We don't need to bind to this service
//			return null;
//		}
//	}
}

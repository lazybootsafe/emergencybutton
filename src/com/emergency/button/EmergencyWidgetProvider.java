package com.emergency.button;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;


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
			//Intent myIntent = new Intent();
			//myIntent.setClassName("com.emergency.button", "com.emergency.button.EmergencyActivity");
			//--Intent myIntent = new Intent(Service.createPackageContext("com.emergency.button", 0), EmergencyActivity.class);
			//Intent myIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "testemail@gmail.com", null));
			//Intent myIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", "987654321", null));
			
			// FLAG_ACTIVITY_NEW_TASK is needed because we're not in an activity
			// already, without it we crash.
			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(myIntent);

			//Emergency.emergencyNow(context);
		}

		super.onReceive(context, intent);
	}
}
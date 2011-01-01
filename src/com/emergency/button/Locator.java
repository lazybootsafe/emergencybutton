/**
 * Locator is a class which allows getting a location with minimum requirements
 * of age and accuracy. There is also a timer which fires after 20 seconds and
 * uses the best location that was found until now. After a location is sent
 * the locator class auto unregisters.
 * 
 */

package com.emergency.button;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


/**
 * @author Yuv
 *
 */
/**
 * @author Yuv
 *
 */
public class Locator {
	private static final int SECOND_MS = 1000;
	private static final int MINUTE_MS = 60 * SECOND_MS;
	
	
	private static final int MAX_WAITING_TIME_MS = 20 * SECOND_MS;
	private static final float REQUIRED_ACCURACY_METERS = 50;
	private static final int LARGE_LOCATION_AGE_MS = 2 * MINUTE_MS;
	
	
	public Location location = null;
	
	private LocationManager locationManager = null;
	private LocationListener locationListener;
	private BetterLocationListener blListener;
	private Timer waitForGoodLocationTimer;
	
    public interface BetterLocationListener {
        void onGoodLocation(Location location);
    }
    
    /**
      * Locator
      *
      * remember to call locator.unregister() when you're done.
    */
    public Locator(final Context context, final BetterLocationListener blListener) {
		if (this.locationManager != null) {
			Log.e("Locator", "registered twice!");
			return;
		}
		
		this.blListener = blListener;
		
		// Acquire a reference to the system Location Manager
		this.locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		//Toast.makeText(act.getBaseContext(), "Locator register", Toast.LENGTH_SHORT).show();

		Location lastKnownNetLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location lastKnownSatLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (Locator.isBetterLocation(lastKnownSatLocation, lastKnownNetLocation)) {
			this.location = lastKnownSatLocation;
		} else {
			this.location = lastKnownNetLocation;
		}

		// Define a listener that responds to location updates
		this.locationListener = new GoodLocationListener();

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this.locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 0, 0, this.locationListener);
		
		this.waitForGoodLocationTimer = new Timer();
		this.waitForGoodLocationTimer.schedule(new GetLastLocation(), MAX_WAITING_TIME_MS);
    }
    
    private class GoodLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location
			// or GPS provider.
			if (Locator.this.location == null) {
				// never use the first location, always compare because
				// that's the only way to find the age of the location.
				Locator.this.location = location;
				return;
			}
			
			if (Locator.isBetterLocation(location, Locator.this.location)) {
				Locator.this.location = location;
				
				if (Locator.isGoodLocation(Locator.this.location)) {
					Locator.this.waitForGoodLocationTimer.cancel();
					Locator.this.emitLocation();
				}
			}

			
			//Toast.makeText(act.getBaseContext(), "New location: " + Locator.location.toString(), Toast.LENGTH_SHORT).show();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
		public void onProviderDisabled(String provider) {}
	};
    
    protected static boolean isGoodLocation(Location location) {
    	// NOTE: You can't know the exact age of each location because
    	//		the system time is set by the user and the location time is set 
    	//		by the satellites. You can only compare locations by age,
    	//		you can't measure the absolute age of a location.
    	if (! location.hasAccuracy()) {
    		return false;
    	}
    	    	
    	if (location.getAccuracy() > REQUIRED_ACCURACY_METERS ) {
    		return false;
    	}
    	
    	return true;
    }
    
    
    /**	
	 Determines whether one Location reading is better than the current
	 Location fix
	 
	 @param location
	            The new Location that you want to evaluate
	 @param currentBestLocation
	            The current Location fix, to which you want to compare the new
	            one
	            
	 @return true if the first location is better.
	*/
	protected static boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > LARGE_LOCATION_AGE_MS;
		boolean isSignificantlyOlder = timeDelta < -LARGE_LOCATION_AGE_MS;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	private void emitLocation() {
		Locator.this.unregister();
		Locator.this.blListener.onGoodLocation(Locator.this.location);
	}
	
	// Checks whether two providers are the same
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	
	class GetLastLocation extends TimerTask {
		@Override
		public void run() {
			// ran out of time to get a good location, go go go
			Locator.this.emitLocation();
		}
	}
    
	public void unregister() {
		if (this.locationManager == null) {
			return;
		}
		
		this.locationManager.removeUpdates(this.locationListener);
		this.locationManager = null;
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		unregister(); 
	}	
}

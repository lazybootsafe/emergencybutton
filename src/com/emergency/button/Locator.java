package com.emergency.button;

import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


public class Locator {

	private static final int TWO_MINUTES = 1000 * 60 * 2;
	public static Location location = null;
	
	private static LocationManager locationManager = null;
	private static LocationListener locationListener;
	
    public interface BetterLocationListener {
        void onBetterLocation(Location location);
    }
    
    /////
    // Locator
    //
    // remember to call locator.unregister() when you're done.
    //
    public Locator(final Context context, final BetterLocationListener bll) {
		if (Locator.locationManager != null) {
			Log.e("Locator", "registered twice!");
			return;
		}
		
		// Acquire a reference to the system Location Manager
		Locator.locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		//Toast.makeText(act.getBaseContext(), "Locator register", Toast.LENGTH_SHORT).show();

		// TODO: use this history in an intelligent way
		// Location lastKnownLocation =
		// locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		// Location lastKnownLocation =
		// locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// or GPS provider.
				// makeUseOfNewLocation(location);
				if (Locator.isBetterLocation(location, Locator.location)) {
					Locator.location = location;
					bll.onBetterLocation(Locator.location);
				}

				//Toast.makeText(act.getBaseContext(), "New location: " + Locator.location.toString(), Toast.LENGTH_SHORT).show();
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 0, 0, locationListener);    	
    }
    
	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected static boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
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

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	
	public void unregister() {
		if (Locator.locationManager == null) {
			return;
		}
		
		Locator.locationManager.removeUpdates(Locator.locationListener);
		Locator.locationManager = null;
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		unregister(); 
	}	
}

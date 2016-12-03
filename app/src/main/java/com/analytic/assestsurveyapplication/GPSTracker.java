package com.analytic.assestsurveyapplication;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;


/**
 * Created by MeHuL on 20-06-2016.
 */
public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    SharedPreferences sharedPreferences;
    Editor editor;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0; //1000 * 10 * 1; // 1 minute
    long checkInterval = 0;
    long minDistance = 0;
    // Declaring a Location Manager
    protected LocationManager locationManager;


    public GPSTracker(Context context) {
        this.mContext = context;

        sharedPreferences = mContext.getSharedPreferences("MyPref", 0);
        editor = sharedPreferences.edit();

        getLocation();
    }

    public Location getLocation() {
        try {

            Location locationfromGPS = null;
            Location locationfromNETWORK = null;
            Location locationfromPASSIVE = null;
            float accuracyfromGps = 0;
            float accuracyfromNETWORK = 0;
            float accuracyfromPASSIVE = 0;
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                for (final String provider : locationManager.getAllProviders()) {
                    Log.e("getAllProviders ", "" + provider);

                    locationManager.requestLocationUpdates(provider, checkInterval, minDistance, this);

                    if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                        if (isGPSEnabled) {
                            locationfromGPS = getBestLocation(provider);
                            // Log.e("GPS", "location : " + locationfromGPS);

                            if (locationfromGPS != null) {
                                accuracyfromGps = locationfromGPS.getAccuracy();
                                //  Log.e("GPS", "Accuracy : " + accuracyfromGps);
                            }
                        }
                    }
                    if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                        if (isNetworkEnabled) {
                            locationfromNETWORK = getBestLocation(provider);
                            // Log.e("Network", "location : " + locationfromNETWORK);

                            if (locationfromNETWORK != null) {
                                accuracyfromNETWORK = locationfromNETWORK.getAccuracy();
                                // Log.e("Network", "Accuracy : " + accuracyfromNETWORK);
                            }
                        }
                    }
                    if (provider.equalsIgnoreCase("passive")) {
                        locationfromPASSIVE = getBestLocation(provider);
                        //   Log.e("passive", "location : " + locationfromPASSIVE);

                        if (locationfromPASSIVE != null) {
                            accuracyfromPASSIVE = locationfromPASSIVE.getAccuracy();
                            //  Log.e("passive", "Accuracy : " + accuracyfromPASSIVE);
                        }
                    }

                }

                if (accuracyfromGps >= accuracyfromNETWORK && accuracyfromGps >= accuracyfromPASSIVE) {
                    Log.e("GPS", "Accur : " + accuracyfromGps);
                    location = locationfromGPS;
                } else if (accuracyfromNETWORK >= accuracyfromGps && accuracyfromNETWORK >= accuracyfromPASSIVE) {
                    Log.e("Network", "Accur : " + accuracyfromNETWORK);
                    location = locationfromNETWORK;
                } else {
                    Log.e("passive", "Accur : " + accuracyfromPASSIVE);
                    location = locationfromPASSIVE;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (location != null) {

            latitude = location.getLatitude();
            longitude = location.getLongitude();

        }
        Log.e("latitude", ":: " + latitude);
        Log.e("longitude", ":: " + longitude);
        return location;
    }

    private Location getBestLocation(String provider) {

        Location location = null;
        Location networkLocation = null;
        Log.e("provider", "BestLocation : " + provider);

        location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            //Log.e(" gpslocation : ", "getBestLocation " + location);
            return location;
        } else {
            Log.e("", "Current location null:");
        }
        return null;
    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu? After GPS Enable you need to reload page to get location.");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

        editor.putString("serviceCurrLat", Double.toString(location.getLatitude()));
        editor.putString("serviceCurrLng", Double.toString(location.getLongitude()));

        editor.commit();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}

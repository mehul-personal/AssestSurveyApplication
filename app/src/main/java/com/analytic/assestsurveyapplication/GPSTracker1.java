package com.analytic.assestsurveyapplication;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker1 extends Service implements LocationListener, GetLocationUpdates.LocationUpdates {

        private final Context mContext;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;
    double latitude;
    double longitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;//meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1; // 1 minute
    protected LocationManager locationManager;
    static Location fusedLocation;

    private LocationManager mgr;
    private String best;
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
//public GPSTracker1(){
//
//}
    public GPSTracker1(Context context) {
        this.mContext = context;

    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Location location = mgr.getLastKnownLocation(best);
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Background service code
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        best = mgr.getBestProvider(criteria, true);
        mgr.requestLocationUpdates(best, 1000, 1, this);

        GetLocationUpdates getLocUpdates = new GetLocationUpdates(this);
        getLocUpdates.setLocationUpdatesListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    public Location getLocation() {
        try {

            Location locationfromGPS = null;
            Location locationfromNETWORK = null;
            Location locationfromPASSIVE = null;
            float accuracyfromGps = 0;
            float accuracyfromNETWORK = 0;
            float accuracyfromPASSIVE = 0;
            float accuracyfromFUSED = 0;
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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

                    locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    // locationManager.addProximityAlert();

                    if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                        if (isGPSEnabled) {
                            locationfromGPS = getBestLocation(provider);
                            // Log.e("GPS", "location : " + locationfromGPS);

                            if (locationfromGPS != null) {
                                accuracyfromGps = locationfromGPS.getAccuracy();
                                Log.e("GPS", "Accuracy : " + accuracyfromGps);
                            }
                        }
                    }
                    if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                        if (isNetworkEnabled) {
                            locationfromNETWORK = getBestLocation(provider);
                            // Log.e("Network", "location : " + locationfromNETWORK);

                            if (locationfromNETWORK != null) {
                                accuracyfromNETWORK = locationfromNETWORK.getAccuracy();
                                Log.e("Network", "Accuracy : " + accuracyfromNETWORK);
                            }
                        }
                    }
                    if (provider.equalsIgnoreCase("passive")) {
                        locationfromPASSIVE = getBestLocation(provider);
                        //   Log.e("passive", "location : " + locationfromPASSIVE);

                        if (locationfromPASSIVE != null) {
                            accuracyfromPASSIVE = locationfromPASSIVE.getAccuracy();
                            Log.e("passive", "Accuracy : " + accuracyfromPASSIVE);
                        }
                    }

                }
                if (fusedLocation != null) {
                    accuracyfromFUSED = fusedLocation.getAccuracy();
                    Log.e("fused", "Accuracy : " + accuracyfromFUSED);
                }
                if (accuracyfromFUSED == 0 && accuracyfromGps == 0 && accuracyfromNETWORK == 0 && accuracyfromPASSIVE == 0) {
                    //  showSettingsAlert(this);
                } else {
                    if (fusedLocation != null) {
                        location = fusedLocation;

                    } else {
                        if (accuracyfromGps <= (accuracyfromNETWORK > 0 ? accuracyfromNETWORK : accuracyfromGps) &&
                                accuracyfromGps <= (accuracyfromPASSIVE > 0 ? accuracyfromPASSIVE : accuracyfromGps) &&
                                accuracyfromGps > 0) {
                            Log.e("GPS", "Accur : " + accuracyfromGps);
                            location = locationfromGPS;
                        } else if (accuracyfromNETWORK <= (accuracyfromGps > 0 ? accuracyfromGps : accuracyfromNETWORK) &&
                                accuracyfromNETWORK <= (accuracyfromPASSIVE > 0 ? accuracyfromPASSIVE : accuracyfromNETWORK) &&

                                accuracyfromNETWORK > 0) {
                            Log.e("NETWORK", "Accur : " + accuracyfromNETWORK);
                            location = locationfromNETWORK;
                        } else if (accuracyfromPASSIVE <= (accuracyfromGps > 0 ? accuracyfromGps : accuracyfromPASSIVE) &&
                                accuracyfromPASSIVE <= (accuracyfromNETWORK > 0 ? accuracyfromNETWORK : accuracyfromPASSIVE) &&

                                accuracyfromPASSIVE > 0) {
                            Log.e("PASSIVE", "Accur : " + accuracyfromPASSIVE);
                            location = locationfromPASSIVE;
                        }
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
           /* SharedPreferences sharedPreferences = getSharedPreferences("LOCATION_DETAIL", 0);
            double oldlat = Double.parseDouble(sharedPreferences.getString("OLDLATITUDE", "0.0"));
            double oldlng = Double.parseDouble(sharedPreferences.getString("OLDLONGIUDE", "0.0"));
            float[] results = new float[3];
            if (oldlat > 0) {
                // Location.distanceBetween(oldlat, oldlng, latitude, longitude, results);

                if (distFrom(oldlat, oldlng, latitude, longitude) > 5 ) {
                    if (checkInternet(this))
                        sendLocation(latitude, longitude);
                    else
                        locationDataSave(latitude, longitude);
                }
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("OLDLATITUDE", String.valueOf(latitude));
            editor.putString("OLDLONGIUDE", String.valueOf(longitude));
            editor.commit();*/
        }
        return location;
    }

  /*  public float distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        if (dist > 10.0) {


        }

        return dist;
    }

    public void sendLocation(final double latitude, final double longitude) {


        if (Integer.parseInt(getSharedPreferences("LOGIN_DETAILS", 0).getString("USER_ID", "0")) > 0) {
            Log.e("latlong", "" + latitude + ":" + longitude + ":");
            //   Toast.makeText(UpdateLocation.this,  "" + currentLocation.getLatitude() +":"+ currentLocation.getLongitude()+":"+currentLocation.getAccuracy(), Toast.LENGTH_SHORT).show();
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String formattedDate = df.format(c.getTime());

            String tag_json_obj = "json_obj_req";
            String url = ApplicationData.serviceURL + "/trackerapi";
            Log.e("url", url + "");
            StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                    url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.e("trackerapi", "data" + response.toString());

                    try {
                        JSONObject object = new JSONObject(response.toString());
                        String msg = object.getString("message");
                        if (msg.equalsIgnoreCase("success")) {


                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    // TODO Auto-generated method stub
                    VolleyLog.e("trackerapi Error", "Error: " + e.getMessage());
                    e.getCause();
                    e.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("tracker_id", getSharedPreferences("LOGIN_DETAILS", 0).getString("USER_ID", "0"));
                    params.put("datetime", formattedDate);
                    params.put("lat", "" + latitude);
                    params.put("lng", "" + longitude);
                    return params;
                }
            };
            ApplicationData.getInstance().addToRequestQueue(jsonObjReq,
                    tag_json_obj);
            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));

        }
    }


    public void locationDataSave(double latitude, double longitude) {
        SQLiteDatabase mdatabase = openOrCreateDatabase(ApplicationData.DATABASE, Context.MODE_PRIVATE, null);
        mdatabase.beginTransaction();
        if (Integer.parseInt(getSharedPreferences("LOGIN_DETAILS", 0).getString("USER_ID", "0")) > 0) {
            try {
                Calendar c = Calendar.getInstance();
                System.out.println("Current time => " + c.getTime());

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final String formattedDate = df.format(c.getTime());

                ContentValues values = new ContentValues();
                values.put("lat", latitude + "");
                values.put("lng", longitude + "");
                values.put("dt", formattedDate);
                values.put("STATUS", "STORE");
                mdatabase.insert(ApplicationData.TABLE, null, values);

                mdatabase.setTransactionSuccessful();
                Log.e("Expereince database", "NEW LOCATION DETAIL INSERTED ON TABLE");

            } catch (Exception e) {
                Log.e("Expereince database", "NEW LOCATION DETAIL NOT INSERTED ON TABLE");

            } finally {
                mdatabase.endTransaction();
                mdatabase.close();
            }
        } else {
            mdatabase.endTransaction();
            mdatabase.close();

        }
    }*/

    public class ServiceManager extends ContextWrapper {

        public ServiceManager(Context base) {
            super(base);
        }

        public boolean isNetworkAvailable() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

    }

    boolean checkInternet(Context context) {
        ServiceManager serviceManager = new ServiceManager(context);
        return serviceManager.isNetworkAvailable();
    }

    private Location getBestLocation(String provider) {

        Location location = null;
        location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            return location;
        }
        return null;
    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker1.this);
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
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
        Log.e("location change", "location change");
        if (location.hasAccuracy() && location.getAccuracy() <= 100.0) {
            // the location has accuracy and has an accuracy span within 100m radius
            // do whatever you want with this location and stop location listener
            if (Integer.parseInt(getSharedPreferences("LOGIN_DETAILS", 0).getString("USER_ID", "0")) > 0) {
                getLocation();
            }
           // stopUsingGPS();
        }
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

    @Override
    public void handleLocationUpdatesCallback(Location location) {
        fusedLocation = location;
    }
}



/*
package com.qubitworks.journey;

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

public class GPSTracker1 extends Service implements LocationListener {
	 
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
    private static final long MIN_TIME_BW_UPDATES =0; //1000 * 10 * 1; // 1 minute
 
    // Declaring a Location Manager
    protected LocationManager locationManager;
 
    public GPSTracker1(Context context) {
        this.mContext = context;
        
        sharedPreferences = mContext.getSharedPreferences("MyPref", 0);
        editor = sharedPreferences.edit();
        
        getLocation();
    }
 
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
 
            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
 
            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS FETCHED LATLONG");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }else if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.e("Network", "NETWORK FETCHED LATLONG");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return location;
    }
     
    */
/**
 * Stop using GPS listener
 * Calling this function will stop using GPS in your app
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 *
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * <p/>
 * Function to get latitude
 * <p/>
 * Function to get longitude
 * <p/>
 * Function to check GPS/wifi enabled
 * @return boolean
 * <p/>
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 *//*

    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker1.this);
        }       
    }
     
    */
/**
 * Function to get latitude
 * *//*

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
         
        // return latitude
        return latitude;
    }
     
    */
/**
 * Function to get longitude
 * *//*

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
         
        // return longitude
        return longitude;
    }
     
    */
/**
 * Function to check GPS/wifi enabled
 * @return boolean
 * *//*

    public boolean canGetLocation() {
        return this.canGetLocation;
    }
     
    */
/**
 * Function to show settings alert dialog
 * On pressing Settings button will lauch Settings Options
 * *//*

    public void showSettingsAlert(final Context context){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu? After GPS Enable you need to reload page to get location.");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
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
*/

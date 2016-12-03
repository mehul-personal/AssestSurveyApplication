package com.analytic.assestsurveyapplication;

/**
 * Created by Admin on 05-02-2016.
 */

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GetLocationUpdates implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    Context mContext;

    private static int UPDATE_INTERVAL = 3000; // 2 sec
    private static int FATEST_INTERVAL = 2000; // 1 sec
    public static int DISPLACEMENT = 8; // 8 meters
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private LocationUpdates locationsUpdatesList;

    public GetLocationUpdates(Context context) {
        // TODO Auto-generated constructor stub
        this.mContext = context;
        buildGoogleApiClient();
        createLocationRequest();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {
//        if ( Build.VERSION.SDK_INT >= 23 &&
//                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return  ;
//        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
//		Log.d("Location changed", "changed");
        if (locationsUpdatesList != null) {
            locationsUpdatesList.handleLocationUpdatesCallback(location);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    public void StopUpdates() {
        stopLocationUpdates();
    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
//		Log.d("Get Location Updates On connected", "Called");
        if (locationsUpdatesList != null) {
//            if ( Build.VERSION.SDK_INT >= 23 &&
//                    ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                    ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return  ;
//            }
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            locationsUpdatesList.handleLocationUpdatesCallback(mLastLocation);
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public interface LocationUpdates {
        void handleLocationUpdatesCallback(Location location);
    }

    public void setLocationUpdatesListener(LocationUpdates locationsUpdatesList) {
        this.locationsUpdatesList = locationsUpdatesList;
    }
}
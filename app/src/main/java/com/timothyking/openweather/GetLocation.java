package com.timothyking.openweather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class GetLocation extends AppCompatActivity {
    private LocationManager myManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "In GetLocation.onCreate", Toast.LENGTH_LONG).show();
        // set up the LocationManager
        myManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onDestroy() {
        stopListening();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        stopListening();
        super.onPause();
    }

    @Override
    protected void onResume() {
        startListening();
        super.onResume();
    }

    /**********************************************************************
     * helpers for starting/stopping monitoring of GPS changes below
     **********************************************************************/
    @SuppressLint("MissingPermission")
    private void startListening() {
        Toast.makeText(this, "In startListening", Toast.LENGTH_LONG).show();
        myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, (LocationListener) this);
    }

    private void stopListening() {
        if (myManager != null)
            myManager.removeUpdates((LocationListener) this);
    }

    /**********************************************************************
     * LocationListener overrides below
     **********************************************************************/
    // @Override
    public void onLocationChanged(Location location) {
        // we got new location info. lets display it in the textview
        String lat = String.valueOf(location.getLatitude());

        // TCK ToDo, fails if input strings are too short
        int latlen = lat.length();

        String lon = String.valueOf(location.getLongitude());
        String short_lat = lat.substring(0, (Integer) lat.indexOf(".") + 7);
        String short_lon = lon.substring(0, (Integer) lon.indexOf(".") + 7);

        Toast.makeText(this, short_lat + "," + short_lon, Toast.LENGTH_LONG).show();
        // Log.i("LatLon", short_lat + "," + short_lon);

    }

    // @Override
    public void onProviderDisabled(String provider) {}

    // @Override
    public void onProviderEnabled(String provider) {}

    // @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}

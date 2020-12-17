package com.example.stepmapper.ui.map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.os.IBinder;
import android.os.Bundle;
import android.app.Service;
import android.util.Log;


import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stepmapper.MainActivity;
import com.example.stepmapper.R;


public class LocationTrack extends Service implements LocationListener {
    private final Context mContext;

    boolean checkGPS = false;
    boolean checkNetwork = false;
    boolean canGetLocation = false;

    Location loc;
    double latitude;
    double longitude;
    TextView locationText;

    // TODO: meaningful distance updates
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // TODO: meaningful update times
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    protected LocationManager locationManager;

    public LocationTrack(Context mContext) {
        this.mContext = mContext;
        getLocation();
    }

    private Location getLocation() {
        Log.d("Location_Update", "1");


        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(Context.LOCATION_SERVICE);

            // get GPS status
            checkGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // get network provider status
            checkNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetwork) {
                Log.d("Location_Update", "2");
                Toast.makeText(mContext, "No Service Provider is available", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Location_Update", "3");
                this.canGetLocation = true;

                // if GPS Enabled get lat/long using GPS Services
                if (checkGPS) {
                    Log.d("Location_Update", "4");
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions

                        Log.d("Location_Update", "5");
                    }
                    Log.d("Location_Update", "4.5");
//                    locationManager.requestLocationUpdates(
//                            LocationManager.GPS_PROVIDER,
//                            MIN_TIME_BW_UPDATES,
//                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Location_Update", "5.5");
                    if (locationManager != null) {
                        Log.d("Location_Update", "6");
                        Log.d("Location_Update", String.valueOf(checkGPS));

                        //TODO: FIX BROKEN HERE
                        if(checkGPS){
                            Log.d("Location_Update", "6.1");
                            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        } else{
                            Log.d("Location_Update", "6.2");
                            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
//                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Log.d("Location_Update", "6.5");
                        if (loc != null) {
                            latitude = loc.getLatitude();
                            longitude = loc.getLongitude();

                            Log.d("Location_Update", "update " + latitude + "/" + longitude);
                        }
                    }
                    Log.d("Location_Update", "5.9");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Location_Update", "7");
            Log.d("Location_Update", String.valueOf(e));
        }
        Log.d("Location_Update", "8");
        return loc;
    }

    public double getLongitude() {
        if (loc != null) {
            longitude = loc.getLongitude();
        }
        return longitude;
    }

    public double getLatitude() {
        if (loc != null) {
            latitude = loc.getLatitude();
        }
        return latitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });


        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void stopListener() {
        if (locationManager != null) {

            if (ActivityCompat.checkSelfPermission(
                    mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                return;
            }
            locationManager.removeUpdates(LocationTrack.this);
        }
    }

    public void setTextViewToModify(TextView tv) {
        this.locationText = tv;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged","start");

        if (location != null) {
            loc=location;
            getLatitude();
            getLongitude();
            Log.d("onLocationChanged","update"+location);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("Latitude","status");
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("Latitude","disable");
    }

}

package com.example.stepmapper.ui.map;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.stepmapper.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    // Views
    private Button mLocationButton;
    private TextView locationText;
    private LocationTrack locationTrack;
    private Boolean LocationIsActive;
    private Thread th;
    SQLiteDatabase database_w;
    SQLiteDatabase database_r;

    private static final int REQUEST_COARSE_LOCATION_PERMISSION = 50;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 51;
    DecimalFormat df = new DecimalFormat("##.######");

    // Maps
    private GoogleMap map;
    private Polyline polyline;
    private PolylineOptions polyTrack;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (container != null) {
            container.removeAllViews();
        }
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        locationText = (TextView) root.findViewById(R.id.location_text);
        LocationIsActive = false;
        // Get an instance of the database
        StepAppOpenHelper databaseOpenHelper = new StepAppOpenHelper(this.getContext());
        database_w = databaseOpenHelper.getWritableDatabase();
        database_r = databaseOpenHelper.getReadableDatabase();

        Button btn = (Button) root.findViewById(R.id.btn);
        btn.setOnClickListener(this);


/*
        root.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(map != null)
                {
                    Toast.makeText(getActivity(),"Clicked!!", Toast.LENGTH_SHORT).show();
                    Log.d("LOCATION_TRACK", "click");
                    btn.performClick();
                }
            }
        });

 */
/*
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LOCATION_TRACK", "click");
                toggleTracking();
        });
 */

        // GOOGLE MAP
        // Retrieve the content view that renders the map.
        getActivity().setContentView(R.layout.fragment_map);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        View view = mapFragment.getView();
        view.setClickable(false);


//        syncMap();
        btn.performClick();
        return root;
    }

    @Override
    public void onClick(View v) {
        Log.d("LOCATION_TRACK", "click registered");

        switch (v.getId()) {
            case R.id.btn: {
                toggleTracking();
                break;
            }
            default:
                break;
        }
    }

    public void toggleTracking() {
        Log.d("LOCATION_TRACK", "click");
        Button btn = (Button) getActivity().findViewById(R.id.btn);
        LocationIsActive = !LocationIsActive;
        if (LocationIsActive) {
            Log.d("LOCATION_TRACK", "active");
            btn.setText(getString(R.string.stop_tracking));
            //                getLocation();
            getCoarseLocation();
            getFineLocation();

            // TODO: erase database at new toggle
//                    database_w.deleteRecords(this.getContext());
            int numberDeletedRecords = database_w.delete(StepAppOpenHelper.TABLE_NAME, null, null);


            locationTrack = new LocationTrack(getActivity());
            locationTrack.setTextViewToModify(locationText);

            if (locationTrack.canGetLocation()) {
                startTimerThread();
            } else {
                locationTrack.showSettingsAlert();
            }

        } else {
            Log.d("LOCATION_TRACK", "not active");
            if (th.isAlive()) th.interrupt();
            btn.setText(getString(R.string.start_tracking));
        }
    }

    @Override
    public void onDestroy() {
        if (LocationIsActive) {
            locationTrack.stopListener();
        }
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        if (LocationIsActive) {
            locationTrack.stopListener();
        }
        super.onDestroyView();
//        locationTrack.stopListener();
    }

    private void startTimerThread() {
        polyTrack = new PolylineOptions();
        th = new Thread(new Runnable() {
            private MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(locationTrack.getLatitude(), locationTrack.getLongitude()));
            private Marker marker;
            private Marker firstMarker;
            private List<Marker> markerList = new ArrayList<>();
            public void run() {
//                marker = map.addMarker(markerOptions.position(
//                        new LatLng(locationTrack.getLatitude(),
//                                locationTrack.getLongitude())));
                while (LocationIsActive) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            locationTrack = new LocationTrack(getActivity());
                            double longitude = locationTrack.getLongitude();
                            double latitude = locationTrack.getLatitude();
                            locationText.setText("Longitude: " + df.format(longitude) + "\nLatitude: " + df.format(latitude));
                            Log.d("LOCTIME", "update " + longitude + "/" + latitude);

                            Date date = new Date();
                            // Insert the data in the database
                            Timestamp timeStamp = new Timestamp(date.getTime());
                            ContentValues values = new ContentValues();
                            values.put(StepAppOpenHelper.KEY_TIMESTAMP, timeStamp.toString());
                            values.put(StepAppOpenHelper.KEY_LAT, latitude);
                            values.put(StepAppOpenHelper.KEY_LON, longitude);
                            database_w.insert(StepAppOpenHelper.TABLE_NAME, null, values);
                            // add point to polyline
                            polyTrack.add(new LatLng(latitude, longitude));
                            polyline = map.addPolyline(polyTrack);
                            // Move marker to this position

                            if (markerList != null && map != null) {
                                for (int i = 0; i < markerList.size(); i++) {
                                    markerList.get(i).remove();
                                }
                            }
                            marker = map.addMarker(new MarkerOptions().position(
                                    new LatLng(locationTrack.getLatitude(),
                                            locationTrack.getLongitude())));
                            if(firstMarker!=null){
                                markerList.add(marker);
                            }else{
                                firstMarker = marker;
                            }


                            // Move view to this position
                            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();
    }


    private void getFineLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION_PERMISSION);
        } else {
            Log.d("Fine Position", "Permission denied");
            return;
        }
    }

    private void getCoarseLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION_PERMISSION);
        } else {
            Log.d("Coarse Position", "Permission denied");
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_COARSE_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCoarseLocation();
            } else {
                Toast.makeText(getActivity(),
                        R.string.step_permission_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFineLocation();
            } else {
                Toast.makeText(getActivity(),
                        R.string.step_permission_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    // [START maps_poly_activity_on_polyline_click]

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        String[] columns = new String[]{StepAppOpenHelper.KEY_LON, StepAppOpenHelper.KEY_LAT};
        Cursor cursor = database_r.query(StepAppOpenHelper.TABLE_NAME, columns, null, null, null,
                null, StepAppOpenHelper.KEY_TIMESTAMP);

        Double firstLat = 0.0;
        Double firstLon = 0.0;
        polyTrack = new PolylineOptions();

        // iterate over returned elements
        if (cursor.moveToFirst()) {
            firstLon = Double.parseDouble(cursor.getString(0));
            firstLat = Double.parseDouble(cursor.getString(1));
            Double lon = firstLon;
            Double lat = firstLat;

            Log.d("DATA_READ", "start " + firstLon + "/" + firstLat);
            for (int index = 0; index < cursor.getCount(); index++) {
                lon = Double.parseDouble(cursor.getString(0));
                lat = Double.parseDouble(cursor.getString(1));

                polyTrack.add(new LatLng(lat, lon));
                Log.d("DATA_READ", "lat: " + lat + ", lon: " + lon);
                cursor.moveToNext();
            }
        }
        polyline = googleMap.addPolyline(polyTrack);
//        database_r.close();

        // Position the map's camera at the start
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(firstLat, firstLon), 16));
        map = googleMap;
        Log.d("DATA_READ", "end");


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);


//        map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
//        {
//            @Override
//            public void onMapClick(LatLng arg0)
//            {
//                Toast.makeText(getActivity(),"Clicked!!", Toast.LENGTH_SHORT).show();
//                Log.d("LOCATION_TRACK", "click");
//            }
//        });



    }


}


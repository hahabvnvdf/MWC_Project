package com.example.stepmapper.ui.map;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    // Views
    private TextView timestampText;
    private LocationTrack locationTrack;
    private Boolean LocationIsActive;
    private Thread th;
    private SQLiteDatabase database_w;
    private SQLiteDatabase database_r;

    private static final int REQUEST_COARSE_LOCATION_PERMISSION = 50;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 51;
    private DecimalFormat df = new DecimalFormat("##.######");

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

        timestampText = (TextView) root.findViewById(R.id.timestamp_text);
        LocationIsActive = false;
        // Get an instance of the database
        StepAppOpenHelper databaseOpenHelper = new StepAppOpenHelper(this.getContext());
        database_w = databaseOpenHelper.getWritableDatabase();
        database_r = databaseOpenHelper.getReadableDatabase();

        Button btn = (Button) root.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(map != null)
                {
                    toggleTracking();
                }
            }
        });

        getCoarseLocation();
        getFineLocation();

        // GOOGLE MAP
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        View view = mapFragment.getView();
        view.setClickable(false);

        return root;
    }

    public void toggleTracking() {
        Log.d("LOCATION_TRACK", "click");
        Button btn = (Button) getActivity().findViewById(R.id.btn);
        LocationIsActive = !LocationIsActive;
        if (LocationIsActive) {
            Log.d("LOCATION_TRACK", "active");
            btn.setText(getString(R.string.stop_tracking));
            getCoarseLocation();
            getFineLocation();

            // Erase database at new toggle
            int numberDeletedRecords = database_w.delete(StepAppOpenHelper.TABLE_NAME, null, null);
            map.clear();

            locationTrack = new LocationTrack(getActivity());
            locationTrack.setTextViewToModify(timestampText);

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
        if (th.isAlive()) th.interrupt();
    }

    @Override
    public void onDestroyView() {
        if (LocationIsActive) {
            locationTrack.stopListener();
        }
        super.onDestroyView();
        if (th.isAlive()) th.interrupt();
    }

    private void startTimerThread() {
        polyTrack = new PolylineOptions();
        th = new Thread(new Runnable() {
            private Marker marker, firstMarker;
            private List<Marker> markerList = new ArrayList<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            private String timestamp, firstTimestamp;

            public void run() {
                while (LocationIsActive) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            locationTrack = new LocationTrack(getActivity());
                            double longitude = locationTrack.getLongitude();
                            double latitude = locationTrack.getLatitude();
                            Log.d("LOCTIME", "update " + latitude + "/" + longitude);

                            Date date = new Date();
                            // Insert the data in the database
                            timestamp = format.format(date);
                            ContentValues values = new ContentValues();
                            values.put(StepAppOpenHelper.KEY_TIMESTAMP, timestamp);
                            values.put(StepAppOpenHelper.KEY_LAT, latitude);
                            values.put(StepAppOpenHelper.KEY_LON, longitude);
                            database_w.insert(StepAppOpenHelper.TABLE_NAME, null, values);
                            // add point to polyline
                            polyTrack.add(new LatLng(latitude, longitude));
                            polyline = map.addPolyline(polyTrack);
                            // Move marker to new position
                            if (markerList != null && map != null) {
                                for (int i = 0; i < markerList.size(); i++) {
                                    markerList.get(i).remove();
                                }
                            }
                            if(firstMarker!=null){
                                marker = setMarker(latitude, longitude,"End", timestamp);
                                markerList.add(marker);
                            }else{
                                // FIRST POSITION
                                firstMarker = setMarker(latitude, longitude, "Start", timestamp);
                                firstTimestamp = timestamp;
                                // Move view to this position
                                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                            }
                            timestampText.setText("Start:\t" + firstTimestamp + "\nEnd:\t\t" + timestamp);

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
            Log.d("Location_Update", "ACCESS_FINE_LOCATION Permission denied");
            return;
        }
    }

    private void getCoarseLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION_PERMISSION);
        } else {
            Log.d("Location_Update", "ACCESS_COARSE_LOCATION Permission denied");
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

    private Marker setMarker (Double lat, Double lon, String title){
        return setMarker(lat, lon, title, null);
    }


                              private Marker setMarker (Double lat, Double lon, String title, String snippet){
        if (map!=null){
            Marker marker = map.addMarker(new MarkerOptions().position(
                    new LatLng(lat, lon)));
            if (title!=null){
                marker.setTitle(title);
            }
            if (snippet!=null){
                marker.setSnippet(snippet);
            }
            return marker;
        }
        return null;
    }

    // [START maps_poly_activity_on_polyline_click]

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        String[] columns = new String[]{StepAppOpenHelper.KEY_LON, StepAppOpenHelper.KEY_LAT, StepAppOpenHelper.KEY_TIMESTAMP};
        Cursor cursor = database_r.query(StepAppOpenHelper.TABLE_NAME, columns, null, null, null,
                null, StepAppOpenHelper.KEY_TIMESTAMP);

        Double firstLat, firstLon, lat, lon;
        firstLat = firstLon = lat = lon = 0.0;
        String firstTimestamp, timestamp;
        firstTimestamp = timestamp = "";
        polyTrack = new PolylineOptions();

        // iterate over returned elements
        if (cursor.moveToFirst()) {
            firstLon = Double.parseDouble(cursor.getString(0));
            firstLat = Double.parseDouble(cursor.getString(1));
            firstTimestamp = cursor.getString(2);
            lon = firstLon;
            lat = firstLat;
            timestamp = firstTimestamp;


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

        setMarker(firstLat,firstLon, "Start", firstTimestamp);
        setMarker(lat, lon, "End", timestamp);
        timestampText.setText("Start:\t" + firstTimestamp + "\nEnd:\t\t" + timestamp);


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getCoarseLocation();
            getFineLocation();
            return;
        }
        map.setMyLocationEnabled(true);
    }

}


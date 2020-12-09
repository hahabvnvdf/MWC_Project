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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.stepmapper.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

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
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (container != null) {
            container.removeAllViews();
        }
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        locationText = (TextView) root.findViewById(R.id.location_text);
        Button btn = (Button)root.findViewById(R.id.btn);
        LocationIsActive  = false;
        // Get an instance of the database
        StepAppOpenHelper databaseOpenHelper = new StepAppOpenHelper(this.getContext());
        database_w = databaseOpenHelper.getWritableDatabase();
        database_r = databaseOpenHelper.getReadableDatabase();

        // TODO: erase database at new toggle
//         database_w.deleteRecords(this.getContext());

        btn.setText(getString(R.string.start_tracking));


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button)getActivity().findViewById(R.id.btn);
                LocationIsActive  = !LocationIsActive;
                if (LocationIsActive){
                    btn.setText(getString(R.string.stop_tracking));
    //                getLocation();
                    getCoarseLocation();
                    getFineLocation();
                    locationTrack = new LocationTrack(getActivity());
                    locationTrack.setTextViewToModify(locationText);

                    if (locationTrack.canGetLocation()) {
                        startTimerThread();
                    } else {
                        locationTrack.showSettingsAlert();
                    }

                } else{
                    if(th.isAlive()) th.interrupt();
                    btn.setText(getString(R.string.start_tracking));
                }
            }


        });

        // GOOGLE MAP
        // Retrieve the content view that renders the map.
        getActivity().setContentView(R.layout.fragment_map);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("DATA_READ", "end2");

        return root;
    }

    @Override
    public void onDestroy() {
        locationTrack.stopListener();
        super.onDestroy();
    }
    @Override
    public void onDestroyView (){
        super.onDestroyView();
        locationTrack.stopListener();
    }



    private void startTimerThread() {
        th = new Thread(new Runnable() {
            public void run() {
                Log.d("LOCTIME", "updating");
                while (LocationIsActive) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            locationTrack = new LocationTrack(getActivity());
                            double longitude = locationTrack.getLongitude();
                            double latitude = locationTrack.getLatitude();
                            locationText.setText("Longitude: " + df.format(longitude) + "\nLatitude: " + df.format(latitude));
                            Log.d("LOCTIME", "update "+longitude+"/"+latitude);

                            Date date = new Date();
                            // Insert the data in the database
                            Timestamp timeStamp = new Timestamp(date.getTime());
                            ContentValues values = new ContentValues();
                            values.put(StepAppOpenHelper.KEY_TIMESTAMP, timeStamp.toString());
                            values.put(StepAppOpenHelper.KEY_LAT, latitude);
                            values.put(StepAppOpenHelper.KEY_LON, longitude);
                            database_w.insert(StepAppOpenHelper.TABLE_NAME, null, values);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
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
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCoarseLocation();
            } else {
                Toast.makeText(getActivity(),
                        R.string.step_permission_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if(grantResults.length > 0
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
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Add polylines to the map.
        // Polylines are useful to show a route or some other connection between points.

        Cursor cursor = database_r.query(StepAppOpenHelper.TABLE_NAME, null, null, null, null,
                null, StepAppOpenHelper.KEY_TIMESTAMP );

        Polyline polyline1;
        Double firstLat = 0.0;
        Double firstLon = 0.0;
        firstLon = Double. parseDouble(cursor.getString(1));
        firstLat = Double. parseDouble(cursor.getString(2));
        PolylineOptions polyTrack = new PolylineOptions();

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
//            polyTrack.clickable(true).add(new LatLng(
//                                    Double. parseDouble(cursor.getString(1)),
//                                    Double. parseDouble(cursor.getString(2))));
            Log.d("DATA_READ", "lat: "+cursor.getString(1)+", lon: "+cursor.getString(2));
            cursor.moveToNext();
        }
        polyline1 = googleMap.addPolyline(polyTrack);
        database_r.close();


/*
        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(-35.016, 143.321),
                        new LatLng(-34.747, 145.592),
                        new LatLng(-34.364, 147.891),
                        new LatLng(-33.501, 150.217),
                        new LatLng(-32.306, 149.248),
                        new LatLng(-32.491, 147.309)));
*/
        // Position the map's camera near Alice Springs in the center of Australia,
        // and set the zoom factor so most of Australia shows on the screen.
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-35.016, 143.321), 4));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(firstLat, firstLon), 15));
        // Set listeners for click events.
        googleMap.setOnPolylineClickListener(this);
        Log.d("DATA_READ", "end");
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }

        Toast.makeText(getActivity(), "Route type " + polyline.getTag().toString(),
                Toast.LENGTH_SHORT).show();
    }
    // [END maps_poly_activity_on_polyline_click]



}


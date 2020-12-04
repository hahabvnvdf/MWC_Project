package com.example.stepmapper.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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

public class MapFragment extends Fragment {

    // Views
    private Button mLocationButton;
    private TextView locationText;
    private LocationTrack locationTrack;
    private Boolean LocationIsActive;

    private static final int REQUEST_COARSE_LOCATION_PERMISSION = 50;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 51;

    private Handler handler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (container != null) {
            container.removeAllViews();
        }
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        locationText = (TextView) root.findViewById(R.id.location_text);
        Button btn = (Button)root.findViewById(R.id.btn);
//        LocationIsActive  = false;

        handler = new Handler();


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                LocationIsActive  = !LocationIsActive;
//                getLocation();
                getCoarseLocation();
                getFineLocation();
                locationTrack = new LocationTrack(getActivity());
                locationTrack.setTextViewToModify(locationText);

                if (locationTrack.canGetLocation()) {
//                    double longitude = locationTrack.getLongitude();
//                    double latitude = locationTrack.getLatitude();
//                    locationText.setText("Longitude: " + Double.toString(longitude) + "\nLatitude: " + Double.toString(latitude));
                    startCounting();


//                    if(LocationIsActive) {
//                        mLocationButton.setText(R.string.stop_tracking);
//                    }else{
//                        mLocationButton.setText(R.string.start_tracking);
//                    }

                } else {
                    locationTrack.showSettingsAlert();
                }
            }


        });





        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }

    private void startCounting() {
        handler.post(run);
    }
    private Runnable run = new Runnable() {
        @Override
        public void run() {
//                number++;
//                tvFragment.setText(number);
            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();
            locationText.setText("Longitude: " + Double.toString(longitude) + "\nLatitude: " + Double.toString(latitude));

            handler.postDelayed(this, 1000);
        }
    };



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

}

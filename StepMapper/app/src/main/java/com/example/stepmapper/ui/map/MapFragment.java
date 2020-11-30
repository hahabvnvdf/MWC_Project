package com.example.stepmapper.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.stepmapper.R;

public class MapFragment extends Fragment {
    LocationTrack locationTrack;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (container != null) {
            container.removeAllViews();
        }
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        final TextView textView = (TextView) root.findViewById(R.id.text_map);

        Button btn = (Button)root.findViewById(R.id.btn);

//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                locationTrack = new LocationTrack(getActivity());
//
//                if (locationTrack.canGetLocation()) {
//                    double longitude = locationTrack.getLongitude();
//                    double latitude = locationTrack.getLatitude();
//                    Toast.makeText(getActivity(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
//                } else {
//                    locationTrack.showSettingsAlert();
//                }
//            }
//        });


        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }

}
// Base code from https://www.journaldev.com/13325/android-location-api-tracking-gps
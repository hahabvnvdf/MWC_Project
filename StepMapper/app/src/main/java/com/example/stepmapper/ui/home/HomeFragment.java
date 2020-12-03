package com.example.stepmapper.ui.home;

import android.content.Context;
import android.content.ContentValues;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.stepmapper.StepAppOpenHelper;
import com.example.stepmapper.FirebaseDatabaseHelper;
import com.example.stepmapper.ui.user.LoginFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.example.stepmapper.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class HomeFragment extends Fragment {
    private static int stepsCompleted;
    public Context context;
    MaterialButtonToggleGroup materialButtonToggleGroup;

    private FirebaseAuth firebaseAuth;
//    private FirebaseUser user = firebaseAuth.getCurrentUser();
    // Text view variables
    public TextView stepsCountTextView;

    private SensorEventListener listener;

    private Sensor mSensorACC;
    private SensorManager mSensorManager;

    public static void setStepsCompleted(int stepsCompleted1) {
        stepsCompleted = stepsCompleted1;
    }
    public static int getStepsCompleted() {
        return stepsCompleted;
    }


    // Progress Bar variable
    public ProgressBar stepsCountProgressBar;

    //Progress Count
    private int progressCount;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_home, container, false);
        // Get the number of steps stored in the current date
        Date cDate = new Date();

        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        stepsCountTextView = (TextView) root.findViewById(R.id.stepsCount);
        stepsCountProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        stepsCountProgressBar.setMax(100);

        FirebaseDatabaseHelper.loadSingleRecord(fDate);

        Log.d("Main", fDate);

        stepsCountTextView.setText(String.valueOf(stepsCompleted));
        stepsCountProgressBar.setProgress(stepsCompleted);

//        FirebaseDatabaseHelper.loadSingleRecord1(fDate);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorACC = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        // instantiate the StepCounterListener
        listener = new StepCounterListener(stepsCountTextView, stepsCountProgressBar, fDate);
        // Toggle group button

        //Place code related to Start button
        Toast.makeText(getContext(), "START", Toast.LENGTH_SHORT).show();

        // Check if the Accelerometer sensor exists
        if(mSensorACC != null){
            //register the ACC listener
            mSensorManager.registerListener(listener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);

        }
        else{
            Toast.makeText(getContext(), R.string.acc_not_available, Toast.LENGTH_SHORT).show();
        }
        materialButtonToggleGroup = (MaterialButtonToggleGroup) root.findViewById(R.id.toggleButtonGroup);
        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (group.getCheckedButtonId() == R.id.toggleStart) {

                    //Place code related to Start button
                    Toast.makeText(getContext(), "START", Toast.LENGTH_SHORT).show();

                    // Check if the Accelerometer sensor exists
                    if(mSensorACC != null){
                        //register the ACC listener
                        mSensorManager.registerListener(listener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);

                    }
                    else{
                        Toast.makeText(getContext(), R.string.acc_not_available, Toast.LENGTH_SHORT).show();
                    }


                } else if (group.getCheckedButtonId() == R.id.toggleStop) {
                    //Place code related to Stop button
                    Toast.makeText(getContext(), "STOP", Toast.LENGTH_SHORT).show();

                    // Unregister the listener
                    mSensorManager.unregisterListener(listener);
                }
            }
        });
        //////////////////////////////////////

        return root;


    }
    @Override
    public void onDestroyView (){
        super.onDestroyView();
        mSensorManager.unregisterListener(listener);
    }
}

// Sensor event listener
class StepCounterListener implements SensorEventListener {
    public Context context;
    private long lastUpdate = 0;

    // ACC Step counter

    ArrayList<Integer> mACCSeries = new ArrayList<Integer>();
    ArrayList<String> mTimeSeries = new ArrayList<String>();
    private double accMag = 0d;
    private int lastXPoint = 1;
    int stepThreshold = 6;

    // Android step detector
    int mAndroidStepCount = 0;
    int progressCount;

    // TextView
    TextView stepsCountTextView;
    ProgressBar stepsCountProgressBar;
    public int mACCStepCounter;
    //
    public String timestamp;
    public String day;
    public String hour;
    public String fDate;



    public StepCounterListener(TextView tv, ProgressBar pb, String date){
        stepsCountTextView = tv;
        stepsCountProgressBar = pb;
        fDate = date;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {

            // Get the sensor type
            case Sensor.TYPE_LINEAR_ACCELERATION:

                //  Get sensor's values
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                // Timestamp
                long timeInMillis = System.currentTimeMillis() + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000;

                // Convert the timestamp to date
                SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String date = jdf.format(timeInMillis);



                // Get the date, the day and the hour
                timestamp = date;
                day = date.substring(0,10);
                hour = date.substring(11,13);
                //Update the Magnitude series
                accMag = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
                mACCSeries.add((int) accMag);
                mTimeSeries.add(timestamp);

                peakDetection();
                if(mACCStepCounter==118)
                {

                }
                break;

        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




    private void peakDetection() {

        int windowSize = 20;
        
        /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer, Mladenov et al.
         */
        int highestValX = mACCSeries.size(); // get the length of the series
        if (highestValX - lastXPoint < windowSize) { // if the segment is smaller than the processing window skip it
            return;
        }

        List<Integer> valuesInWindow = mACCSeries.subList(lastXPoint,highestValX);
        List<String> timesInWindow = mTimeSeries.subList(lastXPoint,highestValX);

        lastXPoint = highestValX;

        int forwardSlope = 0;
        int downwardSlope = 0;

        List<Integer> dataPointList = new ArrayList<Integer>();
        List<String> timePointList = new ArrayList<String>();
        FirebaseDatabaseHelper.loadSingleRecord(fDate);
        mACCStepCounter = HomeFragment.getStepsCompleted();

        Log.d("Main", String.valueOf(mACCStepCounter));
        stepsCountTextView.setText(String.valueOf(mACCStepCounter));
        //update the ProgressBar
        stepsCountProgressBar.setProgress(mACCStepCounter);

        for (int p =0; p < valuesInWindow.size(); p++){
            dataPointList.add(valuesInWindow.get(p));
            timePointList.add(timesInWindow.get(p));
        }

        for (int i = 0; i < dataPointList.size(); i++) {
            if (i == 0) {
            }
            else if (i < dataPointList.size() - 1) {
                forwardSlope = dataPointList.get(i + 1) - dataPointList.get(i);
                downwardSlope = dataPointList.get(i)- dataPointList.get(i - 1);

                if (forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i) > stepThreshold ) {
                    mACCStepCounter += 1;

                    Log.d("ACC STEPS: ", String.valueOf(mACCStepCounter));

                    //update the text view
                    stepsCountTextView.setText(String.valueOf(mACCStepCounter));
                    //update the ProgressBar
                    stepsCountProgressBar.setProgress(mACCStepCounter);

                    FirebaseDatabaseHelper.insertData(day, hour, timePointList.get(i), mACCStepCounter);

//                    //Insert the data in the database
//                    ContentValues values = new ContentValues();
//                    values.put(StepAppOpenHelper.KEY_TIMESTAMP, timePointList.get(i));
//                    values.put(StepAppOpenHelper.KEY_DAY, day);
//                    values.put(StepAppOpenHelper.KEY_HOUR, hour);
//                    database.insert(StepAppOpenHelper.TABLE_NAME, null, values);

                }
            }
        }
    }

}

package com.example.stepmapper.ui.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.stepmapper.MainActivity;
import com.example.stepmapper.FirebaseDatabaseHelper;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.example.stepmapper.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {
    private static LinearLayout cdialog;
    private static Button txtClose;
    private static Button goalTextView;
    private static int stepsCompleted;
    public static String goal;
    public Context context;
    MaterialButtonToggleGroup materialButtonToggleGroup;

    // Text view variables
    public TextView stepsCountTextView;

    private SensorEventListener listener;

    private Sensor mSensorACC;
    private SensorManager mSensorManager;

    public String currentGoal;
//    public String goal;


    public static void setStepsCompleted(int stepsCompleted1) {
        stepsCompleted = stepsCompleted1;
    }
    public static int getStepsCompleted() {
        return stepsCompleted;
    }

    // Progress Bar variable
    public static ProgressBar stepsCountProgressBar;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        cdialog = (LinearLayout) root.findViewById(R.id.congratsDialog);
        txtClose = (Button) root.findViewById(R.id.txtClose);
        // Get the number of steps stored in the current date
        Date cDate = new Date();
        //set the goal
        FirebaseDatabaseHelper.setGoalInit("100");
        goalTextView = (Button) root.findViewById(R.id.textViewGoal);
        currentGoal = goalTextView.getText().toString().split(" : ")[1];
        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        stepsCountTextView = (TextView) root.findViewById(R.id.stepsCount);
        stepsCountProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        FirebaseDatabaseHelper.loadSingleRecord(fDate);

        stepsCountTextView.setText(String.valueOf(stepsCompleted));
        stepsCountProgressBar.setProgress(stepsCompleted);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorACC = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // instantiate the StepCounterListener
        listener = new StepCounterListener(stepsCountTextView, stepsCountProgressBar, fDate);
        final Button startBtn = (Button) root.findViewById(R.id.toggleStart);
        final Button stopBtn = (Button) root.findViewById(R.id.toggleStop);
        final LinearLayout setGoal = (LinearLayout) root.findViewById(R.id.setGoal);
        final Button closeGoal = (Button) root.findViewById(R.id.goalClose);
        final Button setGoalBtn = (Button) root.findViewById(R.id.goalSetBtn);
        final TextInputEditText goalEditText = (TextInputEditText) root.findViewById(R.id.textGoal);


        goalTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGoal.setVisibility(View.VISIBLE);
            }
        });

        closeGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGoal.setVisibility(View.INVISIBLE);
            }
        });

        setGoalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goalEditText.getText() != null) {
                    goal = goalEditText.getText().toString();
                    goalTextView.setText("Goal : " + goal);
                    FirebaseDatabaseHelper.setGoal(goal);
                    hideKeyboardFrom(getContext(), root);
                    Toast.makeText(getContext(), "Goal is set to " + goal, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Goal is not set", Toast.LENGTH_SHORT).show();
                }
                setGoal.setVisibility(View.INVISIBLE);
            }
        });

        // Check if the Accelerometer sensor exists
        if(mSensorACC != null){
            //register the ACC listener
            mSensorManager.registerListener(listener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(getContext(), "The step counter is STARTED", Toast.LENGTH_SHORT).show();
            startBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            stopBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
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
                    Toast.makeText(getContext(), "The step counter is STARTED", Toast.LENGTH_SHORT).show();
                    // Check if the Accelerometer sensor exists
                    if(mSensorACC != null){
                        //register the ACC listener
                        mSensorManager.registerListener(listener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);
                        startBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        stopBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    }
                    else{
                        Toast.makeText(getContext(), R.string.acc_not_available, Toast.LENGTH_SHORT).show();
                    }

                } else if (group.getCheckedButtonId() == R.id.toggleStop) {
                    Toast.makeText(getContext(), "The step counter is STOP", Toast.LENGTH_SHORT).show();
                    mSensorManager.unregisterListener(listener);
                    startBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    stopBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });
        return root;
    }

    static void ShowCongrats() {

        cdialog.setVisibility(View.VISIBLE);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cdialog.setVisibility(View.INVISIBLE);
            }
        });
    }

    public static void setGoal(String goal){
        goalTextView.setText("Goal : " + goal);
        stepsCountProgressBar.setMax(Integer.parseInt(goal));
    }

    @Override
    public void onDestroyView (){
        super.onDestroyView();
        mSensorManager.unregisterListener(listener);
    }
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    // TextView
    TextView stepsCountTextView;
    ProgressBar stepsCountProgressBar;
    public int mACCStepCounter;
    public String timestamp;
    public String day;
    public String hour;
    public String fDate;
    public View v;

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

                break;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void peakDetection() {

        MainActivity mm = new MainActivity();
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
        stepsCountTextView.setText(String.valueOf(mACCStepCounter));
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
                    //When Goal is reached call the Congrats activity
                    if(mACCStepCounter == Integer.parseInt(HomeFragment.goal))
                    {
                        HomeFragment.ShowCongrats();
                    }
                    //update the text view
                    stepsCountTextView.setText(String.valueOf(mACCStepCounter));
                    //update the ProgressBar
                    stepsCountProgressBar.setProgress(mACCStepCounter);
                    //insert the steps in firebase
                    FirebaseDatabaseHelper.insertData(day, hour, timePointList.get(i), mACCStepCounter);
                }
            }
        }
    }

}

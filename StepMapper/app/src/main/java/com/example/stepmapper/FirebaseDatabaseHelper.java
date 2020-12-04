package com.example.stepmapper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.stepmapper.ui.home.HomeFragment;
import com.example.stepmapper.ui.report.ReportFragment;
import com.example.stepmapper.ui.user.UserData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseDatabaseHelper{

    private static DatabaseReference mDatabaseReference;
    private static int numSteps;


    public FirebaseDatabaseHelper(DatabaseReference databaseReference) {
        mDatabaseReference = databaseReference;
    }

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private static UserData userData = new UserData();

    public static void insertData(final String day, final String hour, final String timeS, final int mACCStepCounter){
        if (firebaseAuth.getCurrentUser() != null) {
            String username = firebaseAuth.getCurrentUser().getEmail().replace(".", "");
//            Log.d("User", username);
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("UserData").child("Step Count").child(username+" : " +firebaseAuth.getCurrentUser().getUid());
            String stepCount = Integer.toString(mACCStepCounter);
            userData.setStepCount(stepCount);
            userData.setTimeStamp(timeS);
            mDatabaseReference.child(day).child(hour).setValue(userData);
        }
    }

    private static void getSingleRecord(DataSnapshot dataSnapshot) {
        numSteps = 0;
        for (DataSnapshot child: dataSnapshot.getChildren()) {
            if (child != null){
                numSteps = Integer.parseInt(child.getValue().toString());
            }
            HomeFragment.setStepsCompleted(numSteps);
            break;
        }
    }

    public static void loadSingleRecord(final String date){
        if (firebaseAuth.getCurrentUser() != null) {
            String username = firebaseAuth.getCurrentUser().getEmail().replace(".", "");
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("UserData").child("Step Count").child(username+" : " +firebaseAuth.getCurrentUser().getUid());
            mDatabaseReference.child(date).orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    getSingleRecord(snapshot);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    getSingleRecord(snapshot);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    private static void getStepByHour(DataSnapshot dataSnapshot) {
        Map<String, String> newObj = (Map<String, String>) dataSnapshot.getValue();
        Integer hour = 0;
        Integer steps = 0;
        if(dataSnapshot.getKey() != null && newObj.get("stepCount") != null){
            hour = Integer.parseInt(dataSnapshot.getKey());
            steps = Integer.parseInt(newObj.get("stepCount"));
            Map<Integer, Integer> stepsByHour = new HashMap<>();
            stepsByHour.put(hour, steps);
            ReportFragment.setStepsByHour(stepsByHour);
        }


    }

    public static void loadStepsByHour(final String date){
        if (firebaseAuth.getCurrentUser() != null) {

            String username = firebaseAuth.getCurrentUser().getEmail().replace(".", "");
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("UserData").child("Step Count").child(username+" : " +firebaseAuth.getCurrentUser().getUid());
            mDatabaseReference.child(date).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                   getStepByHour(snapshot);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    getStepByHour(snapshot);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private static void getStepsByDay(DataSnapshot dataSnapshot){
        Log.d("Here", dataSnapshot.getKey());
//        Log.d("Here", String.valueOf(dataSnapshot.getChildrenCount()));
        String date = dataSnapshot.getKey();
        Integer steps = 0;
        int count = 0;

        for (DataSnapshot child: dataSnapshot.getChildren()) {
            if (child != null){
                if(count == dataSnapshot.getChildrenCount()-1){
                    Log.d("Here", child.child("stepCount").getValue().toString());
                    steps = Integer.parseInt(child.child("stepCount").getValue().toString());
                    Map<String, Integer> stepsByDay = new HashMap<>();
                    stepsByDay.put(date, steps);
                    ReportFragment.setStepsByDay(stepsByDay);
                    ReportFragment.setStepsByHourCheck();
                }else{
                    count+=1;
                }
            }
        }
    }

    public static void loadStepsByDay(){
        if (firebaseAuth.getCurrentUser() != null) {
            String username = firebaseAuth.getCurrentUser().getEmail().replace(".", "");
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("UserData").child("Step Count").child(username+" : " +firebaseAuth.getCurrentUser().getUid());
            mDatabaseReference.orderByKey().addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    getStepsByDay(snapshot);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    getStepsByDay(snapshot);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}

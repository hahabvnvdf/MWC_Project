package com.example.stepmapper;

import android.app.Activity;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.stepmapper.ui.home.HomeFragment;
import com.example.stepmapper.ui.user.UserData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.BreakIterator;

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
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("UserData").child("Step Count").child("User: " +firebaseAuth.getCurrentUser().getUid());
            String stepCount = Integer.toString(mACCStepCounter);
            userData.setStepCount(stepCount);
            userData.setTimeStamp(timeS);
            mDatabaseReference.child(day).child(hour).setValue(userData);
        }
    }

    private static void fetchClientData(DataSnapshot dataSnapshot) {
        numSteps = 0;
        for (DataSnapshot child: dataSnapshot.getChildren()) {
            numSteps = Integer.parseInt(child.getValue().toString());
            HomeFragment.setStepsCompleted(numSteps);

            break;
        }
    }

    public static int loadSingleRecord(final String date){
        if (firebaseAuth.getCurrentUser() != null) {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("UserData").child("Step Count").child("User: " +firebaseAuth.getCurrentUser().getUid());
            mDatabaseReference.child(date).orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    fetchClientData(snapshot);

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    fetchClientData(snapshot);
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
        return numSteps;
    }
}

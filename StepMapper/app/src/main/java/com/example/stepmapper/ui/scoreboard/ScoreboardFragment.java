package com.example.stepmapper.ui.scoreboard;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.stepmapper.FirebaseDatabaseHelper;
import com.example.stepmapper.R;
import com.example.stepmapper.ui.report.ReportFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScoreboardFragment extends Fragment {
    private static DatabaseReference mDatabaseReference;
    Date cDate = new Date();
    String current_time = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

    public static Map<String, String> getScoreBoard() {
        return scoreBoard;
    }

    public static void setScoreBoard(Map<String, String> scoreBoard) {
        ScoreboardFragment.scoreBoard.putAll(scoreBoard);
    }
    public static void clearScoreBoard() {
        ScoreboardFragment.scoreBoard.clear();
    }

    public static Map<String, String> scoreBoard = new HashMap<>();

    public static String getUser() {
        return user;
    }

    public static String getSteps() {
        return steps;
    }

    private static String user = "";

    public static void setUser(String user) {
        ScoreboardFragment.user = user;
    }

    public static void setSteps(String steps) {
        ScoreboardFragment.steps = steps;
    }

    private static String steps = "";



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (container != null) {
            container.removeAllViews();
        }
        final View root = inflater.inflate(R.layout.fragment_scoreboard, container, false);
        final TextInputEditText emailEditText = (TextInputEditText) root.findViewById(R.id.ip_addFriend);

        Button buttonAddFriend = root.findViewById(R.id.btn_addFriend);

        addTableRow(root,ScoreboardFragment.getScoreBoard());
        buttonAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEditText.getText() != null) {
                    String email = emailEditText.getText().toString();
                    email = email.replace(".", "");
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("UserID").child(email).child("Self Info");
                    final String finalEmail = email;
                    mDatabaseReference.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String uid = snapshot.getValue().toString();
                            String newEmail = finalEmail + " : " + uid;
                            FirebaseDatabaseHelper.addFriendFromEmail(newEmail, finalEmail, uid);
                            FirebaseDatabaseHelper.getAllFriends(current_time);
                            addTableRow(root,ScoreboardFragment.getScoreBoard());
                            emailEditText.setText("");
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String uid = snapshot.getValue().toString();
                            String newEmail = finalEmail + " : " + uid;
                            FirebaseDatabaseHelper.addFriendFromEmail(newEmail, finalEmail, uid);
                            FirebaseDatabaseHelper.getAllFriends(current_time);
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

//                    FirebaseDatabaseHelper.addFriendFromEmail(email);
//                    Log.d("Email",  email);
                }
            }
        });
        return root;
    }

    public void addTableRow(View root, final Map<String, String> friends){
        final TableLayout tl = (TableLayout) root.findViewById(R.id.tableFriend);
        tl.removeAllViews();
        new Handler().postDelayed(new Runnable() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void run() {
                TableRow tr_head = new TableRow(getActivity());
                tr_head.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                //tr_head.setBackgroundColor(R.color.colorAccent);
                tr_head.setPadding(0,0,0,30);
                TextView tv0 = new TextView(getActivity());
                tv0.setText("Friends");
                tv0.setTextColor(R.color.colorPrimary);
                //tv0.setTextColor(this.getResources().getColor(R.color.colorPrimary));
                tv0.setGravity(Gravity.CENTER_HORIZONTAL);
                tv0.setTextSize(25);
                tv0.setTypeface(null, Typeface.BOLD);
                TextView tv1 = new TextView(getActivity());
                tv1.setText("Steps");
                tv1.setTextColor(R.color.colorPrimary);
                tv1.setGravity(Gravity.CENTER_HORIZONTAL);
                tv1.setTextSize(25);
                tv1.setTypeface(null, Typeface.BOLD);
                tr_head.addView(tv0);
                tr_head.addView(tv1);
                tl.addView(tr_head);
                for (Map.Entry<String, String> friend : friends.entrySet()) {
                    String k = friend.getKey();
                    String v = friend.getValue();

                    TableRow tr_head1 = new TableRow(getActivity());
                    tr_head1.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT));

                    TextView tv2 = new TextView(getActivity());
                    tv2.setText(k.split("@")[0]);
                    tv2.setTextColor(Color.parseColor("#ff0000"));
                    tv2.setGravity(Gravity.CENTER_HORIZONTAL);
                    tv2.setTextSize(20);
                    tv2.setPadding(0,20,0,20);
                    //tv2.setBackgroundColor(R.color.colorAccent);
                    TextView tv3 = new TextView(getActivity());
                    tv3.setText(v);
                    tv3.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv3.setGravity(Gravity.CENTER_HORIZONTAL);
                    tv3.setTextSize(20);
                    tv3.setPadding(0,20,0,20);
                    //tv3.setBackgroundColor(R.color.colorAccent);
                    tr_head1.addView(tv2);
                    tr_head1.addView(tv3);
                    tl.addView(tr_head1);
                }
            }
        }, 500);
    }
}
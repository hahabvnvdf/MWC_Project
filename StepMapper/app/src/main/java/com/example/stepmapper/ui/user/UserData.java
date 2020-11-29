package com.example.stepmapper.ui.user;

import com.google.firebase.Timestamp;

import java.sql.Time;
import java.util.Date;

public class UserData {
    private String timeStamp;

    public String getStepCount() {
        return stepCount;
    }

    public void setStepCount(String stepCount) {
        this.stepCount = stepCount;
    }

    private String stepCount;


    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public UserData(){

    }

    public UserData(String timeStamp, String stepCount){
        this.timeStamp = timeStamp;
        this.stepCount = stepCount;
    }



}

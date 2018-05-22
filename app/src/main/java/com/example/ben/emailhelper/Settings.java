package com.example.ben.emailhelper;

public class Settings {
    public int updateFrequency;
    public int updateTimePeriod;
    public boolean showNotifications;

    //Constants
    public static final int SECONDS = 0;
    public static final int MINUTES = 1;
    public static  final int HOURS = 2;
    public static final int DAYS = 3;

    public Settings() {
        updateFrequency = 30;
        updateTimePeriod = MINUTES;
        showNotifications = true;
    }


    //getters
    public int getUpdateFrequency(){
        return updateFrequency;
    }
    public int getUpdateTimePeriod(){
        return  updateTimePeriod;
    }
    public boolean isShowNotifications(){return showNotifications;}

    //setters
    public void setUpdateFrequency(int x){
        updateFrequency = x;
    }
    public void setUpdateTimePeriod(int x){
        updateTimePeriod = x;
    }
    public void setShowNotifications(boolean x){showNotifications = x;}
}

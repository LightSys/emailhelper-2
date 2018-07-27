package org.lightsys.emailhelper;

/**
 * Created by nicholasweg on 6/26/17.
 */

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonMethods {


    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat completeFormat = new SimpleDateFormat("HH:mma d MMM yyyy");

    private static DateFormat visibleYear = new SimpleDateFormat("yyyy");
    private static DateFormat visibleMonth = new SimpleDateFormat("MMM");
    private static DateFormat visibleDate = new SimpleDateFormat("MMM d");
    private static DateFormat visibleTime = new SimpleDateFormat("HH:mma");

    public static String getTime(Date date){return visibleTime.format(date);}
    public static String getDate(Date date){return visibleDate.format(date);}
    public static String getMonth(Date date){return visibleMonth.format(date);}
    public static String getYear(Date date){return visibleYear.format(date);}

    public static Date getCurrentTime(){return Calendar.getInstance().getTime();}
    public static Date stringToDate(String date){
        try {
            return completeFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String dateToString(Date date){return completeFormat.format(date);}

    public static void textViewMinimize(TextView textView){
        textView.setVisibility(View.INVISIBLE);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(0,0);
        textView.setLayoutParams(size);
    }
    public static void textViewExpand(TextView textView){
        textView.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(size);
    }

    public static boolean checkEmail(String email) {
        boolean valid = true;
        if(!email.contains("@")){
            valid = false;
        }
        String temp = email.substring(email.indexOf("@"));
        if(!temp.contains(".")){
            valid = false;
        }
        if(!hasRecognizedProvider(temp)){
            valid = false;
        }
        return valid;
    }
    private static boolean hasRecognizedProvider(String emailEnd) {
        if(emailEnd.contains("gmail")){
            return true;
        }
        else if(emailEnd.contains("yahoo")){
            return true;
        }
        else if(emailEnd.contains("outlook")){
            return true;
        }
        else if(emailEnd.contains("aol")){
            return true;
        }
        else if(emailEnd.contains("live")){
            return true;
        }
        else if(emailEnd.contains("icloud")){//Don't simplify. This is more readable
            return true;
        }
        else {
            return false;
        }
    }

    public static String getExtension(String filePath) {
        int temp = filePath.length()-1;
        for(int i = temp;i>0;i--){
            if(filePath.charAt(i)=='.'){
                temp = i;
                i = 0;
            }
        }
        filePath = filePath.substring(temp+1);
        return filePath;
    }




    //Constants used in multiple different classes
    public static final int CONVERSATION_DELETED = 65483;
    public static final int CHECK_FOR_DELETION = 28694;
    public static final int CAMERA_REQUEST_CODE = 453;
    public static final int SHARED_PREFERENCES_DEFAULT_MODE = 0;

    public static final int CHECK_FOR_CONTACT_ADDITION = 8295;
    public static final int NEW_CONTACT_ADDED = 6030;
    public static final int DOES_CONTACT_CHANGE = 3030;
    public static final int CONTACT_CHANGED = 28430;
    public static final int CONTACT_NOT_CHANGED = 3036;



}
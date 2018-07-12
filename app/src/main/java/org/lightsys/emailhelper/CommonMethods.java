package org.lightsys.emailhelper;

/**
 * Created by nicholasweg on 6/26/17.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonMethods {

    private static DateFormat displayDate = new SimpleDateFormat("MMM d");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");

    public static String getCurrentTime() {return timeFormat.format(Calendar.getInstance().getTime());}
    public static String getCurrentDate() {return displayDate.format(Calendar.getInstance().getTime());}
    public static String getTime(Date date){return timeFormat.format(date);}
    public static String getDate(Date date){return displayDate.format(date);}

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

    //Constants used in multiple different classes
    public static final int CONVERSATION_DELETED = 65783;
    public static final int CHECK_FOR_DELETION = 28694;
    public static final int CAMERA_REQUEST_CODE = 453;
    public static final int SHARED_PREFERENCES_DEFAULT_MODE = 0;

}
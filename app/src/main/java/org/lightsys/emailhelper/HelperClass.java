package org.lightsys.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.app.Service;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**************************************************************************************************
 *                                 Created by nicholasweg on 6/22/17.                             *
 *  I made this because I saw it in a different app project and liked the idea. If there is data  *
 *  that gets used a lot in different classes you can save it here and call it from that. If we   *
 *  end up needing to save different types of IMAP hosts and ports, this is where it should be.   *
 **************************************************************************************************/

public class HelperClass extends AppCompatActivity{
    public static boolean savedCredentials = false;
    public static String Email    = "";
    public static String Password = "";
    public static String incoming  = "";
    public static String outgoing   = "";

    public HelperClass(String email, String password, boolean saved){
        setEmail(email);
        Password = password;
        savedCredentials = saved;
    }

    public static void setEmail(String email){
        Email   = email;
        incoming = getIncoming(email);
        outgoing  = getOutgoing(email);
    }

    private static String getIncoming(String email) {
        if(email.contains("@gmail.com")){//tested
            return "smtp.gmail.com";
        }
        if(email.contains("@yahoo.com")){//tested
            return "imap.mail.yahoo.com";
        }
        if(email.contains("@outlook.com")||email.contains("@hotmail.com")){//outlook was tested
            return "imap-mail.outlook.com";
        }
        if(email.contains("@aol.com")){
            return "imap.aol.com";
        }
        if(email.contains("@icloud.com")){
            return "imap.mail.me.com";
        }
        if(email.contains("@live.com")){
            return "pop3.live.com";//not sure this one works
        }


        return null;
    }

    private static String getOutgoing(String email) {
        if(email.contains("@gmail.com")){//tested
            return "smtp.gmail.com";
        }
        if(email.contains("@yahoo.com")){//tested
            return "smtp.mail.yahoo.com";
        }
        if(email.contains("@outlook.com")||email.contains("@hotmail.com")){//outlook was tested
            return "imap-mail.outlook.com";
        }
        if(email.contains("@aol.com")){
            return "smtp.aol.com";
        }
        if(email.contains("@icloud.com")){
            return "stmp.mail.me.com";
        }
        if(email.contains("@live.com")){
            return "stmp.live.com";//not sure this one works
        }
        return null;

    }

}

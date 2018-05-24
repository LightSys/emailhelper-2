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

    public static String _Email = "";
    public static String _Password = "";
}

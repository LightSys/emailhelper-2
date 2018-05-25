package org.lightsys.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import org.lightsys.emailhelper.Contact.ContactFragment;
import org.lightsys.emailhelper.Conversation.ConversationFragment;

import java.io.IOException;
import java.util.Calendar;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;

public class MainActivity extends AppCompatActivity {

    // TODO: Remove or figure out how to make the DividerItemDecoration work
    // TODO: Add multiple mail services
    // TODO: Polling or push notifications

    DatabaseHelper db;

    ConversationFragment newConversationFragment = new ConversationFragment();
    ContactFragment newContactFragment = new ContactFragment();
    SettingsFragment newSettingsFragment = new SettingsFragment();
    Settings appSettings;

    int newestMessageNumber = 0;

    NotificationCompat.Builder notification;
    private static int uniqueID = 123456;
    //public Settings mySettings;


    public void setFragmentNoBackStack(Fragment frag){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content, frag);
        transaction.commit();
    }

    public void setFragment(Fragment frag) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

        //start Updater
        Intent updateIntent = new Intent(getBaseContext(), AutoUpdater.class);
        startService(updateIntent);

        //Gathering Credentials
        SharedPreferences sharedPref = getSharedPreferences("myPreferences", 0);
        HelperClass._Email = sharedPref.getString("email", "");
        HelperClass._Password = sharedPref.getString("password", "");
        HelperClass.savedCredentials = sharedPref.getBoolean("check", false);

        //Gets Credentials if the app doesn't have them
        if (!HelperClass.savedCredentials) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }

        setFragmentNoBackStack(newConversationFragment);
        //This is to set the title the first time the app is launched.
        getSupportActionBar().setTitle("Email Helper");
        db = new DatabaseHelper(getBaseContext());
        appSettings = new Settings();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_message,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.message_menu_settings){
            Intent startSettings = new Intent(this,SettingsActivity.class);
            startActivity(startSettings);
            return true;
        }
        /*else if(id == R.id.message_menu_contacts){
            Intent startSettings = new Intent(this,ContactActivity.class);
            startActivity(startSettings);
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }
}

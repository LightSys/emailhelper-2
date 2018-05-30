package org.lightsys.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.lightsys.emailhelper.Contact.ContactActivity;
import org.lightsys.emailhelper.Contact.ContactFragment;
import org.lightsys.emailhelper.Contact.NewContactActivity;
import org.lightsys.emailhelper.Conversation.ConversationFragment;


public class MainActivity extends AppCompatActivity {

    // TODO: Remove or figure out how to make the DividerItemDecoration work
    // TODO: Add multiple mail services
    // TODO: Polling or push notifications

    DatabaseHelper db;

    ConversationFragment newConversationFragment = new ConversationFragment();
    ContactFragment newContactFragment = new ContactFragment();

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
        getSupportActionBar().setTitle("Email Helper");

        //start Updater
        Intent updateIntent = new Intent(getBaseContext(), AutoUpdater.class);
        startService(updateIntent);

        //Gathering Credentials
        SharedPreferences sharedPref = getSharedPreferences("myPreferences", 0);
//        HelperClass._Email = sharedPref.getString("email", "");
//        HelperClass._Password = sharedPref.getString("password", "");
//        HelperClass.savedCredentials = sharedPref.getBoolean("check", false);
        HelperClass._Email = password.User;
        HelperClass._Password = password.auth;
        HelperClass.savedCredentials = true;

        //Gets Credentials if the app doesn't have them
        if (!HelperClass.savedCredentials) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }

        setFragmentNoBackStack(newConversationFragment);
        //This is to set the title the first time the app is launched.

        db = new DatabaseHelper(getBaseContext());

    }
    @Override
    public void onStart() {
        super.onStart();
        newConversationFragment.prepareConversationData();
    }
    @Override
    public void onResume() {
        super.onResume();
        newConversationFragment.prepareConversationData();

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
        else if(id == R.id.message_menu_contacts){
            Intent startContacts = new Intent(this,ContactActivity.class);
            startActivity(startContacts);
            return true;
        }
        else if(id == R.id.message_menu_add_contact){
            Intent startNewContact = new Intent(this, NewContactActivity.class);
            startActivity(startNewContact);
            return true;
        }
        else if(id == R.id.action_new_contact){
            Intent startNewContact = new Intent(this, NewContactActivity.class);
            startActivity(startNewContact);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

}

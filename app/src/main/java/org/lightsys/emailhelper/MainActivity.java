package org.lightsys.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.Contact.ContactActivity;
import org.lightsys.emailhelper.Contact.NewContactActivity;
import org.lightsys.emailhelper.Conversation.ConversationFragment;

public class MainActivity extends AppCompatActivity{

    // TODO: Remove or figure out how to make the DividerItemDecoration work
    // TODO: Add multiple mail services
    // TODO: Polling or push notifications

    DatabaseHelper db;

    ConversationFragment newConversationFragment = new ConversationFragment();

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
        getSupportActionBar().setTitle(getString(R.string.app_name));


        //start Updater
        Intent updateIntent = new Intent(getBaseContext(), AutoUpdater.class);
        startService(updateIntent);

        //init materials
        setFragmentNoBackStack(newConversationFragment);
        db = new DatabaseHelper(getBaseContext());

        //Gathering Credentials
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences), 0);
//        HelperClass._Email = sharedPref.getString(getString(R.string.key_email), getString(R.string.default_email));
//        HelperClass._Password = sharedPref.getString(getString(R.string.key_password), getString(R.string.default_password));
//        HelperClass.savedCredentials = sharedPref.getBoolean(getString(R.string.key_valid_credentials), getResources().getBoolean(R.bool.default_valid_credentials));
        //TODO replace hard code
        HelperClass._Email = password.User;
        HelperClass._Password = password.auth;
        HelperClass.savedCredentials = true;
        Contact send1 = password.sender1;
        db.insertContactData(send1.getEmail(),send1.getFirstName(),send1.getLastName());
        db.insertConversationData(send1.getEmail(),send1.getFirstName()+" "+send1.getLastName(),CommonMethods.getCurrentTime(),CommonMethods.getCurrentDate());
        Contact send2 = password.sender2;
        db.insertContactData(send2.getEmail(),send2.getFirstName(),send2.getLastName());
        db.insertConversationData(send2.getEmail(),send2.getFirstName()+" "+send2.getLastName(),CommonMethods.getCurrentTime(),CommonMethods.getCurrentDate());
        //Hard code ^^^^^

        //Gets Credentials if the app doesn't have them
        if (!HelperClass.savedCredentials) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }


    }
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onResume() {
        super.onResume();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_message,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.message_menu_settings:
                Intent startSettings = new Intent(this, SettingsActivity.class);
                startActivity(startSettings);
                return true;
            case R.id.message_menu_contacts:
                Intent startContacts = new Intent(this, ContactActivity.class);
                startActivity(startContacts);
                return true;
            case R.id.message_menu_add_contact:
            case R.id.action_new_contact:
                Intent startNewContact = new Intent(this, NewContactActivity.class);
                startActivity(startNewContact);
                return true;
            case R.id.message_menu_credentials:
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

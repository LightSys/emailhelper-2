package org.lightsys.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.Contact.ContactActivity;
import org.lightsys.emailhelper.Contact.NewContactActivity;
import org.lightsys.emailhelper.Conversation.ConversationFragment;
import org.lightsys.emailhelper.qr.QRActivity;
import org.lightsys.emailhelper.qr.launchQRScanner;

public class MainActivity extends AppCompatActivity{

    // TODO: Remove or figure out how to make the DividerItemDecoration work
    // TODO: Polling or push notifications

    static private final int QR_RESULT = 1;

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
//        HelperClass.setEmail(sharedPref.getString(getString(R.string.key_email), getString(R.string.default_email)));
//        HelperClass.Password = sharedPref.getString(getString(R.string.key_password), getString(R.string.default_password));
//        HelperClass.savedCredentials = sharedPref.getBoolean(getString(R.string.key_valid_credentials), getResources().getBoolean(R.bool.default_valid_credentials));
        //TODO replace hard code

        HelperClass.setEmail(password.User);
        HelperClass.Password = password.auth;
        HelperClass.savedCredentials = true;
        SharedPreferences.Editor myEdit = sharedPref.edit();
        myEdit.putString(getString(R.string.key_email),password.User);
        myEdit.apply();

        Contact send1 = password.sender2;
        db.insertContactData(send1.getEmail(),send1.getFirstName(),send1.getLastName());
        db.insertConversationData(send1.getEmail(),send1.getFirstName()+" "+send1.getLastName(),CommonMethods.getCurrentTime(),CommonMethods.getCurrentDate());

        Contact send2 = password.sender1;
        db.insertContactData(send2.getEmail(),send2.getFirstName(),send2.getLastName());
        db.insertConversationData(send2.getEmail(),send2.getFirstName()+" "+send2.getLastName(),CommonMethods.getCurrentTime(),CommonMethods.getCurrentDate());
        //Hard code ^^^^^
    }
    @Override
    public void onStart(){
        super.onStart();
        if (!HelperClass.savedCredentials) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        //Gets Credentials if the app doesn't have them

        newConversationFragment.prepareConversationData();
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
            case R.id.action_new_contact:
                Intent startNewContact = new Intent(this, NewContactActivity.class);
                startActivity(startNewContact);
                return true;
            case R.id.message_menu_credentials:
                Intent startCredentials = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(startCredentials);
                return true;
            case R.id.message_menu_QR_output:
                Intent QR = new Intent(getBaseContext(), QRActivity.class);
                startActivity(QR);
                return true;
            case R.id.message_menu_QR_input:
                gatherData(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //launches QR scanner
    public void gatherData(boolean launchScanner){
        //imported from LightSys Event App
        //modified for use in EmailHelper
        if (launchScanner) {
            if (ActivityCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
                gatherData(true);
            } else {
                Intent QR = new Intent(MainActivity.this, launchQRScanner.class);
                startActivityForResult(QR, QR_RESULT);
            }
        }
    }
    private void requestCameraPermission() {
        //imported from LightSys Event App
        Log.w("Barcode-reader", "Camera permission is not granted. Requesting permission");
        final String[] permissions = new String[]{"android.permission.CAMERA"};
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.CAMERA")) {
            ActivityCompat.requestPermissions(this, permissions, 2);
        } else {
            new View.OnClickListener() {
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, 2);
                }
            };
        }
    }
    //This may be good for something later like a start new conversation button
    /*
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    });
    */
}

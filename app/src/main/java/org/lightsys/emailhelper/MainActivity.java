package org.lightsys.emailhelper;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.lightsys.emailhelper.Contact.ContactActivity;
import org.lightsys.emailhelper.Contact.NewContactActivity;
import org.lightsys.emailhelper.Conversation.ConversationFragment;
import org.lightsys.emailhelper.qr.QRActivity;
import org.lightsys.emailhelper.qr.launchQRScanner;

import xdroid.toaster.Toaster;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{



    DatabaseHelper db;
    ConversationFragment newConversationFragment = new ConversationFragment();
    BroadcastReceiver reciever;
    FloatingActionButton fab;

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
        getSupportActionBar().setTitle(getString(R.string.messages));

        reciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(intent.getStringExtra(getString(R.string.broadcast_msg))){
                    case "update_UI":
                        newConversationFragment.prepareConversationData();
                        break;
                }
            }
        };

        fab = findViewById(R.id.mainFloatingButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newContact= new Intent(getBaseContext(), NewContactActivity.class);
                newContact.putExtra(getString(R.string.intent_create_new_contact),true);
                startActivity(newContact);
            }
        });

        //restart Updater
        Intent updateIntent = new Intent(getBaseContext(), AutoUpdater.class);
        stopService(updateIntent);
        startService(updateIntent);

        //init materials
        setFragmentNoBackStack(newConversationFragment);
        db = new DatabaseHelper(getBaseContext());

        //Gathering Credentials
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences), CommonMethods.SHARED_PREFERENCES_DEFAULT_MODE);

        boolean testingWithSignIn = false;
        //TODO remove hard code
        if(testingWithSignIn){
            AuthenticationClass.setEmail(sharedPref.getString(getString(R.string.key_email), getString(R.string.default_email)));
            AuthenticationClass.Password = sharedPref.getString(getString(R.string.key_password), getString(R.string.default_password));
            AuthenticationClass.incoming = sharedPref.getString(getString(R.string.key_imap),"");
            AuthenticationClass.outgoing = sharedPref.getString(getString(R.string.key_smtp),"");
            AuthenticationClass.savedCredentials = sharedPref.getBoolean(getString(R.string.key_valid_credentials), getResources().getBoolean(R.bool.default_valid_credentials));
        }
        else{
            AuthenticationClass.setEmail(password.User);
            AuthenticationClass.Password = password.auth;
            AuthenticationClass.savedCredentials = true;
            SharedPreferences.Editor myEdit = sharedPref.edit();
            myEdit.putString(getString(R.string.key_email),password.User);
            myEdit.apply();
            db.insertContact(password.donaldrshade);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if (!AuthenticationClass.savedCredentials) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        //Gets Credentials if the app doesn't have them
        newConversationFragment.prepareConversationData();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.new_message));
        registerReceiver(reciever,filter);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reciever);
        super.onStop();
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
            case R.id.message_menu_user_settings:
                Intent userSettings = new Intent(getBaseContext(), UserSettingsActivity.class);
                startActivity(userSettings);
                return true;
            case R.id.message_menu_QR_output:
                Intent QR = new Intent(getBaseContext(), QRActivity.class);
                startActivity(QR);
                return true;
            case R.id.message_menu_QR_input:
                gatherData(true);
                return true;
            case R.id.message_menu_about:
                Intent about = new Intent(getBaseContext(), About.class);
                startActivity(about);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //launches QR scanner
    /*
    These next two functions were pulled from EventApp on 7/11/18
    They were originally written by Otter57 and edited by Littlesnowman88.
    They were changed for use in EmailHelper
     */
    public void gatherData(boolean launchScanner){
        if (launchScanner) {
            if (ActivityCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            } else {
                Intent QR = new Intent(MainActivity.this, launchQRScanner.class);
                startActivityForResult(QR, CommonMethods.QR_RESULT);
            }
        }
    }

    //if app does not have camera permission, ask user for permission
    private void requestCameraPermission() {
        Log.w("Barcode-reader", "Camera permission is not granted. Requesting permission");
        final String[] permissions = new String[]{"android.permission.CAMERA"};
        ActivityCompat.requestPermissions(this, permissions, CommonMethods.CAMERA_REQUEST_CODE);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults){
        switch(requestCode){
            case CommonMethods.CAMERA_REQUEST_CODE:
                for(int i = 0;i<permissions.length;i++){
                    if(permissions[i].equalsIgnoreCase("android.permission.CAMERA")){
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                            Intent QR = new Intent(MainActivity.this, launchQRScanner.class);
                            startActivityForResult(QR, CommonMethods.QR_RESULT);
                        }else if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                            if(!ActivityCompat.shouldShowRequestPermissionRationale(this,"android.permission.CAMERA")){//If statement added by DSHADE
                                Toaster.toastLong(R.string.cannot_request_camera_permission);
                            }else{
                                Toaster.toastLong(R.string.need_camera_permission);
                            }
                        }
                        break;
                    }
                }
        }
    }
}

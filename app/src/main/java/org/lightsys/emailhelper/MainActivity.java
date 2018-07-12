package org.lightsys.emailhelper;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.lightsys.emailhelper.Contact.ContactActivity;
import org.lightsys.emailhelper.Contact.NewContactActivity;
import org.lightsys.emailhelper.Conversation.ConversationFragment;
import org.lightsys.emailhelper.qr.QRActivity;
import org.lightsys.emailhelper.qr.launchQRScanner;

import xdroid.toaster.Toaster;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

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
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences), CommonMethods.SHARED_PREFERENCES_DEFAULT_MODE);
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
                startActivityForResult(QR, QR_RESULT);
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
                            startActivityForResult(QR, QR_RESULT);
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

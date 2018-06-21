package org.lightsys.emailhelper.Contact;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.R;

import java.net.URL;

import xdroid.toaster.Toaster;

public class ContactActivity extends AppCompatActivity {
    Button contactButton;
    ContactFragment contactFragment = new ContactFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setUpButton();
        setUpFragment();
    }

    private void setUpFragment() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.contact_fragment, contactFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private void setUpButton(){
        contactButton  = findViewById(R.id.inbox_button);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showInboxContacts = new Intent(getApplicationContext(),InboxContactActivity.class);
                startActivity(showInboxContacts);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contact,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_new_contact){
            Intent startNewContact = new Intent(this, NewContactActivity.class);
            startActivity(startNewContact);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume(){
        super.onResume();
        contactFragment.prepareContactData();
    }
}

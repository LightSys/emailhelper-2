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
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.R;

import java.net.URL;

import xdroid.toaster.Toaster;

public class ContactActivity extends AppCompatActivity {
    CheckBox contactCheckBox;
    ContactFragment contactFragment = new ContactFragment();
    InboxContactFragment inboxContactFragment = new InboxContactFragment();
    ContactList contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getContacts starter = new getContacts(getApplicationContext());
        starter.execute();
        contactCheckBox  = findViewById(R.id.inbox_checkbox);
        contactCheckBox.setChecked(false);
        setFragment(contactFragment);
        contactCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(contactCheckBox.isChecked()){
                    if(contactList != null){
                        inboxContactFragment.setContactList(contactList);
                        setFragment(inboxContactFragment);
                    }else{
                        Toaster.toast("Contacts have not been aquired. Please try again in a few momements.");
                        contactCheckBox.setChecked(false);
                    }

                }else{
                    setFragment(contactFragment);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        contactFragment.prepareContactData();
        setFragment(contactFragment);
        contactCheckBox.setChecked(false);
        if(inboxContactFragment.getContactListStatus()){
            contactList = null;
            getContacts starter = new getContacts(getApplicationContext());
            starter.execute();
        }
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
    public void setFragment(Fragment frag) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.contact_fragment, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private class getContacts extends AsyncTask<URL, Integer, Long> {
        ContactList temp;
        Context context;
        public getContacts(Context context){
            this.context = context;
        }
        @Override
        protected Long doInBackground(URL... urls) {
            GetMail mailer = new GetMail(context);
            temp = mailer.getContactsFromInbox();
            return null;
        }
        @Override
        protected void onPostExecute(Long l){
            contactList = temp;
            Toaster.toast("Contacts found");
        }
    }
}

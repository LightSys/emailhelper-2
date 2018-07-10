package org.lightsys.emailhelper.Contact;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.RecyclerTouchListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import xdroid.toaster.Toaster;

public class ContactActivity extends AppCompatActivity {
    CheckBox contactsCheckBox;
    private ContactList contactList = new ContactList();
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    DatabaseHelper db;
    Context ActivityContext = this;
    SwipeRefreshLayout swipeContainer;
    //simpleItemTouchCallback is used to delete Contacts via swiping
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setUpCheckBox();
        setUpContainer();
        db = new DatabaseHelper(getApplicationContext());
    }

    private void setUpCheckBox(){
        contactsCheckBox  = findViewById(R.id.inbox_check_box);
        contactsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    swipeContainer.setRefreshing(true);
                    new refresh().execute();
                }else{
                    prepareContactData();
                }
            }
        });
    }
    private void setUpContainer(){
        swipeContainer = findViewById(R.id.contact_refresh_layout);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(contactsCheckBox.isChecked()){
                    new refresh().execute();
                }else{
                    swipeContainer.setRefreshing(false);
                }
            }
        });
        simpleItemTouchCallback  = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {
            Contact contact;
            /******************************************************************************************
             *  The onMove function has to be there for the ItemTouchHelper to be happy.              *
             *  There shouldn't be anything that we need to use it for.                               *
             *  -Nick                                                                                 *
             ******************************************************************************************/
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if(!contactsCheckBox.isChecked()){
                    int deleteRow = viewHolder.getAdapterPosition();
                    contact = contactList.get(deleteRow);
                    String deletionMessage = getString(R.string.contact_delete_message_prestring)+contactList.get(deleteRow).getName()+getString(R.string.contact_delete_message_poststring);
                    new ConfirmDialog(deletionMessage,getString(R.string.delete_word),ActivityContext,deletionRunnable,cancelRunnable);
                }
                else{
                    resetScreen();
                }
            }
            Runnable cancelRunnable = new Runnable() {
                @Override
                public void run() {
                    resetScreen();
                }
            };
            Runnable deletionRunnable = new Runnable() {
                @Override
                public void run() {
                    db.deleteConversationData(contact.getEmail());
                    db.deleteContactData(contact.getEmail());
                    if(contactsCheckBox.isChecked()){
                        prepareContactDataWithInbox();
                    }
                    else{
                        prepareContactData();
                    }
                }
            };
        };
        makeRecyclerView();
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
        prepareContactData();
        contactsCheckBox.setChecked(false);
    }


    /**
     * Has all the steps needed to make the RecyclerView that holds the contacts.
     */
    public void makeRecyclerView() {
        recyclerView = findViewById(R.id.recycler_contact_view);//Makes the RecyclerView
        RecyclerView.LayoutManager cLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {//To edit the contact settings
                if(contactsCheckBox.isChecked()){
                    return;
                }
                Contact contact = contactList.get(position);
                Toast.makeText(getApplicationContext(), contact.getEmail() + getString(R.string.is_selected), Toast.LENGTH_SHORT).show();
                Intent editContactDetails = new Intent(getApplicationContext(),ContactSettingsActivity.class);
                editContactDetails.putExtra(getString(R.string.intent_email),contact.getEmail());
                editContactDetails.putExtra(getString(R.string.intent_first_name),contact.getFirstName());
                editContactDetails.putExtra(getString(R.string.intent_last_name),contact.getLastName());
                startActivity(editContactDetails);
            }
            @Override
            public void onLongClick(View view, int position) {//To edit the contact
                if(contactsCheckBox.isChecked()){
                    return;
                }
                Contact contact = contactList.get(position);
                Toast.makeText(getApplicationContext(), contact.getEmail() + getString(R.string.is_selected), Toast.LENGTH_SHORT).show();
                Intent editContact = new Intent(getApplicationContext(),EditContactActivity.class);
                editContact.putExtra(getString(R.string.intent_email),contact.getEmail());
                editContact.putExtra(getString(R.string.intent_first_name),contact.getFirstName());
                editContact.putExtra(getString(R.string.intent_last_name),contact.getLastName());
                startActivity(editContact);
            }
        }));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * This function clears contactList and gets new data from the database.
     * Should be used just before the Data appears on the screen.
     */
    public void prepareContactData() {
        contactList = db.getContactList();
        contactAdapter = new ContactAdapter(contactList,false);
        recyclerView.setAdapter(contactAdapter);
    }
    public void resetScreen(){
        recyclerView.setAdapter(contactAdapter);
    }
    /**
     * This function clears adds contacts from the inbox to the existing list.
     */
    public void prepareContactDataWithInbox() {
        contactAdapter = new ContactAdapter(contactList,true);
        recyclerView.setAdapter(contactAdapter);
    }
    class refresh extends AsyncTask<URL, Integer, Long> {
        @Override
        protected Long doInBackground(URL... urls) {
            GetMail mailer = new GetMail(getApplicationContext());
            contactList.add(mailer.getContactsFromInbox());
            return null;
        }
        @Override
        protected void onPostExecute(Long result){
            swipeContainer.setRefreshing(false);//Must be called or refresh circle will continue forever
            prepareContactDataWithInbox();
        }
    }

}

package org.lightsys.emailhelper.Contact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferObserver;

import org.lightsys.emailhelper.AutoUpdater;
import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.RecyclerTouchListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import xdroid.toaster.Toaster;

public class ContactActivity extends AppCompatActivity{
    CheckBox contactsCheckBox;

    private RecyclerView recyclerView;
    DatabaseHelper db;
    Context ActivityContext = this;
    SwipeRefreshLayout swipeContainer;
    //simpleItemTouchCallback is used to delete Contacts via swiping
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback;
    private boolean waitingForList;
    //Contact Lists
    private ContactList activeList;
    private ContactList databaseContacts;
    private ContactList inboxContacts;

    List runnablesToRunOnPostExecute = new ArrayList<>();

    private final int not_gathered = 0;
    private final int gathering = 1;
    private final int finished = 2;

    int gatheringData = not_gathered;

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
        prepareLists();
        setActiveList(databaseContacts);
    }

    private void prepareLists() {
        databaseContacts = db.getContactList();
        inboxContacts = new ContactList(databaseContacts);
        new refresh().execute();
        gatheringData = gathering;
    }

    //<editor-fold> Basic Functions
    private void setUpCheckBox(){
        contactsCheckBox  = findViewById(R.id.inbox_check_box);
        contactsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    setActiveList(inboxContacts);
                }else{
                    setActiveList(databaseContacts);
                    waitingForList =false;
                    swipeContainer.setRefreshing(false);
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
                    swipeContainer.setRefreshing(false);
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
                SharedPreferences sp = getSharedPreferences(getString(R.string.preferences), CommonMethods.SHARED_PREFERENCES_DEFAULT_MODE);
                Resources r = getApplicationContext().getResources();
                if(!sp.getBoolean(getString(R.string.key_swipe_deletion),getApplicationContext().getResources().getBoolean(R.bool.default_enable_swipe_deletion))){
                    Toaster.toast(R.string.swipe_deletion_disabled);
                    prepareContactData();
                    return;
                }
                if(!contactsCheckBox.isChecked()){
                    int deleteRow = viewHolder.getAdapterPosition();
                    contact = activeList.get(deleteRow);
                    String deletionMessage = getString(R.string.contact_delete_message_prestring)+activeList.get(deleteRow).getName()+getString(R.string.contact_delete_message_poststring);
                    new ConfirmDialog(deletionMessage,getString(R.string.delete_word),ActivityContext,deletionRunnable,cancelRunnable);
                }
                else{
                   prepareContactData();
                }
            }
            Runnable cancelRunnable = new Runnable() {
                @Override
                public void run() {
                    prepareContactData();
                }
            };
            Runnable deletionRunnable = new Runnable() {
                @Override
                public void run() {
                    deleteContact(contact);
                    prepareContactData();
                }
            };
        };
        makeRecyclerView();
    }
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
                Contact contact = activeList.get(position);
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
                Contact contact = activeList.get(position);
                Toast.makeText(getApplicationContext(), contact.getEmail() + getString(R.string.is_selected), Toast.LENGTH_SHORT).show();
                Intent editContact = new Intent(getApplicationContext(),EditContactActivity.class);
                editContact.putExtra(getString(R.string.intent_email),contact.getEmail());
                editContact.putExtra(getString(R.string.intent_first_name),contact.getFirstName());
                editContact.putExtra(getString(R.string.intent_last_name),contact.getLastName());
                startActivityForResult(editContact,CommonMethods.DOES_CONTACT_CHANGE);
            }
        }));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
            startActivityForResult(startNewContact,CommonMethods.CHECK_FOR_CONTACT_ADDITION);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //</editor-fold>
    @Override
    protected void onResume(){
        super.onResume();
        prepareContactData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CommonMethods.CHECK_FOR_CONTACT_ADDITION:
                if(resultCode == CommonMethods.NEW_CONTACT_ADDED){
                    addContact(Contact.getContactFromIntent(data));
                    Intent updaterIntent = new Intent(getBaseContext(), AutoUpdater.class);
                    stopService(updaterIntent);
                    startService(updaterIntent);
                }
                break;
            case CommonMethods.DOES_CONTACT_CHANGE:
                if(resultCode == CommonMethods.CONTACT_CHANGED){
                    updateContact(data);
                    break;
                }
                break;
        }
    }

    private void deleteContact(final Contact contact){
        Runnable resetInDatabase = new Runnable() {
            @Override
            public void run() {
                inboxContacts.resetInDatabase(contact);
            }
        };
        db.deleteContactData(contact.getEmail());
        databaseContacts.delete(contact);
        if(gatheringData == gathering){
            runnablesToRunOnPostExecute.add(resetInDatabase);
        }else{
            resetInDatabase.run();
        }
        prepareContactData();
    }
    private void deleteContact(final String email){
        final Contact contact = new Contact();
        contact.setEmail(email);
        Runnable resetInDatabase = new Runnable() {
            @Override
            public void run() {
                inboxContacts.resetInDatabase(contact);
            }
        };
        db.deleteContactData(email);
        databaseContacts.delete(contact);
        if(gatheringData == gathering){
            runnablesToRunOnPostExecute.add(resetInDatabase);
        }else{
            resetInDatabase.run();
        }
        prepareContactData();
    }
    private void updateContact(Intent data){
        final Contact contact = Contact.getContactFromIntent(data);
        Runnable resetInDatabase = new Runnable() {
            @Override
            public void run() {
                inboxContacts.resetInDatabase(contact);
            }
        };
        databaseContacts.delete(contact);
        if(gatheringData == gathering){
            runnablesToRunOnPostExecute.add(resetInDatabase);
        }else{
            resetInDatabase.run();
        }
        Runnable addContact = new Runnable() {
            @Override
            public void run() {
                inboxContacts.add(contact);
            }
        };
        db.deleteContactData(contact.getEmail());
        databaseContacts.add(contact);
        if(gatheringData == gathering){
            runnablesToRunOnPostExecute.add(addContact);
        }else{
            addContact.run();
        }
        Contact oldContact = db.getContact(data.getStringExtra(getString(R.string.intent_original_email)));
        contact.setCreatedDate(oldContact.getCreatedDate());
        contact.setUpdatedDate(oldContact.getUpdatedDate());
        contact.setSendNotifications(oldContact.getSendNotifications());
        db.updateContact(data.getStringExtra(getString(R.string.intent_original_email)),contact);
        prepareContactData();
    }
    private void addContact(final Contact contact){
        Runnable addInboxListContact = new Runnable() {
            @Override
            public void run() {
                inboxContacts.add(contact);
            }
        };
        contact.setSendNotifications(true);
        databaseContacts.add(contact);
        if(gatheringData == gathering){
            runnablesToRunOnPostExecute.add(addInboxListContact);
        }else{
            addInboxListContact.run();
        }
    }

    /**
     * This function clears contactList and gets new data from the database.
     * Should be used just before the Data appears on the screen.
     */
    public void setActiveList(ContactList activeList){
        if(gatheringData==gathering && isInboxActiveList()){
            swipeContainer.setRefreshing(true);
            waitingForList = true;
            return;
        }
        this.activeList = activeList;
        ContactAdapter contactAdapter = new ContactAdapter(this.activeList, contactsCheckBox.isChecked(),this);
        recyclerView.setAdapter(contactAdapter);
    }

    private boolean isInboxActiveList() {
        return contactsCheckBox.isChecked();
    }

    /**
     * This functions goal is to refresh the lists.
     * It figures out who is the active list so it can be set to the new data.
     */
    public void prepareContactData() {
        ContactAdapter ca = new ContactAdapter(activeList,isInboxActiveList(),this);
        recyclerView.setAdapter(ca);
    }

    /**
     * This class allows the the mailer work in a Async class
     * The goal is to add the extra contacts onto the list and prep the list to display
     */
    class refresh extends AsyncTask<URL, Integer, Long> {
        @Override
        protected Long doInBackground(URL... urls) {
            Log.d("ContactActivity","Starting inbox search.");
            GetMail mailer = new GetMail(getApplicationContext());
            inboxContacts.add(mailer.getContactsFromInbox());
            return null;
        }
        @Override
        protected void onPostExecute(Long result){
            swipeContainer.setRefreshing(false);//Must be called or refresh circle will continue forever
            for(int i = 0;i<runnablesToRunOnPostExecute.size();i++){
                ((Runnable)runnablesToRunOnPostExecute.get(i)).run();
            }
            gatheringData = finished;
            if(waitingForList){
                setActiveList(inboxContacts);
                waitingForList = false;
                //Why this section?
                //This section will automatically display the list once it is done gathering data.
            }
            Log.d("ContactActivity","Ending inbox search");
        }
    }


    /**
     * This adapter is the go between for the contact activity and the contacts in the database.
     * Created by nicholasweg on 6/30/17.
     */
    public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactItem> {

        private ContactList contactList;
        private boolean showCheckbox;
        private ContactActivity callingActivity;

        ContactAdapter(ContactList contactList, boolean showCheckbox, ContactActivity calling) {
            this.contactList = contactList;
            this.showCheckbox = showCheckbox;
            callingActivity = calling;
        }

        @NonNull
        @Override
        public ContactItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list, parent, false);
            return new ContactItem(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactItem holder, int position){
            holder.bind(contactList.get(position),showCheckbox);
        }

        @Override
        public int getItemCount() {return contactList.size();}

        class ContactItem extends RecyclerView.ViewHolder{
            private TextView name, email;
            private CheckBox inDatabase;
            Context context;
            DatabaseHelper db;
            Resources r;

            ContactItem(View view) {
                super(view);
                name = view.findViewById(R.id.name);
                email = view.findViewById(R.id.email);
                //set Class variables
                context = view.getContext();
                db = new DatabaseHelper(context);
                r = context.getResources();
                setUpCheckBox(view);
            }

            /**
             * This function helps to bind the information to the view.
             */
            public void bind(Contact contact,boolean showCheckBox){
                name.setText(contact.getName());
                email.setText(contact.getEmail());
                inDatabase.setChecked(contact.getInContacts());
                if(showCheckBox){
                    inDatabase.setVisibility(View.VISIBLE);
                }else{
                    inDatabase.setVisibility(View.INVISIBLE);
                }
            }

            /**
             * This function sets up the checkbox.
             */
            private void setUpCheckBox(View view) {
                inDatabase = view.findViewById(R.id.contact_checkbox);
                inDatabase.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        final String mail = email.getText().toString();
                        final String fullName = name.getText().toString();
                        if(inDatabase.isChecked()){
                            if(!db.containsContact(mail)) {
                                inDatabase.setChecked(true);
                                Intent addContactDetails = new Intent(context, NewContactActivity.class);
                                addContactDetails.putExtra(r.getString(R.string.intent_email), mail);
                                String first,last;
                                if(fullName.contains(" ")){
                                    first = fullName.substring(0,fullName.indexOf(" "));
                                    last = fullName.substring(fullName.indexOf(" ")+1);
                                }
                                else{
                                    first = fullName;
                                    last = "";
                                }
                                if(fullName.contains("@")){//This indicates its probably not a name but an email.
                                    first = "";
                                    last = "";
                                }

                                addContactDetails.putExtra(r.getString(R.string.intent_first_name), first);
                                addContactDetails.putExtra(r.getString(R.string.intent_last_name), last);
                                callingActivity.startActivityForResult(addContactDetails,CommonMethods.CHECK_FOR_CONTACT_ADDITION);
                            }
                        }
                        else{
                            Runnable cancel = new Runnable() {
                                @Override
                                public void run() {
                                    callingActivity.prepareContactData();
                                }
                            };
                            final Runnable delete = new Runnable() {
                                @Override
                                public void run() {
                                    callingActivity.deleteContact(email.getText().toString());
                                }
                            };
                            String message = r.getString(R.string.contact_delete_message_prestring)+" "+mail+" "+r.getString(R.string.contact_delete_message_poststring);
                            new ConfirmDialog(message,r.getString(R.string.delete_word),context,delete,cancel);
                        }
                    }

                });
            }
        }
    }
}

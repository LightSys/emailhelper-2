package org.lightsys.emailhelper.Conversation;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.Contact.ContactSettingsActivity;
import org.lightsys.emailhelper.Contact.EditContactActivity;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

/**************************************************************************************************
 *  This is the activity where everything happens in the message window, outside of a             *
 *  BottomNavigationActivity.                                                                     *
 **************************************************************************************************/

public class ConversationActivity extends AppCompatActivity {


    ConversationWindowFragment chats = new ConversationWindowFragment();
    Intent intent;
    public String email;
    DatabaseHelper db;

    public void setFragmentNoBackStack(Fragment frag){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.message_window, frag);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        intent = getIntent();
        email = intent.getStringExtra(getString(R.string.intent_email));
        db = new DatabaseHelper(getApplicationContext());
        setTitle(db.getContactName(email));
        passToFragment();
        setFragmentNoBackStack(chats);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    /**********************************************************************************************
     *  The email that is passed was passed to this activity. This function passes it to the      *
     *  fragment that exists inside of the activity.                                              *
     **********************************************************************************************/

    public void passToFragment() {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.intent_email), email);
        chats.setArguments(bundle);
    }
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_conversation,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Contact contact = new DatabaseHelper(getApplicationContext()).getContact(email);
        switch(id){
            case R.id.action_edit_contact:
                Intent editContact = new Intent(getApplicationContext(),EditContactActivity.class);
                editContact.putExtra(getString(R.string.intent_email),contact.getEmail());
                editContact.putExtra(getString(R.string.intent_first_name),contact.getFirstName());
                editContact.putExtra(getString(R.string.intent_last_name),contact.getLastName());
                startActivity(editContact);
                return true;
            case R.id.action_edit_contact_settings:
                Intent editContactDetails = new Intent(getApplicationContext(),ContactSettingsActivity.class);
                editContactDetails.putExtra(getString(R.string.intent_email),contact.getEmail());
                editContactDetails.putExtra(getString(R.string.intent_first_name),contact.getFirstName());
                editContactDetails.putExtra(getString(R.string.intent_last_name),contact.getLastName());
                startActivityForResult(editContactDetails,CommonMethods.CHECK_FOR_DELETION);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        chats.prepareWindowRows();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == CommonMethods.CONVERSATION_DELETED && requestCode==CommonMethods.CHECK_FOR_DELETION){
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

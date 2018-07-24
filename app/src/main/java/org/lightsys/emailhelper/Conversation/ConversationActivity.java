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

/**
 * This activity is where the messages are displayed. It takes in an email from the intent and searches
 * the database for all relevant messages.
 */

public class ConversationActivity extends AppCompatActivity {
    //Class variables
    ConversationWindowFragment chats = new ConversationWindowFragment();
    Intent intent;
    public String email;
    DatabaseHelper db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        db = new DatabaseHelper(getApplicationContext());//connect to database

        intent = getIntent();
        email = intent.getStringExtra(getString(R.string.intent_email));//get the email from the database
        setTitle(db.getContactName(email));//sets the email in the Titlebar

        passToFragment();//Screen set up. See below for more details.
        setFragmentNoBackStack(chats);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chats.prepareWindowRows();
        //This function is called as a last step before the activity is shown.
        //This calls and gets the newest information from the database.
    }

    /**
     * This function places the fragment onto the screen.
     * @param frag the fragment to be displayed
     */
    public void setFragmentNoBackStack(Fragment frag){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.message_window, frag);
        transaction.commit();
    }

    /**
     * The email that is passed was passed to this activity. This function passes it to the
     * fragment that exists inside of the activity.
     */
    private void passToFragment() {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.intent_email), email);
        chats.setArguments(bundle);
    }

    /**
     * Sets Up the menu with the action buttons
     */
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_conversation,menu);
        return true;
    }

    /**
     * This function handles when a button is pressed. If you add a button then you need to add a case
     * statement here to handle when that button is pressed.
     */
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



    /**
     * This function is here to check if when one of the action buttons returns the conversation has
     * been deleted which is why finished is called in this case.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==CommonMethods.CHECK_FOR_DELETION && resultCode == CommonMethods.CONVERSATION_DELETED){
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

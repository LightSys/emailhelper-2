package org.lightsys.emailhelper.Contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.lightsys.emailhelper.AttachmentActivity;
import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.Conversation.ConversationActivity;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

import java.util.Calendar;

public class ContactSettingsActivity extends AppCompatActivity {
    String firstName;
    String lastName;
    String fullName;
    String email;
    Context ActivityContext;
    DatabaseHelper db;
    Button editContact,deleteConversation,startConversation,showAttachments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_settings);
        db = new DatabaseHelper(getApplicationContext());
        ActivityContext = this;
        //Get info from intent
        Intent intent = getIntent();
        firstName = intent.getStringExtra(getString(R.string.intent_first_name));
        lastName = intent.getStringExtra(getString(R.string.intent_last_name));
        email = intent.getStringExtra(getString(R.string.intent_email));
        fullName = firstName + " " + lastName;
        //Set up textViews
        TextView name = findViewById(R.id.name_textView);
        name.setText(fullName);
        TextView emailText = findViewById(R.id.email_textView);
        emailText.setText(email);

        //setUpCheckBoc
        final CheckBox sendNotifications = findViewById(R.id.display_notifications);
        sendNotifications.setChecked(db.getNotificationSettings(email));
        sendNotifications.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                db.setNotificationSettings(email,sendNotifications.isChecked());
            }
        });
        //Creates the buttons
        setUpButtons();
        //Sets them appropriately
        setButtons();
    }

    //These constants are for the views to use to make the buttons appear/disappear
    LinearLayout.LayoutParams expanded = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    LinearLayout.LayoutParams shrunk = new LinearLayout.LayoutParams(0,0);

    /**
     * Sets the buttons to their correct state
     */
    private void setButtons() {
        if(db.hasConversationWith(email)){
            hasConvo();
        }
        else{
            noConvo();
        }
        if(!db.hasAttachments(email)){
            showAttachments.setVisibility(View.INVISIBLE);
            showAttachments.setClickable(false);
            showAttachments.setLayoutParams(shrunk);
        }
    }

    /**
     * Gets all buttons from layout
     */
    private void setUpButtons() {
        editContact = findViewById(R.id.edit_contact);
        editContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editContact = new Intent(getApplicationContext(),EditContactActivity.class);
                editContact.putExtra(getString(R.string.intent_first_name),firstName);
                editContact.putExtra(getString(R.string.intent_last_name),lastName);
                editContact.putExtra(getString(R.string.intent_email),email);
                startActivity(editContact);
            }
        });
        deleteConversation = findViewById(R.id.delete_converstation);
        deleteConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                Runnable deletionRunnable = new Runnable() {
                    @Override
                    public void run() {
                        db.deleteConversationData(email);
                        setResult(CommonMethods.CONVERSATION_DELETED);
                        finish();
                    }
                };
                String deletionMessage = getString(R.string.conversation_delete_message_prestring)+fullName+getString(R.string.conversation_delete_message_poststring);
                new ConfirmDialog(deletionMessage,getString(R.string.delete_word), ActivityContext,deletionRunnable,null);

            }
        });
        startConversation = findViewById(R.id.start_conversation);
        startConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                db.insertConversationData(email,fullName,false);
                finish();
            }
        });
        showAttachments = findViewById(R.id.show_attachments);
        showAttachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent attach = new Intent(getApplicationContext(),AttachmentActivity.class);
                attach.putExtra(getString(R.string.intent_email),email);
                startActivity(attach);
            }
        });
    }

    public void hasConvo(){
        startConversation.setVisibility(View.INVISIBLE);
        startConversation.setClickable(false);
        startConversation.setLayoutParams(shrunk);
        deleteConversation.setVisibility(View.VISIBLE);
        deleteConversation.setClickable(true);
        deleteConversation.setLayoutParams(expanded);

    }
    public void noConvo(){
        startConversation.setVisibility(View.VISIBLE);
        startConversation.setClickable(true);
        startConversation.setLayoutParams(expanded);
        deleteConversation.setVisibility(View.INVISIBLE);
        deleteConversation.setClickable(false);
        deleteConversation.setLayoutParams(shrunk);

    }
    @Override
    public void onResume() {
        setButtons();
        super.onResume();
    }
}

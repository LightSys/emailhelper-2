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
        ActivityContext = this;
        firstName = getIntent().getStringExtra(getString(R.string.intent_first_name));
        lastName = getIntent().getStringExtra(getString(R.string.intent_last_name));
        fullName = firstName + " " + lastName;
        email = getIntent().getStringExtra(getString(R.string.intent_email));
        TextView name = findViewById(R.id.name_textView);
        name.setText(fullName);
        TextView emailText = findViewById(R.id.email_textView);
        emailText.setText(email);
        db = new DatabaseHelper(getApplicationContext());


        final CheckBox sendNotifications = findViewById(R.id.display_notifications);
        sendNotifications.setChecked(db.getNotificationSettings(email));
        sendNotifications.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                db.setNotificationSettings(email,sendNotifications.isChecked());
            }
        });
        setUpButtons();
        setButtons();
    }
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
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
            showAttachments.setLayoutParams(size);
        }
    }

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
                db.insertConversationData(email,fullName, CommonMethods.getCurrentTime(),CommonMethods.getDate(Calendar.getInstance().getTime()));
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
        deleteConversation.setVisibility(View.VISIBLE);
        deleteConversation.setClickable(true);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
        startConversation.setLayoutParams(size);
    }
    public void noConvo(){
        startConversation.setVisibility(View.VISIBLE);
        startConversation.setClickable(true);
        deleteConversation.setVisibility(View.INVISIBLE);
        deleteConversation.setClickable(false);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        startConversation.setLayoutParams(size);
    }
    @Override
    public void onResume() {
        setButtons();
        super.onResume();
    }
}

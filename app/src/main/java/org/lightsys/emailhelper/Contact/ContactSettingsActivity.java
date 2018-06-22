package org.lightsys.emailhelper.Contact;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.lightsys.emailhelper.AttachmentActivity;
import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

public class ContactSettingsActivity extends AppCompatActivity {
    String firstName;
    String lastName;
    String fullName;
    String email;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_settings);
        ActionBar actionBar = this.getSupportActionBar();
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


        Button editContact = findViewById(R.id.edit_contact);
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
        Button deleteConversation = findViewById(R.id.delete_converstation);
        deleteConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                db.deleteConversationData(email);
                Intent upIntent = new Intent(getApplicationContext(),ContactActivity.class);
                navigateUpTo(upIntent);

            }
        });
        Button deleteAndStartNew = findViewById(R.id.delete_and_start_new_conversation);
        deleteAndStartNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                db.deleteConversationData(email);
                db.insertConversationData(email,fullName, CommonMethods.getCurrentTime(),CommonMethods.getCurrentDate());
                Intent upIntent = new Intent(getApplicationContext(),ContactActivity.class);
                navigateUpTo(upIntent);
            }
        });
        Button showAttachments = findViewById(R.id.show_attachments);
        showAttachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent attach = new Intent(getApplicationContext(),AttachmentActivity.class);
                attach.putExtra(getString(R.string.intent_email),email);
                startActivity(attach);
            }
        });
    }
}

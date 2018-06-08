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

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

public class ContactSettingsActivity extends AppCompatActivity {
    String firstName;
    String lastName;
    String email;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_settings);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        firstName = getIntent().getStringExtra("first_name");
        lastName = getIntent().getStringExtra("last_name");
        email = getIntent().getStringExtra("email");
        TextView name = findViewById(R.id.name_textView);
        name.setText(firstName +" "+lastName);
        TextView emailText = findViewById(R.id.email_textView);
        emailText.setText(email);
        db = new DatabaseHelper(getApplicationContext());


        final CheckBox sendNotifications = (CheckBox) findViewById(R.id.display_notifications);
        sendNotifications.setChecked(db.getNotificationSettings(email));
        sendNotifications.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                db.setNotificationSettings(email,sendNotifications.isChecked());
            }
        });


        Button editContact = (Button) findViewById(R.id.edit_contact);
        editContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editContact = new Intent(getApplicationContext(),editContactActivity.class);
                editContact.putExtra("first_name",firstName);
                editContact.putExtra("last_name",lastName);
                editContact.putExtra("email",email);
                startActivity(editContact);
            }
        });
        Button deleteConversation = (Button) findViewById(R.id.delete_converstation);
        deleteConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                db.deleteConversationData(email);
                Intent upIntent = new Intent(getApplicationContext(),ContactActivity.class);
                navigateUpTo(upIntent);

            }
        });
        Button deleteAndStartNew = (Button) findViewById(R.id.delete_and_start_new_conversation);
        deleteAndStartNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                db.deleteConversationData(email);
                db.insertConversationData(email,firstName+" "+lastName, CommonMethods.getCurrentTime(),CommonMethods.getCurrentDate());
                Intent upIntent = new Intent(getApplicationContext(),ContactActivity.class);
                navigateUpTo(upIntent);
            }
        });
    }


}
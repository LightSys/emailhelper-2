package org.lightsys.emailhelper.Contact;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.Contact.ContactActivity;
import org.lightsys.emailhelper.Contact.NewContactActivity;
import org.lightsys.emailhelper.Conversation.ConversationWindow;
import org.lightsys.emailhelper.Conversation.ConversationWindowFragment;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

public class EditContactActivity extends AppCompatActivity {
    String originalFirstName;
    String originalLastName;
    String originalEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        final TextView firstname = findViewById(R.id.editContactActivity_first_name_edit);
        originalFirstName = getIntent().getStringExtra(getString(R.string.intent_first_name));
        firstname.setText(originalFirstName);
        final TextView lastname = findViewById(R.id.editContactActivity_last_name_edit);
        originalLastName = getIntent().getStringExtra(getString(R.string.intent_last_name));
        lastname.setText(originalLastName);
        final TextView email = findViewById(R.id.editContactActivity_email_edit);
        originalEmail = getIntent().getStringExtra(getString(R.string.intent_email));
        email.setText(originalEmail);
        Button saveContactButton = (Button) findViewById(R.id.saveContactButton);
        saveContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contact editedContact = new Contact();
                editedContact.setFirstName((String)firstname.getText().toString());
                editedContact.setLastName((String)lastname.getText().toString());
                editedContact.setEmail((String)email.getText().toString());

                saveContact(editedContact);

                Intent upIntent = new Intent(getApplicationContext(),ContactActivity.class);
                navigateUpTo(upIntent);
            }
        });

    }
    public void saveContact(Contact editedContact){
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        db.deleteContactData(originalEmail);
        db.insertContactData(editedContact);
        if(!originalEmail.equals(editedContact.getEmail())){
            db.updateConversationWindowWithDifferentEmail(originalEmail,editedContact);
        }
        db.updateConversation(originalEmail,editedContact);
    }

}

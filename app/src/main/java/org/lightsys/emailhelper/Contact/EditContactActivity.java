package org.lightsys.emailhelper.Contact;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

public class EditContactActivity extends AppCompatActivity {
    String originalFirstName,originalLastName,originalEmail;
    TextView firstname,lastname,email;
    Button saveContactButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        //set up first name stuff
        firstname = findViewById(R.id.editContactActivity_first_name_edit);
        originalFirstName = getIntent().getStringExtra(getString(R.string.intent_first_name));
        firstname.setText(originalFirstName);
        //setup last name stuff
        lastname = findViewById(R.id.editContactActivity_last_name_edit);
        originalLastName = getIntent().getStringExtra(getString(R.string.intent_last_name));
        lastname.setText(originalLastName);
        //set up email stuff
        email = findViewById(R.id.editContactActivity_email_edit);
        originalEmail = getIntent().getStringExtra(getString(R.string.intent_email));
        email.setText(originalEmail);
        //set up button
        saveContactButton = findViewById(R.id.saveContactButton);
        saveContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveContact();
                finish();
            }
        });

    }
    public void saveContact(){
        boolean changed = false;
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        Contact contact = db.getContact(originalEmail);
        if(contact.getFirstName().equalsIgnoreCase(firstname.getText().toString())){
            contact.setFirstName(firstname.getText().toString());
            changed = true;
        }
        if(contact.getLastName().equalsIgnoreCase(lastname.getText().toString())){
            contact.setLastName(lastname.getText().toString());
            changed = true;
        }
        if(contact.getEmail().equalsIgnoreCase(email.getText().toString())){
            contact.setEmail(email.getText().toString());
            changed = true;
        }
        if(changed){
            Intent passback = new Intent();
            passback.putExtra(getString(R.string.intent_email),contact.getEmail());
            passback.putExtra(getString(R.string.intent_first_name),contact.getFirstName());
            passback.putExtra(getString(R.string.intent_last_name),contact.getLastName());
            passback.putExtra(getString(R.string.intent_original_email),originalEmail);
            setResult(CommonMethods.CONTACT_CHANGED,passback);
            //add intent stuff
            db.updateContact(originalEmail,contact);
        }else{
            setResult(CommonMethods.CONTACT_NOT_CHANGED);
        }

    }
}

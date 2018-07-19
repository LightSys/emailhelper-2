package org.lightsys.emailhelper.Contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.AuthenticationClass;
import org.lightsys.emailhelper.R;

import java.util.Date;

import xdroid.toaster.Toaster;

public class NewContactFragment extends android.app.Fragment {

    DatabaseHelper db;
    Button addContactButton;
    EditText firstNameField, lastNameField, emailField;
    View rootView;


    public NewContactFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_new_contact, container, false);// Inflate the layout for this fragment
        db = new DatabaseHelper(getActivity().getApplicationContext());
        addItems();
        return rootView;
    }

    public void addItems() {
        final Intent passedData = getActivity().getIntent();
        firstNameField = rootView.findViewById(R.id.firstNameField);
        firstNameField.setText(passedData.getStringExtra(getString(R.string.intent_first_name)));
        lastNameField = rootView.findViewById(R.id.lastNameField);
        lastNameField.setText(passedData.getStringExtra(getString(R.string.intent_last_name)));
        emailField = rootView.findViewById(R.id.emailField);
        emailField.setText(passedData.getStringExtra(getString(R.string.intent_email)));
        addContactButton = rootView.findViewById(R.id.addContactButton);
        addContactButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Contact newContact = getContactFromFields();
                    Runnable confirmationRunnable = new Runnable() {
                        @Override
                        public void run() {
                            db.insertContact(newContact);
                            db.insertConversationData(newContact);
                            clearFields();
                            getActivity().finish();//ends the task and reverts to what was going on previously
                        }
                    };
                    if(!CommonMethods.checkEmail(newContact.getEmail()) && passedData.getStringExtra(getString(R.string.intent_email)).equalsIgnoreCase("") ){
                        String message = "EmailHelper does not recognize "+newContact.getEmail()+" as an email. Are you sure you want to add this email?";
                        new ConfirmDialog(message,getString(R.string.confirm_word),getActivity(),confirmationRunnable,null);
                    }else if(newContact.getEmail().equalsIgnoreCase(AuthenticationClass.Email)){
                        Toaster.toastLong(R.string.no_add_self_to_contacts);
                        emailField.getText().clear();
                    }
                    else{
                        confirmationRunnable.run();
                    }


                }
            }
        );
    }



    private void clearFields() {
        emailField.getText().clear();
        firstNameField.getText().clear();
        lastNameField.getText().clear();
    }

    public Contact getContactFromFields() {
        //I spread it out for readability
        Contact contact = new Contact();
        contact.setEmail(emailField.getText().toString());
        contact.setFirstName(firstNameField.getText().toString());
        contact.setLastName(lastNameField.getText().toString());
        Date createdDate = CommonMethods.getCurrentTime();
        createdDate.setHours(0);
        createdDate.setMinutes(0);
        createdDate.setSeconds(0);
        contact.setCreatedDate(createdDate);
        contact.setUpdatedDate(CommonMethods.getCurrentTime());
        contact.setSendNotifications(true);
        contact.setInContacts(true);
        contact.setNumOfReferences(0);
        return contact;
    }
}

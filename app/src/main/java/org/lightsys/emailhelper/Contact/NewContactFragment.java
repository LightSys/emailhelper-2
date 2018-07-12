package org.lightsys.emailhelper.Contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.HelperClass;
import org.lightsys.emailhelper.R;

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
        Intent passedData = getActivity().getIntent();
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
                            db.insertContactData(newContact);
                            db.insertConversationData(newContact, CommonMethods.getCurrentTime(), CommonMethods.getCurrentDate());
                            clearFields();
                            getActivity().finish();//ends the task and reverts to what was going on previously
                        }
                    };

                    if(!CommonMethods.checkEmail(newContact.getEmail()) ){
                        String message = "EmailHelper does not recognize "+newContact.getEmail()+" as an email. Are you sure you want to add this email?";
                        new ConfirmDialog(message,getString(R.string.confirm_word),getActivity(),confirmationRunnable,null);
                    }else if(newContact.getEmail().equalsIgnoreCase(HelperClass.Email)){
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
        return new Contact(emailField.getText().toString(),firstNameField.getText().toString(),lastNameField.getText().toString());
    }
}

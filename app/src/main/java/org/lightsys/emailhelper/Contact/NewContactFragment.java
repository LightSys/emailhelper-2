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

import java.util.Calendar;
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
                            if(passedData.getBooleanExtra(getString(R.string.intent_create_new_contact),false)){
                                db.insertConversationData(newContact);
                            }
                            clearFields();
                            Intent passback = new Intent();
                            passback.putExtra(getString(R.string.intent_email),newContact.getEmail());
                            passback.putExtra(getString(R.string.intent_first_name),newContact.getFirstName());
                            passback.putExtra(getString(R.string.intent_last_name),newContact.getLastName());
                            getActivity().setResult(CommonMethods.NEW_CONTACT_ADDED,passback);
                            getActivity().finish();//ends the task and reverts to what was going on previously
                        }
                    };
                    String checker = passedData.getStringExtra(getString(R.string.intent_email));
                    if(checker == null){
                        checker = "";
                    }
                    if(!CommonMethods.checkEmail(newContact.getEmail()) && checker.equalsIgnoreCase("") ){
                        String message = getString(R.string.email_validation_prestring)+newContact.getEmail()+getString(R.string.email_validation_poststring);
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
        contact.setEmail(emailField.getText().toString().trim());
        contact.setFirstName(firstNameField.getText().toString().trim());
        contact.setLastName(lastNameField.getText().toString().trim());
        Date createdDate = getCreatedDate();
        contact.setCreatedDate(createdDate);
        contact.setUpdatedDate(createdDate);
        contact.setSendNotifications(true);
        contact.setInContacts(true);
        contact.setNumOfReferences(0);
        return contact;
    }
    private Date getCreatedDate(){
        Calendar setter = Calendar.getInstance();
        setter.set(Calendar.SECOND,0);
        setter.set(Calendar.MINUTE,0);
        setter.set(Calendar.HOUR,0);
        if(setter.get(Calendar.DAY_OF_YEAR)>14){
            setter.set(Calendar.DAY_OF_YEAR,setter.get(Calendar.DAY_OF_YEAR)-14);
        }else{
            setter.set(Calendar.DAY_OF_YEAR,setter.get(Calendar.DAY_OF_YEAR)+351);
            setter.set(Calendar.YEAR,setter.get(Calendar.YEAR)-1);
        }
        return setter.getTime();
    }
}

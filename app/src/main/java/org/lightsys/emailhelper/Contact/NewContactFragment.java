package org.lightsys.emailhelper.Contact;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.Contact.ContactFragment;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.MainActivity;
import org.lightsys.emailhelper.R;

import java.util.Calendar;
import java.util.Date;

public class NewContactFragment extends android.app.Fragment {

    DatabaseHelper db;

    Button addContactButton;
    EditText firstNameField, lastNameField, emailField;
    View rootView;

    public NewContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_new_contact, container, false);
        addContactButton = (Button) rootView.findViewById(R.id.addContactButton);
        firstNameField = (EditText) rootView.findViewById(R.id.firstNameField);
        lastNameField = (EditText) rootView.findViewById(R.id.lastNameField);
        emailField = (EditText) rootView.findViewById(R.id.emailField);
        addItems();
        return rootView;
    }

    public void addItems() {
        addContactButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db = new DatabaseHelper(getActivity().getApplicationContext());
                        String insertEmail = emailField.getText().toString();
                        String insertFirstName = firstNameField.getText().toString();
                        String insertLastName = lastNameField.getText().toString();
                        //Contact contact = new Contact(insertEmail, insertFirstName, insertLastName);

                        boolean isInserted = db.insertContactData(insertEmail, insertFirstName, insertLastName);

                        emailField.getText().clear();                                               //Empties EditText field when it is added to the list
                        firstNameField.getText().clear();                                           //Also make sure you don't clear it before you add the data to the DB
                        lastNameField.getText().clear();

                        Date today = Calendar.getInstance().getTime();
                        boolean isConvo = db.insertConversationData(insertEmail, insertFirstName + " " + insertLastName, CommonMethods.getCurrentTime(), CommonMethods.getCurrentDate());
                        if (isInserted) {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.contact_added_prestring)+insertFirstName + " " + insertLastName +getString(R.string.contact_added_poststring), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.contact_not_added_prestring)+insertFirstName + " " + insertLastName+getString(R.string.contact_not_added_poststring), Toast.LENGTH_SHORT).show();
                        }
                        if(isConvo){
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.conversation_added_prestring)+insertFirstName + " " + insertLastName+getString(R.string.conversation_added_poststring), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.conversation_not_added_prestring)+insertFirstName + " " + insertLastName+getString(R.string.conversation_not_added_poststring), Toast.LENGTH_SHORT).show();
                        }
                        ContactFragment temp = new ContactFragment();
                        temp.prepareContactData();
                        NewContactActivity newContactActivity = (NewContactActivity) getActivity();
                        Intent upIntent = new Intent(newContactActivity.getApplicationContext(),ContactActivity.class);
                        newContactActivity.navigateUpTo(upIntent);
                    }
                }
        );
    }

}

package org.lightsys.emailhelper.Contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

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
                    Contact newContact = getContactFromFields();
                    boolean isInserted = db.insertContactData(newContact);
                    boolean isConvo = db.insertConversationData(newContact, CommonMethods.getCurrentTime(), CommonMethods.getCurrentDate());
                    clearFields();
                    if (isInserted) {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.contact_added_prestring)+newContact.getName()+getString(R.string.contact_added_poststring), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.contact_not_added_prestring)+newContact.getName()+getString(R.string.contact_not_added_poststring), Toast.LENGTH_SHORT).show();
                    }
                    if(isConvo){
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.conversation_added_prestring)+newContact.getName()+getString(R.string.conversation_added_poststring), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.conversation_not_added_prestring)+newContact.getName()+getString(R.string.conversation_not_added_poststring), Toast.LENGTH_SHORT).show();
                    }
                    getActivity().finish();//ends the task and reverts to what was going on previously
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

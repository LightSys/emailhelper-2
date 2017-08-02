package com.example.ben.emailhelper;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewContactFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class NewContactFragment extends android.app.Fragment {

    private OnFragmentInteractionListener mListener;

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
                        Contact contact = new Contact(insertEmail, insertFirstName, insertLastName);

                        boolean isInserted = db.insertContactData(insertEmail, insertFirstName,
                                insertLastName);

                        emailField.getText().clear();                                               //Empties EditText field when it is added to the list
                        firstNameField.getText().clear();                                           //Also make sure you don't clear it before you add the data to the DB
                        lastNameField.getText().clear();

                        Date today = Calendar.getInstance().getTime();
                        boolean isConvo = db.insertConversationData(insertEmail, insertFirstName + " " + insertLastName, CommonMethods.getCurrentTime(), today.toString());
                        if (isConvo) {
                            Toast.makeText(getActivity().getApplicationContext(), "Conversation Inserted", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(getActivity().getApplicationContext(), "Conversation Not Inserted", Toast.LENGTH_SHORT).show();

                        MainActivity mainActivity = (MainActivity) getActivity();
                        ContactFragment newContactFragment = new ContactFragment();
                        mainActivity.setFragmentNoBackStack(newContactFragment);
                    }
                }
        );
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

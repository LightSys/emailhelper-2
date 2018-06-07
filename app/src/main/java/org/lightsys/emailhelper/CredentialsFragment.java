package org.lightsys.emailhelper;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class CredentialsFragment extends android.app.Fragment {

    EditText emailField, passwordField;
    Button saveChanges;
    View rootView;


    public CredentialsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_settings, container, false);
        emailField = (EditText) rootView.findViewById(R.id.emailField);
        emailField.setHint(HelperClass.Email);           //This one is so it shows up when the fragment is first opened
        emailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    emailField.setHint("");
                else
                    emailField.setHint(HelperClass.Email);
            }
        });
        passwordField = (EditText) rootView.findViewById(R.id.passwordField);
        passwordField.setHint("••••••••"); //This one is so it shows up when the fragment is first opened
        passwordField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    passwordField.setHint("");
                else
                    passwordField.setHint("••••••••");
            }
        });
        saveChanges = (Button) rootView.findViewById(R.id.saveChangesButton);
        SaveChanges();
        return rootView;
    }

    public void SaveChanges() {
        saveChanges.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HelperClass.Email = emailField.getText().toString();
                        HelperClass.Password = passwordField.getText().toString();

                        // Create object of SharedPreferences
                        SharedPreferences sharedPref = getActivity().getSharedPreferences("myPreferences", 0);
                        // New get Editor
                        SharedPreferences.Editor editor = sharedPref.edit();
                        // Put your values
                        editor.putString(getResources().getString(R.string.key_email), HelperClass.Email);
                        editor.putString(getResources().getString(R.string.key_password), HelperClass.Password);
                        // Apply your edits
                        editor.apply();
                    }
                }
        );
    }
}
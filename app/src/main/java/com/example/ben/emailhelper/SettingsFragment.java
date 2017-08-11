package com.example.ben.emailhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class SettingsFragment extends android.app.Fragment {

    EditText emailField, passwordField;
    Button saveChanges;
    View rootView;


    public SettingsFragment() {
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
        emailField.setHint(HelperClass._Email);                                                     //This one is so it shows up when the fragment is first opened
        emailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    emailField.setHint("");
                else
                    emailField.setHint(HelperClass._Email);
            }
        });
        passwordField = (EditText) rootView.findViewById(R.id.passwordField);
        passwordField.setHint("••••••••");                                                          //This one is so it shows up when the fragment is first opened
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
                        HelperClass._Email = emailField.getText().toString();
                        HelperClass._Password = passwordField.getText().toString();

                        // Create object of SharedPreferences
                        SharedPreferences sharedPref = getActivity().getSharedPreferences("myPreferences", 0);
                        // New get Editor
                        SharedPreferences.Editor editor = sharedPref.edit();
                        // Put your values
                        editor.putString("email", HelperClass._Email);
                        editor.putString("password", HelperClass._Password);
                        // Apply your edits
                        editor.apply();
                    }
                }
        );
    }
}

package org.lightsys.emailhelper.qr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.scheme.VCard;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.UserSettingsActivity;

import xdroid.toaster.Toaster;

public class QRActivity extends AppCompatActivity {
    TextView emailField;
    TextView nameField;
    TextView phoneNumberField;
    TextView addressField;
    TextView companyField;
    TextView websiteField;
    TextView noteField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Button editContactInformation = findViewById(R.id.editUserSettings);
        editContactInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editInfo = new Intent(getApplicationContext(), UserSettingsActivity.class);
                startActivity(editInfo);
            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
        String email = setUpTextView(emailField,R.id.email_qr_field,getString(R.string.key_email));
        String name = setUpNameTextView();
        String phone = setUpTextView(phoneNumberField,R.id.phone_qr_field,getString(R.string.key_phone_number));
        String address = setUpTextView(addressField,R.id.address_qr_field,getString(R.string.key_address));
        String company = setUpTextView(companyField,R.id.company_qr_field,getString(R.string.key_company));
        String website = setUpTextView(websiteField,R.id.website_qr_field,getString(R.string.key_website));
        String note = setUpTextView(noteField,R.id.note_qr_field,getString(R.string.key_note));

        VCard contactInfo = new VCard();
        contactInfo.setEmail(email);
        contactInfo.setName(name);
        contactInfo.setPhoneNumber(phone);
        contactInfo.setAddress(address);
        contactInfo.setCompany(company);
        contactInfo.setWebsite(website);
        contactInfo.setNote(note);
        Bitmap myBit = QRCode.from(contactInfo).bitmap();
        ImageView myImage = findViewById(R.id.imageView);
        myImage.setImageBitmap(myBit);
    }

    private String setUpNameTextView() {
        Boolean hasCreated = true;
        SharedPreferences sp = getSharedPreferences(getString(R.string.preferences),0);
        String firstName = sp.getString(getString(R.string.key_first_name),getString(R.string.default_first_name));
        if(firstName.equalsIgnoreCase(getString(R.string.default_first_name))){
            firstName = "";
            hasCreated = false;
        }
        String lastName = sp.getString(getString(R.string.key_last_name),getString(R.string.default_last_name));
        if(firstName.equalsIgnoreCase(getString(R.string.default_last_name))){
            lastName = "";
            hasCreated = false;
        }
        nameField = findViewById(R.id.name_qr_field);
        if(!hasCreated){
            CommonMethods.textViewMinimize(nameField);
            Toaster.toastLong(getString(R.string.barcode_without_name));
        }
        else{
            String setText = "Name: " + firstName +" "+ lastName;
            nameField.setText(setText);
        }
        return firstName + " " + lastName;

    }

    public String setUpTextView(TextView textView,int id, String spKey){
        SharedPreferences sp = getSharedPreferences(getString(R.string.preferences),0);
        textView = findViewById(id);
        String data = sp.getString(spKey,null);
        if(data == null){
            data = "";
            CommonMethods.textViewMinimize(textView);
        }
        else{
            textView.setText(setText(data,spKey));
        }
        return data;
    }

    private String setText(String data, String key) {
        switch(key){
            case "email":
                return "Email: " + data;
            case "phone_number":
                return "Phone Number: "+data;
            case "address":
                return "Address: " +data;
            case "company":
                return "Company: " + data;
            case "website":
                return "Website: " +data;
            case "note":
                return "Note: " + data;
            default:
                return "";
        }
    }
}

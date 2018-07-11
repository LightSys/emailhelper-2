package org.lightsys.emailhelper.qr;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.scheme.VCard;

import org.lightsys.emailhelper.R;

import xdroid.toaster.Toaster;

public class QRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean hasCreated = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        SharedPreferences sp = getSharedPreferences(getString(R.string.preferences),0);
        String email = sp.getString(getString(R.string.key_email),getString(R.string.default_email));//takes logged in email
        String firstName = sp.getString(getString(R.string.key_first_name),getString(R.string.default_first_name));//grabs names from preferences
        String lastName = sp.getString(getString(R.string.key_last_name),getString(R.string.default_last_name));
        if(firstName.equals(getString(R.string.default_first_name))){//Checks to make sure they are set
            firstName = "";
            hasCreated = false;
        }
        if(lastName.equals(getString(R.string.default_last_name))){
            lastName = "";
            hasCreated = false;
        }
        TextView emailField = findViewById(R.id.email_qr_field);
        String emailSet = getString(R.string.qr_email)+email;
        emailField.setText(emailSet);
        TextView nameField = findViewById(R.id.name_qr_field);
        String nameSet = getString(R.string.qr_name)+firstName+" "+lastName;
        if(nameSet.equalsIgnoreCase("Name:  ")){
            nameField.setVisibility(View.INVISIBLE);
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
            nameField.setLayoutParams(size);
        }
        else{
            nameField.setText(nameSet);
        }
        VCard contactInfo = new VCard();
        contactInfo.setName(firstName +" "+lastName);
        contactInfo.setEmail(email);
        Bitmap myBit = QRCode.from(contactInfo).bitmap();
        ImageView myImage = findViewById(R.id.imageView);
        myImage.setImageBitmap(myBit);
        if(!hasCreated){
            Toaster.toastLong(getString(R.string.barcode_without_name));
        }
    }

}

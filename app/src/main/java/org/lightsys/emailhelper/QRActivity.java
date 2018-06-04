package org.lightsys.emailhelper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import net.glxn.qrgen.android.QRCode;

public class QRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        String email = getSharedPreferences(getString(R.string.preferences),0).getString(getString(R.string.key_email),getString(R.string.default_email));
        Bitmap myBit = QRCode.from(email).bitmap();
        ImageView myImage = findViewById(R.id.imageView);
        myImage.setImageBitmap(myBit);
    }

}

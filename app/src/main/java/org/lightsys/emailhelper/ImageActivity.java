package org.lightsys.emailhelper;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {
    ImageView attachmentImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(getString(R.string.image_title));
        attachmentImage = findViewById(R.id.attachment_image);
        attachmentImage.setImageBitmap(BitmapFactory.decodeFile(getIntent().getStringExtra("file_path")));
    }

}

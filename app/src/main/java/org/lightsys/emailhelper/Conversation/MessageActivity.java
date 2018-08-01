package org.lightsys.emailhelper.Conversation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

/**
 * The MessageActivity was built for displaying longer messages when they are clicked in the
 * ConversationActivity.
 * Created by DSHADE Summer 2018.
 */
public class MessageActivity extends AppCompatActivity {
    Intent incoming;
    DatabaseHelper db;
    String messageID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        incoming = getIntent();
        messageID = incoming.getStringExtra(getString(R.string.intent_message_id));//gets the message ID
        db = new DatabaseHelper(getApplicationContext());//connects to the database
        Message displayMessage = db.getMessage(messageID);//gets data from database
        setTitle(displayMessage.getSubject());//sets the content to the message subject
        setContentView(R.layout.activity_message);

        //This section makes the back button go up one panel instead of to MainActivity.
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        TextView body = findViewById(R.id.message_activity_body);
        body.setText(displayMessage.getMessage());//sets up the content

        //Links messages if enabled.
        SharedPreferences sp = getSharedPreferences(getString(R.string.preferences), CommonMethods.SHARED_PREFERENCES_DEFAULT_MODE);
        if(sp.getBoolean(getString(R.string.key_link_messages),getApplicationContext().getResources().getBoolean(R.bool.default_link_messages))){
            Linkify.addLinks(body,Linkify.ALL);
        }
    }
}

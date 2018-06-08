package org.lightsys.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.lightsys.emailhelper.Conversation.ConversationWindowFragment;

/**************************************************************************************************
 *  This is the activity where everything happens in the message window, outside of a             *
 *  BottomNavigationActivity.                                                                     *
 **************************************************************************************************/

public class MessageWindowActivity extends AppCompatActivity {

    ConversationWindowFragment chats = new ConversationWindowFragment();
    Intent intent;
    public String email;

    public void setFragmentNoBackStack(Fragment frag){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.message_window, frag);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_window);
        intent = getIntent();
        email = intent.getStringExtra(getString(R.string.intent_email));
        passToFragment();
        setFragmentNoBackStack(chats);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**********************************************************************************************
     *  The email that is passed was passed to this activity. This function passes it to the      *
     *  fragment that exists inside of the activity.                                              *
     **********************************************************************************************/

    public void passToFragment() {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.intent_email), email);
        chats.setArguments(bundle);
    }
}

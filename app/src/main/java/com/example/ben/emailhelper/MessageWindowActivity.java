package com.example.ben.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.BaseBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
        email = intent.getStringExtra("email");
        passToFragment();
        setFragmentNoBackStack(chats);
    }

    /**********************************************************************************************
     *  The email that is passed was passed to this activity. This function passes it to the      *
     *  fragment that exists inside of the activity.                                              *
     **********************************************************************************************/

    public void passToFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        chats.setArguments(bundle);
    }
}
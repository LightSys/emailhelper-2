package com.example.ben.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import android.view.View;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;

import android.app.FragmentTransaction;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;



import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //This is an instance of the database for testing
    DatabaseHelper db;

    //These variables are used in the list view
    private List<Conversation> conversationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConversationAdapter cAdapter;

    //These variables handle the button and text field for adding items to the list
    private EditText newEmail;
    private int clickCounter = 0;

    public void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void goToSelector() {
        Intent intent = new Intent(this, SelectorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    /*public void setFragmentNoBackStack(Fragment frag){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, frag);
        transaction.commit();
    }

    public void setFragment(Fragment frag) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }*/


    /*
     *  I still need to understand this part a little more. Not sure what the difference between
     *  onMove and onSwiped is, or if we even use onMove.
     *  -Nick
     */
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            Toast.makeText(MainActivity.this, "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Toast.makeText(MainActivity.this, "on Swiped ", Toast.LENGTH_SHORT).show();

            int itemPosition = viewHolder.getAdapterPosition();

            //Need to delete it from DB before getting rid of it from the list
            Integer deletedRows = db.deleteData(conversationList.get(itemPosition).getEmail());
            if (deletedRows > 0)
                Toast.makeText(MainActivity.this, "Data Deleted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "Data Not Deleted", Toast.LENGTH_SHORT).show();

            //Remove swiped item from list and notify the RecyclerView
            conversationList.remove(itemPosition);
            cAdapter.notifyDataSetChanged();
        }
    };


    public void makeRecyclerView() {
        db = new DatabaseHelper(this);                                                              //Creates instance of database

        newEmail = (EditText) findViewById(R.id.addEmailButton);                                    //Sets newEmail varable to the EditText field

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);                             //Makes the RecyclerView

        cAdapter = new ConversationAdapter(conversationList);                                       //Adapter for the Conversations

        //recyclerView.setHasFixedSize(true);                                                       I don't think we want this because we will be adding and removing
        //                                                                                          conversations often.


        RecyclerView.LayoutManager cLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Conversation conversation = conversationList.get(position);
                Toast.makeText(getApplicationContext(), conversation.getEmail() + " is selected!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {}
        }));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        prepareConversationData();
    }

    /*
     *  Most of the stuff in here was used for testing to set up the RecyclerView. It probably
     *  should be moved once everything starts coming together. Most likely we will want this in the
     *  MessageListFragment class or in a similar location.
     *  -Nick
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        goToSelector();
    }

    /*
     *  This function is what the button in content_main.xml calls. It will need to be changed in
     *  add a persons name/email address to the list instead of meaningless text
     *  -Nick
     */

    public void addItems(View v) {
        Conversation conversation = new Conversation(newEmail.getText().toString(), newEmail
                .getText().toString(), CommonMethods.getCurrentTime());

        boolean isInserted = db.insertData(newEmail.getText().toString(), newEmail.getText()
                .toString(), CommonMethods.getCurrentTime());
        if (isInserted) {
            Toast.makeText(MainActivity.this, "Data Inserted", Toast.LENGTH_SHORT).show();
            conversationList.add(0, conversation);                                                  //Adds data to first position of list, making it display at the top
            cAdapter.notifyDataSetChanged();
        }
        else
            Toast.makeText(MainActivity.this, "Data Not Inserted", Toast.LENGTH_SHORT).show();

        newEmail.getText().clear();                                                                 //Empties EditText field when it is added to the list
                                                                                                    //Also make sure you don't clear it before you add the data to the DB
    }


    public void prepareConversationData() {
        Cursor res = db.getData();
        while (res.moveToNext()) {
            Conversation conversation = new Conversation(res.getString(0), res.getString(1),
                    res.getString(2));
            conversationList.add(conversation);
        }
        cAdapter.notifyDataSetChanged();
    }
}
package org.lightsys.emailhelper.Contact;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.DividerItemDecoration;
import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.RecyclerTouchListener;

import java.net.URL;

import xdroid.toaster.Toaster;

public class InboxContactActivity extends AppCompatActivity {
    DatabaseHelper db;
    private ContactList contactList;
    private RecyclerView recyclerView;
    private InboxContactAdapter adapter;
    Context context;
    Resources resources;
    SharedPreferences sp;
    CheckBox showDatabaseContacts;
    SwipeRefreshLayout swipeContainer;
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_contact);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setUpClassVariables();
        setUpCheckBox();
        setUpContainer();
    }
    private void setUpContainer(){
        swipeContainer = findViewById(R.id.fragment_inbox_contact);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new refresh().execute();
            }
        });
        simpleItemTouchCallback  = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            /******************************************************************************************
             *  The onMove function has to be there for the ItemTouchHelper to be happy.              *
             *  There shouldn't be anything that we need to use it for.                               *
             *  -Nick                                                                                 *
             ******************************************************************************************/
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

            }
            Runnable deletionRunnable = new Runnable() {
                @Override
                public void run() {

                }
            };
            Runnable cancelRunnable = new Runnable(){
                @Override
                public void run() {

                }
            };
        };
        makeRecyclerView();
    }
    private void setUpCheckBox(){
        showDatabaseContacts = findViewById(R.id.showDatabaseContactsCheckBox);
        showDatabaseContacts.setChecked(sp.getBoolean(getString(R.string.key_show_database_contacts),resources.getBoolean(R.bool.default_show_database_contacts)));
        showDatabaseContacts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = context.getSharedPreferences(getString(R.string.preferences),0);
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean(getString(R.string.key_show_database_contacts),isChecked);
                edit.apply();
                new refresh().execute();
                swipeContainer.setRefreshing(true);
            }
        });
    }
    private void setUpClassVariables() {
        context = getApplicationContext();
        db = new DatabaseHelper(context);
        resources = context.getResources();
        sp = context.getSharedPreferences(getString(R.string.preferences),0);

    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeContainer.setRefreshing(true);
        new refresh().execute();
    }
    public void makeRecyclerView() {
        recyclerView = findViewById(R.id.recycler_inbox_contact_view);//Makes the RecyclerView

        RecyclerView.LayoutManager cLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(cLayoutManager);
        //This ItemDecoration was working at the start but I don't know I did to make it stop
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /**
         *  We can probably make tapping on a contact open the NewContactFragment to change the info
         *  for it. Somehow we need the ability to edit the info.
         */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //
            }
            @Override
            public void onLongClick(View view, int position) {
                //
            }
        }));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    public void prepareContactData() {
        SharedPreferences sp = context.getSharedPreferences(getString(R.string.preferences),0);
        boolean temp = sp.getBoolean(getString(R.string.key_show_database_contacts),context.getResources().getBoolean(R.bool.default_show_database_contacts));
        adapter = new InboxContactAdapter(contactList,temp);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    //Async class for refresh stuff
    class refresh extends AsyncTask<URL, Integer, Long> {
        Handler handler;
        @Override
        protected Long doInBackground(URL... urls) {
            handler = new Handler(Looper.getMainLooper());
            GetMail mailer = new GetMail(context);
            contactList = mailer.getContactsFromInbox();
            return null;
        }
        @Override
        protected void onPostExecute(Long result){
            swipeContainer.setRefreshing(false);//Must be called or refresh circle will continue forever
            //Toaster.toast(R.string.refresh_finished);
            prepareContactData();
        }
    }
}

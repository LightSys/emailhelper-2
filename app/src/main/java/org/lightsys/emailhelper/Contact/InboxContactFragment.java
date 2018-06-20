package org.lightsys.emailhelper.Contact;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.lightsys.emailhelper.Conversation.ConversationFragment;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.DividerItemDecoration;
import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.RecyclerTouchListener;
import org.lightsys.emailhelper.emailNotification;

import java.net.URL;

import xdroid.toaster.Toaster;

public class InboxContactFragment extends android.app.Fragment {

    //This is an instance of the database for testing
    DatabaseHelper db;

    //These variables are used in the list view
    private ContactList contactList;
    private RecyclerView recyclerView;
    private InboxContactAdapter adapter;
    View rootView;//This variable had to be made global so that the list wouldn't duplicate data
    Context context;
    CheckBox showDatabaseContacts;
    SwipeRefreshLayout swipeContainer;


    public InboxContactFragment() {
    }// Required empty public constructor


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();



    }
    public void setContactList(ContactList contactList){
        this.contactList = contactList;
    }

    /**********************************************************************************************
     *  This stuff is put in the onCreateView because it's a fragment and it needs a view to      *
     *  declare the button and stuff, which you can't get in the onCreate function. In the others *
     *  this is put in an if statement to only called the first time, but I think this one isn't  *
     *  because of something with adding a new contact form.                                      *
     *                                                                                            *
     *  After looking through it, I don't think it's the most efficient way to do it since the    *
     *  entire list is being deleted and remade every time somebody loads the fragment. It works  *
     *  for now but will probably need to be changed in the future.                               *
     *  -Nick                                                                                     *
     **********************************************************************************************/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_inbox_contact, container, false);
        showDatabaseContacts = rootView.findViewById(R.id.showDatabaseContactsCheckBox);
        SharedPreferences sp = context.getSharedPreferences(getString(R.string.preferences),0);
        Resources r = context.getResources();
        swipeContainer = rootView.findViewById(R.id.fragment_inbox_contact);
        boolean showDatabaseContactsBoolean = sp.getBoolean(getString(R.string.key_show_database_contacts),r.getBoolean(R.bool.default_show_database_contacts));
        showDatabaseContacts.setChecked(showDatabaseContactsBoolean);
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
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh myRefresher = new refresh();
                myRefresher.execute();
            }
        });
        makeRecyclerView(rootView);



        return rootView;
    }

    /**********************************************************************************************
     *                        Function used to handle swiping to delete.                          *
     **********************************************************************************************/

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

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


    /**********************************************************************************************
     *          Has all the steps needed to make the RecyclerView that holds the contacts.        *
     **********************************************************************************************/

    public void makeRecyclerView(View view) {
        db = new DatabaseHelper(getActivity().getApplicationContext());//Creates instance of database

        recyclerView = view.findViewById(R.id.recycler_inbox_contact_view);//Makes the RecyclerView

        adapter = new InboxContactAdapter(contactList, showDatabaseContacts.isChecked());

        RecyclerView.LayoutManager cLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        //This ItemDecoration was working at the start but I don't know I did to make it stop
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        /**
         *  We can probably make tapping on a contact open the NewContactFragment to change the info
         *  for it. Somehow we need the ability to edit the info.
         */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
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
        prepareContactData();
    }


    /**********************************************************************************************
     *                      Clears contactList and inserts data from database                     *
     **********************************************************************************************/

    public void prepareContactData() {
        adapter = new InboxContactAdapter(contactList,showDatabaseContacts.isChecked());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public boolean getContactListStatus() {
        return contactList != null;
    }
    class refresh extends AsyncTask<URL, Integer, Long> {
        Handler handler;
        @Override
        protected Long doInBackground(URL... urls) {
            handler = new Handler(Looper.getMainLooper());
            GetMail mailer = new GetMail(getActivity().getApplicationContext());
            contactList = mailer.getContactsFromInbox();
            return null;
        }
        @Override
        protected void onPostExecute(Long result){
            swipeContainer.setRefreshing(false);//Must be called or refresh circle will continue forever
            Toaster.toast(R.string.refresh_finished);
            prepareContactData();
        }
    }
}
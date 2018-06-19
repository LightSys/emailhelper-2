package org.lightsys.emailhelper.Contact;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.DividerItemDecoration;
import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.NotificationBase;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.RecyclerTouchListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InboxContactFragment extends android.app.Fragment {

    //This is an instance of the database for testing
    DatabaseHelper db;

    //These variables are used in the list view
    private ContactList contactList;
    private RecyclerView recyclerView;
    private InboxContactAdapter adapter;
    View rootView;//This variable had to be made global so that the list wouldn't duplicate data
    Context context;


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

        adapter = new InboxContactAdapter(contactList);

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
        adapter = new InboxContactAdapter(contactList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
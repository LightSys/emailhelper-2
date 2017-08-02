package com.example.ben.emailhelper;

import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends android.app.Fragment {

    //This is an instance of the database for testing
    DatabaseHelper db;

    //These variables are used in the list view
    private List<Contact> contactList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    View rootView;                                                                                  //This variable had to be made global so that the list wouldn't duplicate data

    //These variables handle the button and text field for adding items to the list
    //Right now the EditText field isn't doing anything and the layout needs to change for that
    private EditText newContact;

    Button addContactButton;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        makeRecyclerView(rootView);
        addContactButton = (Button) rootView.findViewById(R.id.addContactButton);
        //addItems();
        goToNewContact();
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
            Toast.makeText(getActivity().getApplicationContext(), "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

            int itemPosition = viewHolder.getAdapterPosition();

            //Need to delete it from DB before getting rid of it from the list
            Integer deletedRows = db.deleteContactData(contactList.get(itemPosition).getEmail());
            if (deletedRows > 0)
                Toast.makeText(getActivity().getApplicationContext(), "Data Deleted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity().getApplicationContext(), "Data Not Deleted", Toast.LENGTH_SHORT).show();

            //Remove swiped item from list and notify the RecyclerView
            contactList.remove(itemPosition);
            contactAdapter.notifyDataSetChanged();
        }
    };

    /**********************************************************************************************
     *          Has all the steps needed to make the RecyclerView that holds the contacts.        *
     **********************************************************************************************/

    public void makeRecyclerView(View view) {
        db = new DatabaseHelper(getActivity().getApplicationContext());                             //Creates instance of database

        newContact = (EditText) view.findViewById(R.id.addContactField);                            //Sets newEmail variable to the EditText field

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_contact_view);                //Makes the RecyclerView

        contactAdapter = new ContactAdapter(contactList);                                           //Adapter for the Contacts

        RecyclerView.LayoutManager cLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        //This ItemDecoration was working at the start but I don't know I did to make it stop
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(contactAdapter);

        /**
         *  We can probably make tapping on a contact open the NewContactFragment to change the info
         *  for it. Somehow we need the ability to edit the info.
         */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Contact contact = contactList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), contact.getEmail() + " is selected!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onLongClick(View view, int position) {}
        }));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        prepareContactData();
    }

    /**********************************************************************************************
     *              Function for when the plus button is clicked to add a new contact             *
     **********************************************************************************************/
    public void goToNewContact() {
        addContactButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if (mainActivity instanceof MainActivity) {
                            NewContactFragment newContactFragment = new NewContactFragment();
                            mainActivity.setFragment(newContactFragment);
                        }
                    }
                }
        );
    }

    /**********************************************************************************************
     *                      Clears contactList and inserts data from database                     *
     **********************************************************************************************/

    public void prepareContactData() {

        int size = contactList.size();                                                              //I clear the list here so that we can update it after new contacts are added from the
        contactList.clear();                                                                        //new contact fragment
        contactAdapter.notifyItemRangeRemoved(0,size);

        Cursor res = db.getContactData();
        while (res.moveToNext()) {
            Contact contact = new Contact(res.getString(0), res.getString(1), res.getString(2));
            contactList.add(contact);
        }
        contactAdapter.notifyDataSetChanged();
    }
}

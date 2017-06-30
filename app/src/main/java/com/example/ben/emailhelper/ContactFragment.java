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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ContactFragment extends android.app.Fragment {

    //This is an instance of the database for testing
    DatabaseHelper db;

    //These variables are used in the list view
    private List<Contact> contactList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    View rootView;

    //These variables handle the button and text field for adding items to the list
    private EditText newContact;

    Button addContactButton;

    private OnFragmentInteractionListener mListener;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_contact, container, false);
            makeRecyclerView(rootView);
            addContactButton = (Button) rootView.findViewById(R.id.addContactButton);
            //addItems();
            goToNewContact();
        }
        return rootView;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            Toast.makeText(getActivity().getApplicationContext(), "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Toast.makeText(MainActivity.this, "on Swiped ", Toast.LENGTH_SHORT).show();

            int itemPosition = viewHolder.getAdapterPosition();

            //Need to delete it from DB before getting rid of it from the list
            Integer deletedRows = db.deleteContactData(contactList.get(itemPosition).getEmail());
            if (deletedRows > 0)
                Toast.makeText(getActivity().getApplicationContext(), "Data Deleted",
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity().getApplicationContext(), "Data Not Deleted",
                        Toast.LENGTH_SHORT).show();

            //Remove swiped item from list and notify the RecyclerView
            contactList.remove(itemPosition);
            contactAdapter.notifyDataSetChanged();
        }
    };

    public void makeRecyclerView(View view) {
        db = new DatabaseHelper(getActivity().getApplicationContext());                             //Creates instance of database

        newContact = (EditText) view.findViewById(R.id.addContactField);                             //Sets newEmail varable to the EditText field

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_contact_view);                //Makes the RecyclerView

        contactAdapter = new ContactAdapter(contactList);                                           //Adapter for the Conversations

        //recyclerView.setHasFixedSize(true);                                                       I don't think we want this because we will be adding and removing
        //                                                                                          conversations often.


        RecyclerView.LayoutManager cLayoutManager = new LinearLayoutManager(getActivity()
                .getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()
                .getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(contactAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity()
                .getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Contact contact = contactList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), contact.getEmail() +
                        " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {}
        }));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        prepareContactData();
    }

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

    public void addItems() {
        addContactButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String contactString = newContact.getText().toString();
                        String[] split = contactString.split("\\s+");
                        Contact contact = new Contact(contactString + "@email.com", split[0], split[1]);

                        boolean isInserted = db.insertContactData(contactString + "@email.com",
                                split[0], split[1]);
                        if (isInserted) {
                            Toast.makeText(getActivity().getApplicationContext(), "Data Inserted",
                                    Toast.LENGTH_SHORT).show();
                            contactList.add(0, contact);                                            //Adds data to first position of list, making it display at the top
                            contactAdapter.notifyDataSetChanged();
                        }
                        else
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Data Not Inserted", Toast.LENGTH_SHORT).show();

                        newContact.getText().clear();                                                 //Empties EditText field when it is added to the list
                        //Also make sure you don't clear it before you add the data to the DB
                    }
                }
        );
    }

    public void prepareContactData() {
        Cursor res = db.getContactData();
        while (res.moveToNext()) {
            Contact contact = new Contact(res.getString(0), res.getString(1),
                    res.getString(2));
            contactList.add(contact);
        }
        contactAdapter.notifyDataSetChanged();
    }
}

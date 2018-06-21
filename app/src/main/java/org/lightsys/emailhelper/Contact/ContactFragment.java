package org.lightsys.emailhelper.Contact;

import android.content.Intent;
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
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.RecyclerTouchListener;
import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends android.app.Fragment {
    //These variables are used in the list view
    private List<Contact> contactList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    DatabaseHelper db;
    View rootView;//This variable had to be made global so that the list wouldn't duplicate data
    //simpleItemTouchCallback is used to delete Contacts via swiping
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
        int deleteRow;
        String deleteName;
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            deleteRow = viewHolder.getAdapterPosition();
            deleteName = contactList.get(deleteRow).getFirstName()+" "+contactList.get(deleteRow).getLastName();
            String deletionMessage = getString(R.string.contact_delete_message_prestring)+deleteName+getString(R.string.contact_delete_message_poststring);
            new ConfirmDialog(deletionMessage,getString(R.string.delete_word),getActivity(),deletionRunnable,cancelRunnable);
        }
        Runnable deletionRunnable = new Runnable() {
            @Override
            public void run() {
                Integer deletedRows = db.deleteContactData(contactList.get(deleteRow).getEmail());
                db.deleteConversationData(contactList.get(deleteRow).getEmail());
                if (deletedRows > 0)//Remove from Database
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.contact_deleted_prestring)+deleteName+getString(R.string.contact_deleted_poststring), Toast.LENGTH_SHORT).show();
                contactList.remove(deleteRow);//Then delete from list
                prepareContactData();
            }
        };
        Runnable cancelRunnable = new Runnable(){
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.contact_not_deleted_prestring)+deleteName+getString(R.string.contact_not_deleted_poststring), Toast.LENGTH_SHORT).show();
                prepareContactData();
            }
        };
    };


    public ContactFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_contact, container, false);// Inflate the layout for this fragment
        makeRecyclerView(rootView);//RecyclerView needs to be created in onCreateView where is can reference the view.
        db = new DatabaseHelper(getActivity().getApplicationContext());
        return rootView;
    }

    /**
     * Has all the steps needed to make the RecyclerView that holds the contacts.
     */
    public void makeRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_contact_view);//Makes the RecyclerView
        RecyclerView.LayoutManager cLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {//To edit the contact settings
                Contact contact = contactList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), contact.getEmail() + getString(R.string.is_selected), Toast.LENGTH_SHORT).show();
                Intent editContactDetails = new Intent(getActivity().getApplicationContext(),ContactSettingsActivity.class);
                editContactDetails.putExtra(getString(R.string.intent_email),contact.getEmail());
                editContactDetails.putExtra(getString(R.string.intent_first_name),contact.getFirstName());
                editContactDetails.putExtra(getString(R.string.intent_last_name),contact.getLastName());
                startActivity(editContactDetails);
            }
            @Override
            public void onLongClick(View view, int position) {//To edit the contact
                Contact contact = contactList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), contact.getEmail() + getString(R.string.is_selected), Toast.LENGTH_SHORT).show();
                Intent editContact = new Intent(getActivity().getApplicationContext(),EditContactActivity.class);
                editContact.putExtra(getString(R.string.intent_email),contact.getEmail());
                editContact.putExtra(getString(R.string.intent_first_name),contact.getFirstName());
                editContact.putExtra(getString(R.string.intent_last_name),contact.getLastName());
                startActivity(editContact);
            }
        }));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * This function clears contactList and gets new data from the database.
     * Should be used just before the Data appears on the screen.
     */
    public void prepareContactData() {
        contactList.clear();
        contactList = db.getContacts();
        contactAdapter = new ContactAdapter(contactList);
        recyclerView.setAdapter(contactAdapter);
    }
}

package com.example.ben.emailhelper;

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
 * {@link ConversationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ConversationFragment extends android.app.Fragment {

    //This is an instance of the database for testing
    DatabaseHelper db;

    //These variables are used in the list view
    private List<Conversation> conversationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConversationAdapter cAdapter;
    View rootView;                                                                                  //This variable had to be made global so that the list wouldn't duplicate data

    //These variables handle the button and text field for adding items to the list
    private EditText newEmail;

    Button addConversationButton;

    private OnFragmentInteractionListener mListener;                                                //I'll keep this here in case it's needed. I think everything is gonna be transferred using the database

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
     *  Normally, you would put this stuff in the onCreate function, but because it is a fragment,
     *  it needs to be in the onCreateView so you can reference the view that it inflates. That view
     *  is put into a while loop to see if it has been created and only acts the first time,
     *  otherwise it duplicates data in the RecyclerView list.
     *  -Nick
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_conversation, container, false);
            makeRecyclerView(rootView);
            addConversationButton = (Button) rootView.findViewById(R.id.addBtn);
            addItems();
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
    public interface OnFragmentInteractionListener {                                                //This goes with the other thing above, so it stays
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
            Integer deletedRows = db.deleteConversationData(conversationList.get(itemPosition).getEmail());
            if (deletedRows > 0)
                Toast.makeText(getActivity().getApplicationContext(), "Data Deleted",
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity().getApplicationContext(), "Data Not Deleted",
                        Toast.LENGTH_SHORT).show();

            //Remove swiped item from list and notify the RecyclerView
            conversationList.remove(itemPosition);
            cAdapter.notifyDataSetChanged();
        }
    };

    public void makeRecyclerView(View view) {
        db = new DatabaseHelper(getActivity().getApplicationContext());                             //Creates instance of database

        newEmail = (EditText) view.findViewById(R.id.addEmailButton);                               //Sets newEmail varable to the EditText field

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);                        //Makes the RecyclerView

        cAdapter = new ConversationAdapter(conversationList);                                       //Adapter for the Conversations

        //recyclerView.setHasFixedSize(true);                                                       I don't think we want this because we will be adding and removing
        //                                                                                          conversations often.


        RecyclerView.LayoutManager cLayoutManager = new LinearLayoutManager(getActivity()
                .getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()
                .getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity()
                .getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Conversation conversation = conversationList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), conversation.getEmail() +
                        " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {}
        }));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        prepareConversationData();
    }

    public void addItems() {
        addConversationButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Conversation conversation = new Conversation(newEmail.getText().toString(),
                                newEmail.getText().toString(), CommonMethods.getCurrentTime());

                        boolean isInserted = db.insertConversationData(newEmail.getText().toString(),
                                newEmail.getText()
                                .toString(), CommonMethods.getCurrentTime());
                        if (isInserted) {
                            Toast.makeText(getActivity().getApplicationContext(), "Data Inserted",
                                    Toast.LENGTH_SHORT).show();
                            conversationList.add(0, conversation);                                  //Adds data to first position of list, making it display at the top
                            cAdapter.notifyDataSetChanged();
                        }
                        else
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Data Not Inserted", Toast.LENGTH_SHORT).show();

                        newEmail.getText().clear();                                                 //Empties EditText field when it is added to the list
                                                                                                    //Also make sure you don't clear it before you add the data to the DB
                    }
                }
        );
    }

    public void prepareConversationData() {
        Cursor res = db.getConversationData();
        while (res.moveToNext()) {
            Conversation conversation = new Conversation(res.getString(0), res.getString(1),
                    res.getString(2));
            conversationList.add(conversation);
        }
        cAdapter.notifyDataSetChanged();
    }
}

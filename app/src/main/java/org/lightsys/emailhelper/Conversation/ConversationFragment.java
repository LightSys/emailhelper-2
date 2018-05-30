package org.lightsys.emailhelper.Conversation;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.DividerItemDecoration;
import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.MessageWindowActivity;
import org.lightsys.emailhelper.NotificationBase;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.RecyclerTouchListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConversationFragment extends android.app.Fragment {

    //This is an instance of the database for testing
    DatabaseHelper db;

    //These variables are used in the list view
    private List<Conversation> conversationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConversationAdapter cAdapter;
    private SwipeRefreshLayout swipeContainer;
    View rootView;
    SharedPreferences sp;
    Resources r;
    //This variable had to be made global so that the list wouldn't duplicate data

    public ConversationFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getActivity().getApplication().getSharedPreferences("myPreferences", 0);
        r = getResources();
    }

    /**********************************************************************************************
     *  Normally, you would put this stuff in the onCreate function, but because it is a fragment *
     *  it needs to be in the onCreateView so you can reference the view that it inflates. That   *
     *  view is put into an if statement to see if it has been created and only acts the first    *
     *  time, otherwise it duplicates data in the RecyclerView list.                              *
     *                                                                                            *
     *  This may need to be changed to be like the ContactFragment if we want the conversation to *
     *  be added as soon as there is a new contact added. Otherwise you need to restart the app   *
     *  for it to take effect.                                                                    *
     *  -Nick                                                                                     *
     **********************************************************************************************/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_conversation, container, false);
        makeRecyclerView(rootView);
        db = new DatabaseHelper(rootView.getContext());
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            // Code to refresh the list here.
            GetMail mailer = new GetMail(db,sp,r);
            mailer.execute();
            // Make sure you call swipeContainer.setRefreshing(false)
            swipeContainer.setRefreshing(false);//Must be called
            Toast.makeText(getActivity().getApplicationContext(), "Refresh in Progress",
                        Toast.LENGTH_SHORT).show();
            prepareConversationData();
            }
        });
        return rootView;
    }

    /**********************************************************************************************
     *                        Function used to handle swiping to delete.                          *
     **********************************************************************************************/

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {
        /**
         *  onMove probably doesn't need to be used by us, but you need it for the ItemTouchHelper
         *  to be happy.
         *  -Nick
         */
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            Toast.makeText(getActivity().getApplicationContext(), "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
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

    /**********************************************************************************************
     *        Has all the steps needed to make the RecyclerView that holds the conversations.     *
     **********************************************************************************************/

    public void makeRecyclerView(View view) {
        db = new DatabaseHelper(getActivity().getApplicationContext());                             //Creates instance of database

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);                        //Makes the RecyclerView

        cAdapter = new ConversationAdapter(conversationList);                                       //Adapter for the Conversations

        RecyclerView.LayoutManager cLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Conversation conversation = conversationList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), conversation.getEmail() + " is selected!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity().getBaseContext(), MessageWindowActivity.class);
                intent.putExtra("email", conversation.getEmail());
                startActivity(intent);
            }
            @Override
            public void onLongClick(View view, int position) {}
        }));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        prepareConversationData();
    }

    /**********************************************************************************************
     *                  Function to put database items into the conversationList                  *
     **********************************************************************************************/

    public void prepareConversationData() {
        int size = conversationList.size();
        conversationList.clear();
        cAdapter.notifyItemRangeRemoved(0,size);

        Cursor res = db.getConversationData();
        while (res.moveToNext()) {
            Conversation conversation = new Conversation(res.getString(0), res.getString(1), res.getString(2));
            conversationList.add(conversation);
        }
        cAdapter.notifyDataSetChanged();
    }

}

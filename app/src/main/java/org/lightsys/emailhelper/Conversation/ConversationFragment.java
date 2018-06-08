package org.lightsys.emailhelper.Conversation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
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
import android.widget.Toast;

import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.DividerItemDecoration;
import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.MessageWindowActivity;
import org.lightsys.emailhelper.NotificationBase;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.RecyclerTouchListener;
import org.lightsys.emailhelper.emailNotification;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConversationFragment extends android.app.Fragment{

    //These variables are used in the list view
    private List<Conversation> conversationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConversationAdapter cAdapter;
    private SwipeRefreshLayout swipeContainer;
    View rootView;
    SharedPreferences sp;
    Resources r;
    DatabaseHelper db;

    public ConversationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        r = getResources();
        sp = getActivity().getApplication().getSharedPreferences(getString(R.string.preferences), 0);
        db = new DatabaseHelper(getActivity().getApplicationContext());
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
        // Lookup the swipe container view
        swipeContainer = rootView.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        //TODO to easily find where the refresh code is.

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh myRefresher = new refresh();
                myRefresher.execute();
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
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.conversation_deleted_prestring)+conversationList.get(itemPosition).getName()+getString(R.string.conversation_deleted_poststring),
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity().getApplicationContext(),getString(R.string.conversation_not_deleted_prestring)+conversationList.get(itemPosition).getName()+getString(R.string.conversation_not_deleted_poststring),
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

        cAdapter = new ConversationAdapter(conversationList);//Adapter for the Conversations

        LinearLayoutManager cLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Conversation conversation = conversationList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), conversation.getName() + " is selected!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity().getBaseContext(), MessageWindowActivity.class);
                intent.putExtra(getString(R.string.intent_email), conversation.getEmail());
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
    private class refresh extends AsyncTask<URL, Integer, Long>{
        Handler handler;
        emailNotification creds;
        @Override
        protected Long doInBackground(URL... urls) {
            handler = new Handler(Looper.getMainLooper());
            GetMail mailer = new GetMail(getActivity().getApplicationContext());
            creds = mailer.getMail();
            return null;
        }
        @Override
        protected void onPostExecute(Long result){
            swipeContainer.setRefreshing(false);//Must be called or refresh circle will continue forever
            if(creds.getInvalid_Credentials()){
                Toast.makeText(getActivity().getApplicationContext(),getString(R.string.invalid_credentials),Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.refresh_finished), Toast.LENGTH_SHORT).show();
            }
            prepareConversationData();
        }
    }
}

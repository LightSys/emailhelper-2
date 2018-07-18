package org.lightsys.emailhelper.Conversation;

import android.content.Intent;
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
import android.widget.Toast;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.ConfirmDialog;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.DividerItemDecoration;
import org.lightsys.emailhelper.GetMail;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.RecyclerTouchListener;
import org.lightsys.emailhelper.emailNotification;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import xdroid.toaster.Toaster;

public class ConversationFragment extends android.app.Fragment{

    //These variables are used in the list view
    private List<Conversation> conversationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    View rootView;
    DatabaseHelper db;

    public ConversationFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setUpSwipeContainer();
        return rootView;
    }

    private void setUpSwipeContainer() {
        swipeContainer = rootView.findViewById(R.id.swipeContainer);// Lookup the swipe container view
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {// Setup refresh listener which triggers new data loading
                refresh myRefresher = new refresh();
                myRefresher.execute();
            }
        });
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
        int deleteRow;
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            Toast.makeText(getActivity().getApplicationContext(), "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            Resources r = getActivity().getApplicationContext().getResources();
            SharedPreferences sp = getActivity().getApplicationContext().getSharedPreferences(getString(R.string.preferences), CommonMethods.SHARED_PREFERENCES_DEFAULT_MODE);
            if(!sp.getBoolean(getString(R.string.key_swipe_deletion),getActivity().getApplicationContext().getResources().getBoolean(R.bool.default_enable_swipe_deletion))){
                Toaster.toast(R.string.swipe_deletion_disabled);
                prepareConversationData();
                return;
            }
            deleteRow = viewHolder.getAdapterPosition();
            String deletionMessage = getString(R.string.conversation_delete_message_prestring)+conversationList.get(deleteRow).getName()+getString(R.string.conversation_delete_message_poststring);
            new ConfirmDialog(deletionMessage,getString(R.string.delete_word),getActivity(),deletionRunnable,cancelRunnable);
        }
        Runnable deletionRunnable = new Runnable() {
            @Override
            public void run() {
                Integer deletedRows = db.deleteConversationData(conversationList.get(deleteRow).getEmail());
                if (deletedRows > 0)
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.conversation_deleted_prestring)+conversationList.get(deleteRow).getName()+getString(R.string.conversation_deleted_poststring), Toast.LENGTH_SHORT).show();
                conversationList.remove(deleteRow);
                prepareConversationData();
            }
        };
        Runnable cancelRunnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(),getString(R.string.conversation_not_deleted_prestring)+conversationList.get(deleteRow).getName()+getString(R.string.conversation_not_deleted_poststring), Toast.LENGTH_SHORT).show();
                prepareConversationData();
            }
        };

    };


    /**
     * Sets up the RecyclerView that holds the conversations.
     */
    public void makeRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);//Makes the RecyclerView
        LinearLayoutManager cLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(cLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Conversation conversation = conversationList.get(position);
                Toast.makeText(getActivity().getApplicationContext(), conversation.getName() + getString(R.string.is_selected), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity().getBaseContext(), ConversationActivity.class);
                intent.putExtra(getString(R.string.intent_email), conversation.getEmail());
                conversation.resetNewMail();
                db.resetNewMailBoolean(conversation.getEmail());
                startActivity(intent);
            }
            @Override public void onLongClick(View view, final int position) {
                String deletionMessage = getString(R.string.conversation_delete_message_prestring)+conversationList.get(position).getName()+getString(R.string.conversation_delete_message_poststring);
                Runnable deletionRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (0 < db.deleteConversationData(conversationList.get(position).getEmail()))
                            Toaster.toast(getString(R.string.conversation_deleted_prestring)+conversationList.get(position).getName()+getString(R.string.conversation_deleted_poststring), Toast.LENGTH_SHORT);
                        conversationList.remove(position);
                        prepareConversationData();
                    }
                };
                Runnable cancelRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Toaster.toast(getString(R.string.conversation_not_deleted_prestring)+conversationList.get(position).getName()+getString(R.string.conversation_not_deleted_poststring));
                        prepareConversationData();
                    }
                };
                new ConfirmDialog(deletionMessage,getString(R.string.delete_word),getActivity(),deletionRunnable,cancelRunnable);
            }

        }));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**********************************************************************************************
     *                  Function to put database items into the conversationList                  *
     **********************************************************************************************/

    public void prepareConversationData() {
        conversationList = db.getConversations();
        ConversationAdapter cAdapter = new ConversationAdapter(conversationList,getActivity().getApplicationContext());
        recyclerView.setAdapter(cAdapter);
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

package com.example.ben.emailhelper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

public class ConversationFragment extends android.app.Fragment {

    //This is an instance of the database for testing
    DatabaseHelper db;

    //These variables are used in the list view
    private List<Conversation> conversationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConversationAdapter cAdapter;
    private SwipeRefreshLayout swipeContainer;
    View rootView;                                                                                  //This variable had to be made global so that the list wouldn't duplicate data

    public ConversationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully
                GetMail mail = new GetMail();
                mail.execute();
                    System.out.println("what's happening");
           }
            });
        // Configue the refreshing colours
        // swipeContainer.setColorSchemeColors(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
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

    private class GetMail extends AsyncTask<URL, Integer, Long> {
        protected void onProgressUpdate() {
        }

        @Override
        protected Long doInBackground(URL... params) {
            System.out.println("Do we even run this");
            fetchTimeLineAsync();
            return null;
        }

        protected void onPostExecute(Long result) {
            swipeContainer.setRefreshing(false);
            cAdapter.notifyDataSetChanged();
        }
    }

    /**********************************************************************************************
     *                  Function to put database items into the conversationList                  *
     **********************************************************************************************/

    public void fetchTimeLineAsync() {
        System.out.println("Does it even run this");
        Cursor res = db.getContactData();
        SearchTerm sender;
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("smtp.googlemail.com", HelperClass._Email, HelperClass._Password);


            Folder inbox = store.getFolder("Inbox");
            UIDFolder uf = (UIDFolder)inbox;
            inbox.open(Folder.READ_ONLY);

            System.out.println("Do we get here?");

            while(res.moveToNext()) {
                System.out.println("How many times does it go?");
                Date today = Calendar.getInstance().getTime();
                sender = new FromTerm(new InternetAddress(res.getString(0)));
                SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, today);
                SearchTerm andTerm = new AndTerm(sender, newerThan);
                Message messages[] = inbox.search(andTerm);
                for (int i = messages.length-1; i >= 0; i--) {
                    Message message = messages[i];
                    String messageID = Long.toString(uf.getUID(message));
                    String body = MainActivity.getTextFromMessage(message);

                    ConversationWindow convo = new ConversationWindow(res.getString(0), null, body, messageID, false);
                    boolean isInserted = db.insertWindowData(res.getString(0), res.getString(0), body, false, messageID);
                    if (isInserted == false)
                        break;
                    // This moves the conversation with the newest email to the top of the list.
                    else {
                        for (Conversation conversation : conversationList) {
                            if (conversation.getEmail().equals(convo.getEmail())) {
                                int index = conversationList.indexOf(conversation);
                                conversationList.remove(index);
                                conversationList.add(0,conversation);
                            }
                        }
                    }
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Messaging Exception.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception.");
        }
    }
}

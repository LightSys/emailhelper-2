package org.lightsys.emailhelper.Conversation;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.DividerItemDecoration;
import org.lightsys.emailhelper.HelperClass;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.SendMail;

import java.util.ArrayList;
import java.util.List;

public class ConversationWindowFragment extends android.app.Fragment {

    DatabaseHelper db;
    private List<ConversationWindow> conversationWindowList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConversationWindowAdapter cAdapter;
    View rootView;
    String passedEmail = ""; //This is the email of the person we are getting messages from


    public ImageButton sendMessageButton;

    public EditText messageSend;
    String persistentMessage;

    public ConversationWindowFragment() {
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
            passedEmail = getArguments().getString(getString(R.string.intent_email));
            rootView = inflater.inflate(R.layout.fragment_conversation_window, container, false);
            makeRecyclerView(rootView);
            sendMessageButton = rootView.findViewById(R.id.sendMessageButton);
            messageSend = rootView.findViewById(R.id.messageEditText);
            //prepareWindowRows();
            sendMessage();
        }
        return rootView;
    }

    /**********************************************************************************************
     *        Has all the steps needed to make the RecyclerView that holds the chat bubbles.      *
     **********************************************************************************************/

    public void makeRecyclerView(View view) {
        db = new DatabaseHelper(getActivity().getApplicationContext());

        recyclerView = (RecyclerView) view.findViewById(R.id.window_recycler_view);

        cAdapter = new ConversationWindowAdapter(conversationWindowList,getActivity().getApplicationContext());

        LinearLayoutManager cLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        cLinearLayoutManager.setStackFromEnd(true);
        RecyclerView.LayoutManager cLayoutManager = cLinearLayoutManager;
        recyclerView.setLayoutManager(cLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);
        // This makes the list scroll to the bottom when the keyboard is displayed
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,int oldRight, int oldBottom)
            {
                recyclerView.smoothScrollToPosition(conversationWindowList.size());
            }
        });
    }

    public void sendMessage() {
        sendMessageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ConversationWindow conversationWindow = new ConversationWindow(HelperClass.Email, null, messageSend.getText().toString(), null, true,false);
                        conversationWindowList.add(conversationWindow);
                        cAdapter.notifyDataSetChanged();
                        persistentMessage = messageSend.getText().toString();
                        // ^ This is so we can clear the EditText field as soon as the button is
                        //pressed and not have to wait until after the Async Task is finished.
                        boolean isInserted = db.insertWindowData(passedEmail, null, persistentMessage, true, null,false);
                        SendMail sendInstance = new SendMail(passedEmail, persistentMessage,getActivity().getApplicationContext());
                        sendInstance.execute();
                        messageSend.getText().clear();
                        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        db.updateConversation(passedEmail, CommonMethods.getCurrentTime());
                    }
                }
        );
    }

    public void prepareWindowRows() {
        Cursor res = db.getWindowData(passedEmail);
        while (res.moveToNext()) {
            boolean sentValue = (res.getInt(5) == 1);
            boolean temp = (1== res.getInt(res.getColumnIndex(db.WINDOW_COL_4)));
            String messageID = res.getString(res.getColumnIndex(db.WINDOW_COL_5));
            ConversationWindow conversationWindow = new ConversationWindow(res.getString(0), res.getString(1), res.getString(2),messageID, sentValue,temp);
            conversationWindowList.add(conversationWindow);
        }
        cAdapter.notifyDataSetChanged();
    }
}
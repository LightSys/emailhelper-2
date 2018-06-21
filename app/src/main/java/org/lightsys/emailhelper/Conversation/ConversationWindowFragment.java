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
    String passedEmail; //This is the email of the person we are getting messages from


    public ImageButton sendMessageButton;

    public EditText messageSend;

    public ConversationWindowFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null) {
            passedEmail = getArguments().getString(getString(R.string.intent_email));
            rootView = inflater.inflate(R.layout.fragment_conversation_window, container, false);
            makeRecyclerView(rootView);
            sendMessageButton = rootView.findViewById(R.id.sendMessageButton);
            messageSend = rootView.findViewById(R.id.messageEditText);
            setUpSendMessageButton();
        }
        db = new DatabaseHelper(getActivity().getApplicationContext());
        return rootView;
    }

    /**
     * Sets up the RecyclerView that holds the messages
     */
    public void makeRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.window_recycler_view);
        LinearLayoutManager cLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        cLinearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(cLinearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // This makes the list scroll to the bottom when the keyboard is displayed
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,int oldRight, int oldBottom)
            {
                recyclerView.smoothScrollToPosition(conversationWindowList.size());
            }
        });
    }

    public void setUpSendMessageButton() {
        sendMessageButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConversationWindow conversationWindow = new ConversationWindow(HelperClass.Email, null, messageSend.getText().toString(), null, true,false);
                    conversationWindowList.add(conversationWindow);
                    cAdapter.notifyDataSetChanged();
                    db.insertWindowData(conversationWindow);

                    SendMail sendInstance = new SendMail(passedEmail, messageSend.getText().toString(),getActivity().getApplicationContext());
                    sendInstance.execute();
                    db.updateConversation(passedEmail, CommonMethods.getCurrentTime());

                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    messageSend.getText().clear();
                }
            }
        );
    }

    public void prepareWindowRows() {
        conversationWindowList = db.getMessages(passedEmail);
        cAdapter = new ConversationWindowAdapter(conversationWindowList,getActivity().getApplicationContext());
        recyclerView.setAdapter(cAdapter);
    }
}
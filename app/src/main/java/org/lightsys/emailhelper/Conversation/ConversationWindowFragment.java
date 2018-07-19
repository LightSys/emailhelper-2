package org.lightsys.emailhelper.Conversation;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.DividerItemDecoration;
import org.lightsys.emailhelper.R;
import org.lightsys.emailhelper.SendMail;

import java.util.ArrayList;
import java.util.List;

public class ConversationWindowFragment extends android.app.Fragment {

    DatabaseHelper db;
    private List<Message> messageList = new ArrayList<>();
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
            setUpTextBox();
        }
        db = new DatabaseHelper(getActivity().getApplicationContext());
        return rootView;
    }
    public void setUpTextBox(){
        messageSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(messageSend.getText().toString().trim().equalsIgnoreCase("")){
                    sendMessageButton.setClickable(false);
                }else{
                    sendMessageButton.setClickable(true);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
                recyclerView.smoothScrollToPosition(messageList.size());
            }
        });
    }

    public void setUpSendMessageButton() {
        sendMessageButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message message = new Message(passedEmail, null,getActivity().getApplicationContext().getResources().getString(R.string.getSubjectLine), messageSend.getText().toString(), null, true,false);
                    messageList.add(message);
                    cAdapter.notifyDataSetChanged();
                    db.insertMessage(message);

                    SendMail sendInstance = new SendMail(passedEmail, messageSend.getText().toString(),getActivity().getApplicationContext());
                    sendInstance.execute();
                    //Update contact time
                    //TODO analyze what happens when update time is updated and prevents an email from coming in.
                    Contact sent = db.getContact(passedEmail);
                    sent.setUpdatedDate(CommonMethods.getCurrentTime());
                    db.updateContact(passedEmail,sent);

                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    messageSend.getText().clear();
                }
            }
        );
        sendMessageButton.setClickable(false);
    }


    public void prepareWindowRows() {
        messageList = db.getMessages(passedEmail);
        cAdapter = new ConversationWindowAdapter(messageList,getActivity().getApplicationContext());
        recyclerView.setAdapter(cAdapter);
    }
}
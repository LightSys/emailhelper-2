package com.example.ben.emailhelper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.*;
import javax.mail.*;
import javax.activation.*;
import javax.mail.internet.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ConversationWindowFragment extends android.app.Fragment {

    DatabaseHelper db;
    private List<ConversationWindow> conversationWindowList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ConversationWindowAdapter cAdapter;
    View rootView;
    String passedEmail = "";                                                                        //This is the email of the person we are getting messages from

    public ImageButton sendMessageButton;

    public EditText messageSend;
    String persistantMessage;

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
            passedEmail = getArguments().getString("email");
            rootView = inflater.inflate(R.layout.fragment_conversation_window, container, false);
            makeRecyclerView(rootView);
            sendMessageButton = (ImageButton) rootView.findViewById(R.id.sendMessageButton);
            messageSend = (EditText) rootView.findViewById(R.id.messageEditText);
            prepareWindowRows();
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

        cAdapter = new ConversationWindowAdapter(conversationWindowList);

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

                        ConversationWindow conversationWindow = new ConversationWindow(HelperClass._Email, null, messageSend.getText().toString(), null, true);
                        conversationWindowList.add(conversationWindow);
                        cAdapter.notifyDataSetChanged();

                        persistantMessage = messageSend.getText().toString();                       //This is so we can clear the EditText field as soon as the button is
                                                                                                    //pressed and not have to wait until after the Async Task is finished.
                        boolean isInserted = db.insertWindowData(passedEmail, null, persistantMessage, true, null);

                        SendNewEmail sendInstance = new SendNewEmail();
                        sendInstance.execute();

                        messageSend.getText().clear();

                        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
        );
    }

    public void prepareWindowRows() {
        Cursor res = db.getWindowData(passedEmail);
        while (res.moveToNext()) {
            boolean sentValue = (res.getInt(5) == 1);
            ConversationWindow conversationWindow = new ConversationWindow(res.getString(0), res.getString(1), res.getString(2), res.getString(4), sentValue);
            conversationWindowList.add(conversationWindow);
            System.out.println(res.getString(2));
        }
        cAdapter.notifyDataSetChanged();
    }

    /**********************************************************************************************
     *              The Async class used to send the email on a different thread.                 *
     **********************************************************************************************/

    private class SendNewEmail extends AsyncTask<URL, Integer, Long> {
        protected void onProgressUpdate() {
        }

        @Override
        protected Long doInBackground(URL... params) {
            sendMailTLS();
            return null;
        }

        protected void onPostExecute(Long result) {
        }
    }

    /**********************************************************************************************
     *  I'm pretty sure that we will need to have a different host and port depending on the type *
     *  of email the person is using. This one works with Gmail. Something to maybe do would be   *
     *  put the different hosts in the HelperClass file and just pull whatever ones are needed    *
     *  for the email provider being used.
     *  -Nick
     **********************************************************************************************/

    public void sendMailTLS() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() { protected PasswordAuthentication getPasswordAuthentication() {return new PasswordAuthentication(HelperClass._Email, HelperClass._Password);}});

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(HelperClass._Email));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(passedEmail));
            message.setSubject("Conversation Email");
            message.setText(persistantMessage);

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

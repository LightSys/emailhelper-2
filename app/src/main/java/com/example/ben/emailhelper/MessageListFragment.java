package com.example.ben.emailhelper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ben.emailhelper.dummy.DummyContent;
import com.example.ben.emailhelper.dummy.DummyContent.DummyItem;

import java.util.List;
import java.util.Properties;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MessageListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private List<Message> messagesList;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessageListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MessageListFragment newInstance(int columnCount) {
        MessageListFragment fragment = new MessageListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        //GetMail getMail = new GetMail();      Started work on something else, but this doesn't work because it calls
        //getMail.execute();                    the value before the ASync task is finished running
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messagelist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                System.out.println("Column count: " + mColumnCount);                     //Debug prints
            }
            recyclerView.setAdapter(new MyMessageListRecyclerViewAdapter(messagesList, mListener));
            // messagesList should be a list of messages made from the network call in GetMail class
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //Async Task because network calls can't be made on the main thread
    public class GetMail extends AsyncTask<Void, Void, Boolean> {
        Message messages[] = null;

        @Override
        protected Boolean doInBackground(Void... params) {
            /*
             *  This was pulled from a YouTube video. Not 100% sure what everything means with the Store and Session,
             *  but JavaMail API has info on it.
             *  https://www.youtube.com/watch?v=glEwFPKid74&t=6s
             *  -Nick
             */
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imap.starttls.enable", "true");
            props.setProperty("mail.imap.ssl.enable", "true");
            try {
                Session session = Session.getDefaultInstance(props, null);
                Store store = session.getStore("imaps");
                store.connect("imap.gmail.com", HelperClass._Email, HelperClass._Password);

                Folder inbox = store.getFolder("Inbox");
                inbox.open(Folder.READ_ONLY);
                messages = inbox.getMessages();

                //messagesList = Arrays.asList(messages);
                mColumnCount = messages.length;          //I don't actually know if column count is what that should be
                System.out.println("Number of messages: " + messages.length);
            } catch (MessagingException e) {
                e.printStackTrace();
                System.out.println("Messaging Exception.");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception.");
            }
            return true;
        }

        /*
         *  Don't know what should normally go in these two function, but I'll keep them here in case
         *  they're important
         *  -Nick
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            messagesList = Arrays.asList(messages);
        }

        @Override
        protected void onCancelled() {
        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        // The To Do is in case we want to change naming standards, so we can easily find it. The argument type is changed
        void onListFragmentInteraction(Message item);
    }

}

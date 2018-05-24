package org.lightsys.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import org.lightsys.emailhelper.Contact.ContactFragment;
import org.lightsys.emailhelper.Conversation.ConversationFragment;

import java.io.IOException;
import java.util.Calendar;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;

public class MainActivity extends AppCompatActivity {

    // TODO: Remove or figure out how to make the DividerItemDecoration work
    // TODO: Add multiple mail services
    // TODO: Polling or push notifications

    DatabaseHelper db;

    ConversationFragment newConversationFragment = new ConversationFragment();
    ContactFragment newContactFragment = new ContactFragment();
    SettingsFragment newSettingsFragment = new SettingsFragment();
    Settings appSettings;

    int newestMessageNumber = 0;

    NotificationCompat.Builder notification;
    private static int uniqueID = 123456;
    //public Settings mySettings;


    public void setFragmentNoBackStack(Fragment frag){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content, frag);
        transaction.commit();
    }

    public void setFragment(Fragment frag) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            TextView overview = findViewById(R.id.overview);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setFragmentNoBackStack(newConversationFragment);
                    overview.setText("Messages");
                    //getSupportActionBar().setTitle("Messages");
                    return true;
                case R.id.navigation_dashboard:
                    setFragmentNoBackStack(newSettingsFragment);
                    overview.setText("Settings");
                    //getSupportActionBar().setTitle("Settings");
                    return true;
                case R.id.navigation_notifications:
                    setFragmentNoBackStack(newContactFragment);
                    overview.setText("Contacts");
                    //getSupportActionBar().setTitle("Contacts");
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start Updater
        Intent updateIntent = new Intent(getBaseContext(), AutoUpdater.class);
        startService(updateIntent);

        //Gathering Credentials
        SharedPreferences sharedPref = getSharedPreferences("myPreferences", 0);
        HelperClass._Email = "richdom2015@gmail.com";
        HelperClass._Password = "***REMOVED***";
        HelperClass.savedCredentials = true;
        //HelperClass._Email = sharedPref.getString("email", "");
        //HelperClass._Password = sharedPref.getString("password", "");
        //HelperClass.savedCredentials = sharedPref.getBoolean("check", false);

        //Gets Credentials if the app doesn't have them
        if (!HelperClass.savedCredentials) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }

        setFragmentNoBackStack(newConversationFragment);
        //This is to set the title the first time the app is launched.
        getSupportActionBar().setTitle("Email Helper");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        db = new DatabaseHelper(getBaseContext());
        appSettings = new Settings();
        //TODO remove hard code testing implementation
        db.insertContactData("donaldrshade@cedarville.edu","Donald","Shade");
        db.insertContactData("donaldrshadejr@gmail.com","John","Doe");
        db.insertConversationData("donaldrshade@cedarville.edu","Donald Shade", CommonMethods.getCurrentTime(), Calendar.getInstance().getTime().toString());
        db.insertConversationData("donaldrshadejr@gmail.com","John Doe", CommonMethods.getCurrentTime(), Calendar.getInstance().getTime().toString());

    }
    @Override
    protected void onStart(){
        super.onStart();

    }
    @Override
    protected void onStop() {
        super.onStop();

    }
    /**********************************************************************************************
     *                  Async class to run get the emails on a different thread                   *
     **********************************************************************************************/

    /*private class GetMailAddresses extends AsyncTask<URL, Integer, Long> {
        protected void onProgressUpdate() {

        }

        @Override
        protected Long doInBackground(URL... params) {

            //getAddresses();
            //com.example.ben.emailhelper.GetMail.getMail(db);

            return null;

        }

        protected void onPostExecute(Long result) {
                //newEmailReceived();
        }
    }*/

    /**********************************************************************************************
     *  TODO: This is to quickly find this comment.                                               *
     *  Currently, the function searches for all emails that were sent after when the             *
     *  conversation was made. This means that it will search for all emails even after the most  *
     *  recently sent ones. It will stop trying to put them into the database when it first finds *
     *  something already exists, but the network time doesn't change at all. There is a          *
     *  MessageNumberTerm search term, but I couldn't figure out how to implement it well.        *
     *  -Nick                                                                                     *
     **********************************************************************************************/
/*
    public void getAddresses() {
        Cursor res = db.getContactData();
        SearchTerm sender;
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getDefaultInstance(props, null);
            IMAPStore store = (IMAPStore) session.getStore("imaps");
            store.connect("smtp.googlemail.com", HelperClass._Email, HelperClass._Password);

            Folder inbox = store.getFolder("Inbox");
            UIDFolder uf = (UIDFolder)inbox;
            inbox.open(Folder.READ_ONLY);

            while(res.moveToNext()) {

                Date today = Calendar.getInstance().getTime();
                sender = new FromTerm(new InternetAddress(res.getString(0)));
                SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.LE, today);
                SearchTerm andTerm = new AndTerm(sender, newerThan);
                Message messages[] = inbox.search(andTerm);

                Stack<ConversationWindow> test = new Stack<>();//The purpose of this stack is to organize more messages into time order.
                for (int i = messages.length-1; i >= 0; i--) {
                    Message message = messages[i];
                    String messageID = Long.toString(uf.getUID(message));
                    String subject = getSubjectFromMessage(message);
                    String body = getTextFromMessage(message);
                    String output = subject + "\n\n" + body;
                    ConversationWindow convo = new ConversationWindow(res.getString(0), null, output, messageID, false);
                    boolean isInserted = db.willInsertWindowData(res.getString(0), res.getString(0), output, false, messageID);
                    if (isInserted == false) {
                        break;

                    }else{
                        test.push(convo);
                    }

                }
                while(!test.isEmpty()){
                    ConversationWindow convo = test.pop();
                    db.insertWindowData(convo.getEmail(),convo.getName(),convo.getMessage(),false,convo.getMessageId());
                    //Puts them into the data base in order
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



    /**********************************************************************************************
     *  Got these function from here:
     *  https://stackoverflow.com/questions/11240368/how-to-read-text-inside-body-of-mail-using-javax-mail
     **********************************************************************************************/

/*    public static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart mimeMultipart = (Multipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }
    public static String getSubjectFromMessage(Message message) throws MessagingException {
        String result = "";
        result = message.getSubject().toString();
        return result;
    }
    /**********************************************************************************************
     *  TODO: This is to quickly find this comment.                                               *
     *  We probably need to mess with this a bit to make extra parts in some emails not show up.  *
     *  When someone replies to the email we send it sends the chain of emails and we don't want  *
     *  that. Probably can apply to other types of emails as well.                                *
     **********************************************************************************************/






}

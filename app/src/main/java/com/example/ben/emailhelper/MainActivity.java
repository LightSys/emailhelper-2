package com.example.ben.emailhelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.URLName;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.MessageNumberTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientTerm;
import javax.mail.search.SearchTerm;

public class MainActivity extends AppCompatActivity {

    // TODO: Remove or figure out how to make the DividerItemDecoration work
    // TODO: Add multiple mail services
    // TODO: Polling or push notifications

    DatabaseHelper db;

    ConversationFragment newConversationFragment = new ConversationFragment();
    ContactFragment newContactFragment = new ContactFragment();
    SettingsFragment newSettingsFragment = new SettingsFragment();

    int newestMessageNumber = 0;

    NotificationCompat.Builder notification;
    private static int uniqueID = 123456;


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

        //Gathering Credentials
        SharedPreferences sharedPref = getSharedPreferences("myPreferences", 0);
        HelperClass._Email = sharedPref.getString("email", "");
        HelperClass._Password = sharedPref.getString("password", "");
        HelperClass.savedCredentials = sharedPref.getBoolean("check", false);

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
        db = new DatabaseHelper(getApplicationContext());
        GetMailAddresses GMA = new GetMailAddresses();
        GMA.execute();

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);
    }

    /**********************************************************************************************
     *                  Async class to run get the emails on a different thread                   *
     **********************************************************************************************/

    private class GetMailAddresses extends AsyncTask<URL, Integer, Long> {
        protected void onProgressUpdate() {
        }

        @Override
        protected Long doInBackground(URL... params) {
            getAddresses();
            return null;
        }

        protected void onPostExecute(Long result) {

        }
    }

    /**********************************************************************************************
     *  TODO: This is to quickly find this comment.                                               *
     *  Currently, the function searches for all emails that were sent after when the             *
     *  conversation was made. This means that it will search for all emails even after the most  *
     *  recently sent ones. It will stop trying to put them into the database when it first finds *
     *  something already exists, but the network time doesn't change at all. There is a          *
     *  MessageNumberTerm search term, but I couldn't figure out how to implement it well.        *
     *  -Nick                                                                                     *
     **********************************************************************************************/

    public void getAddresses() {
        Cursor res = db.getContactData();
        SearchTerm sender;
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            //Session session = Session.getDefaultInstance(props, null);
            //Store store = session.getStore("imaps");
            //store.connect("smtp.googlemail.com", HelperClass._Email, HelperClass._Password);

            Session session = Session.getDefaultInstance(props, null);
            IMAPStore store = (IMAPStore) session.getStore("imaps");
            store.connect("smtp.googlemail.com", HelperClass._Email, HelperClass._Password);


            Folder inbox = store.getFolder("Inbox");
            UIDFolder uf = (UIDFolder)inbox;
            inbox.open(Folder.READ_WRITE);

            /*inbox.addMessageCountListener(new MessageCountListener() {
                @Override
                public void messagesAdded(MessageCountEvent messageCountEvent) {
                    System.out.println("Message Count Event Fired");
                    newEmailReceived();
                }

                @Override
                public void messagesRemoved(MessageCountEvent messageCountEvent) {
                    System.out.println("Message Removed Event Fired");
                }
            });

            inbox.addMessageChangedListener(new MessageChangedListener() {
                @Override
                public void messageChanged(MessageChangedEvent messageChangedEvent) {
                    System.out.println("Message Changed Event Fired");
                }
            });*/

            while(res.moveToNext()) {
                Date today = Calendar.getInstance().getTime();
                sender = new FromTerm(new InternetAddress(res.getString(0)));
                SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, today);
                SearchTerm andTerm = new AndTerm(sender, newerThan);
                Message messages[] = inbox.search(andTerm);
                for (int i = messages.length-1; i >= 0; i--) {
                    Message message = messages[i];
                    String messageID = Long.toString(uf.getUID(message));
                    String body = getTextFromMessage(message);

                    ConversationWindow convo = new ConversationWindow(res.getString(0), null, body, messageID, false);
                    boolean isInserted = db.insertWindowData(res.getString(0), res.getString(0), body, false, messageID);
                    if (isInserted == false)
                        break;
                }
            }

            /*int freq = 2000;
            boolean supportsIdle = false;
            try {
                if (inbox instanceof  IMAPFolder) {
                    IMAPFolder f = (IMAPFolder) inbox;
                    f.idle();
                    supportsIdle = true;
                }
            } catch (FolderClosedException fex) {
                throw fex;
            } catch (MessagingException mex) {
                supportsIdle = false;
            }

            for (; ; ) {
                if (supportsIdle && inbox instanceof IMAPFolder) {
                    IMAPFolder f = (IMAPFolder) inbox;
                    f.idle();
                    System.out.println("IDLE done");
                }
                else {
                    Thread.sleep(freq); // sleep for freq milliseconds

                    // This is to force the IMAP server to send us
                    // EXISTS notifications.
                    inbox.getMessageCount();
                }
            }*/

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

    public static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart mimeMultipart = (Multipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    /**********************************************************************************************
     *  TODO: This is to quickly find this comment.                                               *
     *  We probably need to mess with this a bit to make extra parts in some emails not show up.  *
     *  When someone replies to the email we send it sends the chain of emails and we don't want  *
     *  that. Probably can apply to other types of emails as well.                                *
     **********************************************************************************************/

    public static String getTextFromMimeMultipart(
            Multipart mimeMultipart)  throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }

    /**********************************************************************************************
     *                                  Notification Builder                                      *
     **********************************************************************************************/

    public void newEmailReceived() {
        // Build the notification
        notification.setSmallIcon(R.drawable.ic_home_black_24dp);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Here is the title");
        notification.setContentText("I am the body of your notification");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);
        notification.setPriority(Notification.PRIORITY_HIGH);

        //Build notification and issue it
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());
    }
}

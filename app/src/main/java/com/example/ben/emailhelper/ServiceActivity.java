package com.example.ben.emailhelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.ThemedSpinnerAdapter;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

public class ServiceActivity extends Service {

    private MediaPlayer mp;
    NotificationCompat.Builder notification;
    private static int uniqueID = 123456;

    private MediaPlayer player;

    private String username = HelperClass._Email, password = HelperClass._Password;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("onCreate started.");
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
        builder.setPriority(Notification.PRIORITY_MIN);

        Notification notification = builder
                .setSmallIcon(R.drawable.ic_home_black_24dp)
                .setContentTitle("Email Helper App")
                .setContentText("Checking for new emails...")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }

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

    public void getAddresses() {
        System.out.println("Does getAdresses function run?");
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            //Session session = Session.getDefaultInstance(props, null);
            //Store store = session.getStore("imaps");
            //store.connect("smtp.googlemail.com", HelperClass._Email, HelperClass._Password);

            Session session = Session.getDefaultInstance(props, null);
            IMAPStore store = (IMAPStore) session.getStore("imaps");
            store.connect("smtp.googlemail.com", username, password);


            Folder inbox = store.getFolder("Inbox");
            UIDFolder uf = (UIDFolder)inbox;
            inbox.open(Folder.READ_WRITE);

            inbox.addMessageCountListener(new MessageCountListener() {
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
            });

            int freq = 2000;
            boolean supportsIdle = false;
            try {
                if (inbox instanceof IMAPFolder) {
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
            }

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Messaging Exception.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception.");
        }
    }

    public void newEmailReceived() {
        // Build the notification
        System.out.println("Is this function running?");
        notification.setSmallIcon(R.drawable.ic_home_black_24dp);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Here is the title");
        notification.setContentText("I am the body of your notification");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        //Build notification and issue it
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());
    }
}

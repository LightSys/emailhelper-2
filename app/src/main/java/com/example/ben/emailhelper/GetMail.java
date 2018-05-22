package com.example.ben.emailhelper;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;


import com.sun.mail.imap.IMAPStore;

import java.io.IOException;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;


import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

import static android.content.Context.NOTIFICATION_SERVICE;

public class GetMail {

    public static void execute(AutoUpdate autoUpdate, String action) {
    }

    public void newEmailNotification(Context context){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_home_black_24dp);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle("New Email!");
        builder.setContentText("You have a new message...");
        builder.setAutoCancel(true);
        builder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);

        Intent intent = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setChannelId("EMAIL_HELPER_NOTIFICATIONS");
        //NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //nm.notify();

    }

    public static boolean getMail(DatabaseHelper db) {
        boolean receivedNew = false;
        Cursor res = db.getContactData();
        SearchTerm sender;
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getDefaultInstance(props, null);
            IMAPStore store = (IMAPStore) session.getStore("imaps");
            store.connect("smtp.googlemail.com", HelperClass._Email, HelperClass._Password);
            Folder inbox = store.getFolder("Inbox");
            UIDFolder uf = (UIDFolder) inbox;
            inbox.open(Folder.READ_WRITE);
            while (res.moveToNext()) {
                Date today = Calendar.getInstance().getTime();
                sender = new FromTerm(new InternetAddress(res.getString(0)));
                SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GE,today);//What can we do?
                SearchTerm andTerm = new AndTerm(sender, newerThan);
                Message messages[] = inbox.search(andTerm);
                Stack<ConversationWindow> test = new Stack<>();//The purpose of this stack is to organize more messages into time order.
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    String messageID = Long.toString(uf.getUID(message));
                    String subject = getSubjectFromMessage(message);
                    String body = getTextFromMessage(message);
                    String output = subject + "\n\n" + body;
                    ConversationWindow convo = new ConversationWindow(res.getString(0), null, output, messageID, false);
                    boolean isInserted = db.willInsertWindowData(res.getString(0), res.getString(0), output, false, messageID);
                    if (isInserted == false) {
                        break;
                    } else {
                        test.push(convo);
                        receivedNew = true;
                    }

                }
                while (!test.isEmpty()) {
                    ConversationWindow convo = test.pop();
                    db.insertWindowData(convo.getEmail(), convo.getName(), convo.getMessage(), false, convo.getMessageId());
                    //Puts them into the database in order
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Messaging Exception.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception.");
        }
        return receivedNew;
    }

    public static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart mimeMultipart = (Multipart) message.getContent();
            result = MainActivity.getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    public static String getSubjectFromMessage(Message message) throws MessagingException {
        String result = "";
        result = message.getSubject().toString();
        return result;
    }



}

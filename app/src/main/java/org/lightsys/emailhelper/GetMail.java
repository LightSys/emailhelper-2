package org.lightsys.emailhelper;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.sun.mail.imap.IMAPStore;

import org.lightsys.emailhelper.Conversation.ConversationWindow;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Stack;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

public class GetMail extends AsyncTask<URL, Integer, Long> {
    DatabaseHelper db;
    SharedPreferences sp;
    Resources r;
    public GetMail(DatabaseHelper databaseHelper,SharedPreferences sharedPreferences,Resources resources){
        db = databaseHelper;
        sp = sharedPreferences;
        r = resources;
    }
    protected void onProgressUpdate() {
    }
    @Override
    protected Long doInBackground(URL... params) {
        getMail(sp.getBoolean(r.getString(R.string.key_update_show_messages),r.getBoolean(R.bool.default_update_show_messages)));
        //showMessage can be anything because it is not getting pushed to a notification
        return null;
    }
    protected void onPostExecute(Long result) {

    }
    public emailNotification getMail(boolean showMessage) {
        emailNotification receivedNew = new emailNotification();
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
                //TODO we need to change the today term to the last message recieved for that contact
                SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GE,today);//What can we do?
                SearchTerm andTerm = new AndTerm(sender, newerThan);
                Message messages[] = inbox.search(andTerm);
                Stack<ConversationWindow> test = new Stack<>();//The purpose of this stack is to organize more messages into time order.
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    String messageID = Long.toString(uf.getUID(message));
                    String subject = getSubjectFromMessage(message);
                    String body = getTextFromMessage(message);
                    String output = subject + "\n" + body;
                    ConversationWindow convo = new ConversationWindow(res.getString(0), null, output, messageID, false);
                    boolean isInserted = db.willInsertWindowData(res.getString(0), res.getString(0), output, false, messageID);
                    if (isInserted == false) {
                        break;
                    } else {
                        test.push(convo);
                        if(true){

                        }
                        String Title = "New Message from " + db.getContactName(convo.getEmail());
                        String NotificationMessage = convo.getMessage();
                        if(!showMessage){
                            NotificationMessage = NotificationMessage.substring(0,subject.length());
                        }
                        receivedNew.push(Title,NotificationMessage);
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
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    public static String getSubjectFromMessage(Message message) throws MessagingException {
        String result = "";
        result = message.getSubject().toString();
        return result;
    }
    public static String getTextFromMimeMultipart(Multipart mimeMultipart)  throws MessagingException, IOException {
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
}

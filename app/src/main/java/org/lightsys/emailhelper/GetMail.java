package org.lightsys.emailhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.sun.mail.imap.IMAPStore;

import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.Conversation.Conversation;
import org.lightsys.emailhelper.Conversation.ConversationFragment;
import org.lightsys.emailhelper.Conversation.ConversationWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Stack;

import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

public class GetMail extends AsyncTask<URL, Integer, Long> {
    private DatabaseHelper db;
    SharedPreferences sp;
    Resources r;
    private static Context c;

    public GetMail(Context context){
        c = context;
        db = new DatabaseHelper(context);
        r = c.getResources();
        sp = c.getSharedPreferences(r.getString(R.string.preferences),0);

    }
    @Override
    protected Long doInBackground(URL... params) {
        getMail();
        return null;
    }
    protected void onPostExecute(Long result) {

    }
    public emailNotification getMail() {
        emailNotification receivedNew = new emailNotification();
        Cursor res = db.getContactData();
        SearchTerm sender;
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.put("mail.stmp.auth","true");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.imap.port","993");
        try {
            Session session = Session.getDefaultInstance(props, null);
            IMAPStore store = (IMAPStore) session.getStore("imaps");
            store.connect(HelperClass.incoming, HelperClass.Email, HelperClass.Password);
            Folder inbox = store.getFolder("Inbox");
            UIDFolder uf = (UIDFolder) inbox;
            inbox.open(Folder.READ_WRITE);
            while (res.moveToNext()) {
                String email = res.getString(0);
                String name = db.getContactName(email);
                sender = new FromTerm(new InternetAddress(email));
                Date createdDate = db.getContactDate(email);
                SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GE,createdDate);
                SearchTerm andTerm = new AndTerm(sender, newerThan);
                Message messages[] = inbox.search(andTerm);
                Stack<ConversationWindow> convos = new Stack<>();//The purpose of this stack is to organize more messages into time order.
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    String messageID = Long.toString(uf.getUID(message));
                    if(!db.willInsertWindowData(messageID)){
                        break;
                    }
                    String subject = getSubjectFromMessage(message);
                    String body = getTextFromMessage(message);
                    String output = subject + "\n" + body;
                    String filePath = getAttachment(email,(Multipart) message.getContent());
                    if(filePath != null){
                        output += "\n Attachment Saved to "+filePath+".";
                    }
                    ConversationWindow convo = new ConversationWindow(email, name, output, messageID, false,filePath);
                    convos.push(convo);
                    String Title = r.getString(R.string.notification_title_prestring)+ name +r.getString(R.string.notification_title_poststring);
                    String NotificationMessage = convo.getMessage();
                    boolean showMessage = sp.getBoolean(r.getString(R.string.key_update_show_messages),r.getBoolean(R.bool.default_update_show_messages));
                    if(!showMessage){
                        NotificationMessage = NotificationMessage.substring(0,subject.length());
                    }
                    if(db.getNotificationSettings(email)){
                        receivedNew.push(Title,NotificationMessage);
                    }



                }
                while(!convos.isEmpty()){
                    ConversationWindow convo = convos.pop();
                    db.insertWindowData(convo);
                    db.updateConversation(convo.getEmail(),CommonMethods.getCurrentTime());
                }
            }
        } catch(AuthenticationFailedException e){
            e.printStackTrace();
            System.out.println("Messaging Exception.");
            receivedNew.setInvalid_Credentials();
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Messaging Exception.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception.");
        }
        return receivedNew;
    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart mimeMultipart = (Multipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getSubjectFromMessage(Message message) throws MessagingException {
        String result = "";
        result = message.getSubject();
        return result;
    }
    private static String getTextFromMimeMultipart(Multipart mimeMultipart)  throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append(org.jsoup.Jsoup.parse(html).text());
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }

        }
        return result.toString();
    }
    private static String getAttachment(String email, Multipart mimeMultipart) throws MessagingException, IOException {
        int count = mimeMultipart.getCount();
        String filePath = null;
        for(int i = 0;i<count;i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            String temp = bodyPart.getDisposition();
            if(temp != null) {
                if (temp.equalsIgnoreCase(Part.ATTACHMENT)) {//checks for an attachment
                    c.getDir(email,Context.MODE_PRIVATE);
                    File tempFile = new File(c.getDir(email,Context.MODE_PRIVATE),bodyPart.getFileName());
                    FileOutputStream outputStream = new FileOutputStream(tempFile);
                    InputStream inputStream = bodyPart.getInputStream();
                    byte[] buffer = new byte[4096];
                    int byteRead;
                    while ((byteRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, byteRead);
                    }
                    outputStream.close();
                    filePath = tempFile.getAbsolutePath();
                }
            }
        }
        return filePath;
    }

}

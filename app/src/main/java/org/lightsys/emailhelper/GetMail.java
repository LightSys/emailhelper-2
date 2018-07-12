package org.lightsys.emailhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import com.sun.mail.imap.IMAPStore;

import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.Contact.ContactList;
import org.lightsys.emailhelper.Conversation.ConversationWindow;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
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
    private Context c;

    public GetMail(Context context){
        c = context;
        db = new DatabaseHelper(context);
        r = c.getResources();
        sp = c.getSharedPreferences(r.getString(R.string.preferences),0);

    }
    @Override
    protected Long doInBackground(URL... params) {
        //This does nothing currently
        //may end up putting getMail or getContacts back in here
        return null;
    }
    protected void onPostExecute(Long result) {}
    public emailNotification getMail() {
        emailNotification receivedNew = new emailNotification();
        List<Contact> contactList = db.getListOfContacts();
        Properties props = System.getProperties();
        setProperties(props);
        try {
            Folder inbox = getInbox(props);
            UIDFolder uf = (UIDFolder) inbox;
            inbox.open(Folder.READ_WRITE);
            for(int j = 0;j<contactList.size();j++){
                Contact contact = contactList.get(j);
                String email = contact.getEmail();
                String name = contact.getName();
                SearchTerm searchTerm = getSearchTerm(email);
                Message messages[] = inbox.search(searchTerm);
                Stack<ConversationWindow> convos = new Stack<>();//The purpose of this stack is to organize more messages into time order.
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    Date recieved = message.getReceivedDate();
                    String time = CommonMethods.getTime(recieved);
                    String date = CommonMethods.getDate(recieved);
                    db.updateConversation(email,time,date);
                    String messageID = Long.toString(uf.getUID(message));
                    if(!db.willInsertWindowData(messageID)){
                        break;
                    }
                    ConversationWindow convo = new ConversationWindow(email, name, getMessageContent(message), messageID, false,getAttachments(email,message,uf));
                    convos.push(convo);
                    String Title = r.getString(R.string.notification_title_prestring)+ name +r.getString(R.string.notification_title_poststring);
                    String NotificationMessage = convo.getMessage();
                    if(!sp.getBoolean(r.getString(R.string.key_update_show_messages),r.getBoolean(R.bool.default_update_show_messages))){
                        NotificationMessage = NotificationMessage.substring(0,message.getSubject().length());
                    }
                    if(db.getNotificationSettings(email)){
                        receivedNew.push(Title,NotificationMessage);
                    }
                }
                while(!convos.isEmpty()){
                    ConversationWindow convo = convos.pop();
                    db.insertWindowData(convo);
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
    private String getMessageContent(Message message) throws MessagingException, IOException {
        String subject = message.getSubject();
        String body = getTextFromMessage(message);
        return subject + "\n" + body;
    }
    private SearchTerm getSearchTerm(String email) throws AddressException {
        SearchTerm sender = new FromTerm(new InternetAddress(email));
        Date createdDate = db.getContactDate(email);
        SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GE,createdDate);
        return new AndTerm(sender, newerThan);
    }
    private Folder getInbox(Properties props) throws MessagingException {
        Session session = Session.getDefaultInstance(props, null);
        IMAPStore store = (IMAPStore) session.getStore("imaps");
        store.connect(HelperClass.incoming, HelperClass.Email, HelperClass.Password);
        return store.getFolder("Inbox");
    }
    private void setProperties(Properties properties){
        properties.setProperty("mail.store.protocol", "imaps");
        properties.put("mail.stmp.auth","true");
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.imap.port","993");
    }
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart mimeMultipart = (Multipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }
    private String getTextFromMimeMultipart(Multipart mimeMultipart)  throws MessagingException, IOException {
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
    private boolean getAttachments(String email, Message message,UIDFolder uf) throws MessagingException, IOException {
        boolean hasAttachments = false;
        if(message.isMimeType("multipart/*")){
            Multipart mimeMultipart = (Multipart)message.getContent();
            int count = mimeMultipart.getCount();
            String filePath;
            for(int i = 0;i<count;i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                String temp = bodyPart.getDisposition();
                if (temp != null) {
                    if (temp.equalsIgnoreCase(Part.ATTACHMENT)) {//checks for an attachment
                        c.getDir(email, Context.MODE_PRIVATE);
                        File tempFile = new File(c.getDir(email, Context.MODE_PRIVATE), bodyPart.getFileName());
                        FileOutputStream outputStream = new FileOutputStream(tempFile);
                        InputStream inputStream = bodyPart.getInputStream();
                        byte[] buffer = new byte[4096];
                        int byteRead;
                        while ((byteRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, byteRead);
                        }
                        outputStream.close();
                        filePath = tempFile.getAbsolutePath();
                        DatabaseHelper db = new DatabaseHelper(c);
                        db.insertAttachment(email, filePath,Long.toString(uf.getUID(message)));
                        hasAttachments = true;
                    }
                }
            }
        }
        return hasAttachments;
    }
    public ContactList getContactsFromInbox(){
        ContactList contactList = new ContactList();
        Properties props = System.getProperties();
        setProperties(props);
        try {
            Folder inbox = getInbox(props);
            UIDFolder uf = (UIDFolder) inbox;
            inbox.open(Folder.READ_WRITE);
            Message messages[] = inbox.getMessages();
            Date today = new Date();
            if(today.getMonth() >= 2) {
                today.setMonth(today.getMonth() - 3);
            }else{
                today.setYear(today.getYear()-1);
                today.setMonth(today.getMonth()+9);
            }
            for (int i = messages.length - 1; i >= 0; i--) {
                Message message = messages[i];
                Date sent = message.getSentDate();
                if(sent.before(today)){
                    return contactList;
                }
                Address[] froms = message.getFrom();
                String email = froms == null ? null : ((InternetAddress) froms[0]).getAddress();
                contactList.add(email);
            }
        } catch(AuthenticationFailedException e){
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactList;
    }
}

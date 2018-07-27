package org.lightsys.emailhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;

import com.sun.mail.imap.IMAPStore;

import org.lightsys.emailhelper.Contact.Contact;
import org.lightsys.emailhelper.Contact.ContactList;
import org.lightsys.emailhelper.Conversation.Message;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Folder;
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
import javax.mail.search.DateTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

public class GetMail {
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
    public emailNotification getMail() {
        emailNotification receivedNew = new emailNotification();
        ContactList contactList = db.getContactList();
        Properties props = System.getProperties();
        setProperties(props);
        try {
            Folder inbox = getInbox(props);
            UIDFolder uf = (UIDFolder) inbox;
            inbox.open(Folder.READ_WRITE);
            for(int i = 0;i<contactList.size();i++){
                Contact contact = contactList.get(i);
                SearchTerm searchTerm = getSearchTerm(contact.getEmail());
                javax.mail.Message messages[] = inbox.search(searchTerm);
                for (javax.mail.Message message : messages) {
                    try {//This prevents one email from breaking the bunch.
                        if(db.willInsertMessage(contact.getEmail(),Long.toString(uf.getUID(message)))){
                            //Add new Time Message
                            Message timeHolder = new Message();
                            timeHolder.setEmail(contact.getEmail());
                            timeHolder.setName("");
                            String date = CommonMethods.dateToString(message.getReceivedDate());
                            timeHolder.setSubject(date);
                            timeHolder.setMessage("");
                            timeHolder.setSent(Message.TIME);
                            timeHolder.setHasAttachments(false);
                            timeHolder.setMessageId("");

                            //Push conversation
                            Message conversationWindow = new Message();
                            conversationWindow.setEmail(contact.getEmail());
                            conversationWindow.setName(contact.getName());
                            conversationWindow.setSubject(message.getSubject());
                            conversationWindow.setMessage(getMessageContent(message));
                            conversationWindow.setSent(Message.SENT_BY_OTHER);
                            conversationWindow.setHasAttachments(getAttachments(contact.getEmail(), message, uf));
                            conversationWindow.setMessageId(Long.toString(uf.getUID(message)));

                            //Insert the new messages
                            db.insertMessage(timeHolder);
                            db.insertMessage(conversationWindow);
                            //both are done at the same time just in case there was an error else where

                            //Add notification
                            String title = getNotificationTitle(conversationWindow);
                            String body = getNotifcationBody(conversationWindow, message);
                            if (db.getNotificationSettings(contact.getEmail())) {
                                receivedNew.push(title, body);
                            }

                            //Update contact
                            contact.setUpdatedDate(message.getReceivedDate());
                            db.updateContact(contact.getEmail(), contact);

                            //Update Conversation
                            if(db.hasConversationWith(contact.getEmail())){
                                db.setNewMailBoolean(contact.getEmail());
                            }else{
                                db.insertConversationData(contact,true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch(AuthenticationFailedException e){
            e.printStackTrace();
            System.out.println("Messaging Exception.");
//            receivedNew.setInvalid_Credentials();
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Messaging Exception.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception.");
        }
        return receivedNew;
    }

    private String getNotifcationBody(Message conversationWindow, javax.mail.Message email) throws MessagingException {
        String message = conversationWindow.getSubject()+"\n"+conversationWindow.getMessage();
        boolean showMessages = sp.getBoolean(r.getString(R.string.key_update_show_messages),r.getBoolean(R.bool.default_update_show_messages));
        if(!showMessages){
            message = message.substring(0,conversationWindow.getSubject().length());
        }else{
            int lengthToSet = message.length();
            int maxSize = 200;
            if(lengthToSet>maxSize){
                lengthToSet = maxSize;
                for(int i = maxSize-1;i>50;i--){
                    if(message.charAt(i)==' '){
                        message = message.substring(0,i);
                        i = 49;
                    }
                }
            }
        }

        return message;
    }

    private String getNotificationTitle(Message conversationWindow) {
        String message = r.getString(R.string.notification_title_prestring);
        message += conversationWindow.getName();
        message += r.getString(R.string.notification_title_poststring);
        return message;
    }

    private SearchTerm getSearchTerm(String email) throws AddressException {
        SearchTerm sender = new FromTerm(new InternetAddress(email));
        Date updatedDate = db.getContactUpdatedDate(email);
        SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GE,updatedDate);
        return new AndTerm(sender, newerThan);
    }
    private String getMessageContent(javax.mail.Message message) throws MessagingException, IOException {
        return getTextFromMessage(message);
    }
    private Folder getInbox(Properties props) throws MessagingException {
        Session session = Session.getDefaultInstance(props, null);
        IMAPStore store = (IMAPStore) session.getStore("imaps");
        store.connect(AuthenticationClass.incoming, AuthenticationClass.Email, AuthenticationClass.Password);
        return store.getFolder("Inbox");
    }
    private void setProperties(Properties properties){
        properties.setProperty("mail.store.protocol", "imaps");
        properties.put("mail.stmp.auth","true");
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.imap.port","993");
    }
    private String getTextFromMessage(javax.mail.Message message) throws MessagingException, IOException {
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
    private boolean getAttachments(String email, javax.mail.Message message, UIDFolder uf) throws MessagingException, IOException {
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
                        File sharedFiles = new File(c.getFilesDir(),"sharedFiles");
                        File subDirectory = new File(sharedFiles,email);
                        subDirectory.mkdirs();
                        File tempFile = new File(subDirectory, bodyPart.getFileName());
                        if(!db.hasAttachment(email,tempFile.getAbsolutePath(),Long.toString(uf.getUID(message)))) {
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
                            db.insertAttachment(email, filePath, Long.toString(uf.getUID(message)));
                        }
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
            Calendar checker = Calendar.getInstance();
            if(checker.get(Calendar.MONTH) >= 2) {
                checker.set(Calendar.MONTH,checker.get(Calendar.MONTH)-3);
            }else{
                checker.set(Calendar.YEAR,checker.get(Calendar.YEAR)-1);
                checker.set(Calendar.MONTH,checker.get(Calendar.MONTH)+9);
            }
            Date lastDate = checker.getTime();
            SearchTerm after = new ReceivedDateTerm(ComparisonTerm.GE,lastDate);
            javax.mail.Message messages[] = inbox.search(after);
            for (int i = messages.length - 1; i >= 0; i--) {
                javax.mail.Message message = messages[i];
                String fullAddress = message.getFrom()[0].toString();
                if(fullAddress.contains("<") && fullAddress.contains(">")){
                    String name = fullAddress.substring(0,fullAddress.indexOf("<")).trim();
                    String email = fullAddress.substring(fullAddress.indexOf("<")+1,fullAddress.length()-1);
                    contactList.add(name,email);
                }
                else{
                    contactList.add("",fullAddress);
                }

            }
        } catch(AuthenticationFailedException e){
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactList;
    }
}

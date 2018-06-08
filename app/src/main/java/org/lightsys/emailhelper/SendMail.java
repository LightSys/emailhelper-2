package org.lightsys.emailhelper;

import android.content.Context;
import android.os.AsyncTask;

import java.net.URL;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import xdroid.toaster.Toaster;

/**********************************************************************************************
 *              The Async class used to send the email on a different thread.                 *
 **********************************************************************************************/

public class SendMail extends AsyncTask<URL, Integer, Long> {
    private String passedEmail;
    private String persistantMessage;
    Context c;

    public SendMail(String recipient, String message,Context context){
        passedEmail = recipient;
        persistantMessage = message;
        c = context;
    }
    protected void onProgressUpdate() {
    }
    @Override
    protected Long doInBackground(URL... params) {
        sendMailTLS();
        return null;
    }
    protected void onPostExecute(Long result) {
    }


    /**********************************************************************************************
     *  I'm pretty sure that we will need to have a different host and port depending on the type *
     *  of email the person is using. This one works with Gmail. Something to maybe do would be   *
     *  put the different hosts in the HelperClass file and just pull whatever ones are needed    *
     *  for the email provider being used.
     *  -Nick
     **********************************************************************************************/
    public void sendMailTLS(){
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HelperClass.outgoing);
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props, new javax.mail.Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(HelperClass.Email, HelperClass.Password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(HelperClass.Email));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(passedEmail));
            message.setSubject(c.getResources().getString(R.string.getSubjectLine));

            message.setText(persistantMessage);
            Transport transport = session.getTransport("smtp");
            transport.send(message);
        }
        catch(AuthenticationFailedException e){
            e.printStackTrace();
            System.out.println("Messaging Exception.");
            Toaster.toastLong(R.string.invalid_credentials_message);
        }catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

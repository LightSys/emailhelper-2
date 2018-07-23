package org.lightsys.emailhelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ExpandableListActivity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.MailConnectException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import xdroid.toaster.Toaster;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * THIS DOESN'T DO ANYTHING RIGHT NOW. USE IT TO GET THE SIGN IN INFORMATION THE FIRST TIME THE USER SIGNS IN.
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CheckBox enableAdvancedLogin;
    private EditText mIMAPview;
    private EditText mSTMPview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = findViewById(R.id.password);

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mIMAPview = findViewById(R.id.incoming);
        mSTMPview = findViewById(R.id.outgoing);
        final LinearLayout.LayoutParams shrink = new LinearLayout.LayoutParams(0,0);
        final LinearLayout.LayoutParams expand = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mIMAPview.setLayoutParams(shrink);
        mSTMPview.setLayoutParams(shrink);
        enableAdvancedLogin = findViewById(R.id.enableAdvanced);
        enableAdvancedLogin.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mIMAPview.setLayoutParams(expand);
                    mSTMPview.setLayoutParams(expand);
                } else {
                    mIMAPview.setLayoutParams(shrink);
                    mSTMPview.setLayoutParams(shrink);
                }
            }
        });
        enableAdvancedLogin.setChecked(false);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }
    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String imap = mIMAPview.getText().toString();
        String stmp = mSTMPview.getText().toString();

        // Save the 'global' variables of HelperClass
        if(enableAdvancedLogin.isChecked()){
            AuthenticationClass.Email = email;
            AuthenticationClass.Password = password;
            AuthenticationClass.incoming = imap;
            AuthenticationClass.outgoing = stmp;
        }else{
            AuthenticationClass.setEmail(email);
            AuthenticationClass.Password = password;
        }




        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        mAuthTask = new UserLoginTask();
        mAuthTask.execute();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {
        private final int unknownError = -1;
        private final int success = 0;
        private final int emailInvalid = 1;
        private final int credentialsInvalid = 2;
        private final int advancedLoginIssue = 3;
        private final int invalidSTMP = 4;
        private final int invalidIMAP = 5;

        private final String mEmail;
        private final String mPassword;

        UserLoginTask() {
            mEmail = AuthenticationClass.Email;
            mPassword = AuthenticationClass.Password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if(enableAdvancedLogin.isChecked()){
                try{
                    Properties props = System.getProperties();
                    //props.setProperty("mail.store.protocol", "imaps");
                    props.setProperty("mail.store.protocol", "imaps");
                    props.put("mail.stmp.auth","true");
                    props.put("mail.smtp.starttls.enable","true");
                    props.put("mail.imap.port","993");
                    Session session = Session.getDefaultInstance(props, null);
                    IMAPStore store = (IMAPStore) session.getStore("imaps");
                    store.connect(AuthenticationClass.incoming, mEmail, mPassword);
                    Folder inbox = store.getFolder("Inbox");
                    UIDFolder uf = (UIDFolder) inbox;
                    inbox.open(Folder.READ_WRITE);
                }catch(AuthenticationFailedException e){
                    return credentialsInvalid;
                }catch(MailConnectException e){
                    return invalidIMAP;
                }catch(MessagingException e){
                    return unknownError;
                }catch(Exception e){
                    e.printStackTrace();
                    return unknownError;
                }
                if(AuthenticationClass.outgoing.equalsIgnoreCase("") || !AuthenticationClass.outgoing.contains(".")){
                    return invalidSTMP;
                }
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", AuthenticationClass.outgoing);
                    props.put("mail.smtp.port", "587");
                    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(AuthenticationClass.Email, AuthenticationClass.Password);
                        }
                    });
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(AuthenticationClass.Email));
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress("john.doe@example.com"));
                    //^May cause a bounced email.
                    Transport transport = session.getTransport("smtp");
                    transport.send(message);

                }
                catch (MailConnectException e) {
                    e.printStackTrace();
                    return invalidSTMP;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{
                if(!CommonMethods.checkEmail(mEmail)){
                    return emailInvalid;
                }
                try{
                    Properties props = System.getProperties();
                    //props.setProperty("mail.store.protocol", "imaps");
                    props.setProperty("mail.store.protocol", "imaps");
                    props.put("mail.stmp.auth","true");
                    props.put("mail.smtp.starttls.enable","true");
                    props.put("mail.imap.port","993");
                    Session session = Session.getDefaultInstance(props, null);
                    IMAPStore store = (IMAPStore) session.getStore("imaps");
                    store.connect(AuthenticationClass.incoming, mEmail, mPassword);
                    Folder inbox = store.getFolder("Inbox");
                    UIDFolder uf = (UIDFolder) inbox;
                    inbox.open(Folder.READ_WRITE);
                }catch(AuthenticationFailedException e){
                    return credentialsInvalid;

                }catch(MessagingException e){
                    return unknownError;
                }
            }
            Toaster.toastLong(R.string.valid_credentials);
            return success;

        }

        @Override
        protected void onPostExecute(final Integer result) {
            mAuthTask = null;
            showProgress(false);

            AuthenticationClass.savedCredentials = result == success;
            switch(result) {
                case invalidIMAP:
                    mIMAPview.setError(getString(R.string.error_invalid_imap));
                    mIMAPview.requestFocus();
                    break;
                case invalidSTMP:
                    mSTMPview.setError(getString(R.string.error_invalid_stmp));
                    mSTMPview.requestFocus();
                    break;
                case advancedLoginIssue:
                    mEmailView.setError(getString(R.string.error_check_stuff));
                    mEmailView.requestFocus();
                    break;
                case emailInvalid:
                    mEmailView.setError(getString(R.string.error_invalid_email));
                    mEmailView.requestFocus();
                    break;
                case credentialsInvalid:
                    if (mEmail.contains("@gmail.com")) {
                        mEmailView.setError(getString(R.string.OAuthNonCompat));
                        mEmailView.requestFocus();
                    }
                case unknownError:
                    if(!mEmail.contains("@gmail.com")){//This is basically and else for the above.
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                    break;
                case success:
                    updateSharedPreferences();
                    finish();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
    private void updateSharedPreferences(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences), 0);// Create object of SharedPreferences
        SharedPreferences.Editor editor = sharedPref.edit();// New get Editor
        editor.putString(getResources().getString(R.string.key_email), AuthenticationClass.Email);// Put your values
        editor.putString(getResources().getString(R.string.key_password), AuthenticationClass.Password);
        editor.putString(getString(R.string.key_imap),AuthenticationClass.incoming);
        editor.putString(getString(R.string.key_smtp),AuthenticationClass.outgoing);
        editor.putBoolean(getResources().getString(R.string.key_valid_credentials), AuthenticationClass.savedCredentials);
        editor.apply();// Apply your edits
    }
}


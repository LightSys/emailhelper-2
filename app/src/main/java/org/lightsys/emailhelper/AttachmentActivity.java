package org.lightsys.emailhelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import org.lightsys.emailhelper.Conversation.Conversation;
import org.lightsys.emailhelper.Conversation.ConversationActivity;
import org.lightsys.emailhelper.Conversation.ConversationAdapter;
import org.lightsys.emailhelper.Conversation.ConversationFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.zip.Inflater;

import javax.activation.MimeType;

import xdroid.toaster.Toaster;

public class AttachmentActivity extends AppCompatActivity {
    RecyclerView attachments;
    private AttachmentAdapter adapter;
    String email;
    SwipeRefreshLayout swipeContainer;

    Context activityContext = this;
    String attachment;
    int deleteRow;
    File attachmentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(getString(R.string.attachments));
        email = getIntent().getStringExtra(getString(R.string.intent_email));
        attachments = (RecyclerView) findViewById(R.id.recycler_view_attachments);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        attachments.setLayoutManager(layoutManager);
        attachments.setHasFixedSize(false);
        swipeContainer = findViewById(R.id.swipeDelete);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(false);
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(attachments);
        attachments.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), attachments, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String attach = adapter.attachments.get(position);
                File path = getDir(adapter.email, Context.MODE_PRIVATE);
                File file = new File(path, attach);
                try{
                    openFile(file);
                }catch(Exception e){

                }

            }
            @Override
            public void onLongClick(View view, int position) {}
        }));
    }
    //TODO test this function and see if it will open the files now
    private void openFile(File file) throws IOException {
        File sdCardFile = new File("/data/data/org.lightsys.emailhelper/app_"+email+"/"+file.getName());
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try{
            inputStream = new FileInputStream(file);
            outputStream = new FileOutputStream(sdCardFile);
            byte[] buffer = new byte[4096];
            int read;
            while((read = inputStream.read(buffer))>0){
                outputStream.write(buffer,0,read);
            }
        }finally{
            inputStream.close();
            outputStream.close();
        }
        String filePath = sdCardFile.getAbsolutePath();
        if(filePath.contains(".png")||filePath.contains(".jpg")||filePath.contains(".jpeg")){
            Intent intent = new Intent(getApplicationContext(),ImageActivity.class);
            intent.putExtra("file_path",filePath);
            startActivity(intent);
        }
        /*else{
            String ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
            if(ext == "" && filePath.contains(".pdf")){
                ext = "pdf";
            }
            String mimeType = null;
            if(ext != null){
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(filePath));
            intent.setType(mimeType);
            if(mimeType == null){
                Toaster.toast("EmailHelper does not recognize the attachment");
            }else{
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);PackageManager manager = getPackageManager();
                List<ResolveInfo> capableApps = manager.queryIntentActivities(intent,0);
                if(capableApps.size() > 0){
                    startActivity(intent);
                }else{
                    Toaster.toast("No apps found capable to open selected attachment");
                }
            }
        }*/
        else{
            Uri uri = Uri.parse(filePath);
            String ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
            if(ext == "" && filePath.contains(".pdf")){
                ext = "pdf";
            }
            String mime = null;
            if(ext != null){
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri,mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PackageManager manager = getPackageManager();
            List<ResolveInfo> capableApps = manager.queryIntentActivities(intent,0);
            if(capableApps.size() > 0){
                startActivity(intent);
            }else{
                Toaster.toast("No apps found capable to open selected attachment");
            }
        }
    }

    @Override
    public void onResume() {
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        adapter = new AttachmentAdapter(email,db);
        attachments.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        super.onResume();
    }
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        /**
         * onMove probably doesn't need to be used by us, but you need it for the ItemTouchHelper
         * to be happy.
         * -Nick
         */
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            deleteRow = viewHolder.getAdapterPosition();
            String attachmentName = adapter.attachments.get(deleteRow);
            //Need to delete it from DB before getting rid of it from the list
            attachment = "/data/user/0/org.lightsys.emailhelper/app_"+email+"/"+attachmentName;
            attachmentFile = new File(attachment);


            //Remove swiped item from list and notify the RecyclerView
            AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
            builder.setMessage("Are you sure that you want to delete "+attachmentName+"?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.deleteAttachment(email, attachment);
                    adapter.attachments.remove(deleteRow);
                    adapter = new AttachmentAdapter(email,db);
                    attachments.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    attachmentFile.delete();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    adapter = new AttachmentAdapter(email,db);
                    attachments.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            });
            builder.create().show();
        }
    };
}

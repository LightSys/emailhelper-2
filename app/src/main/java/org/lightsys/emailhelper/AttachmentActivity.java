package org.lightsys.emailhelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import xdroid.toaster.Toaster;

public class AttachmentActivity extends AppCompatActivity {
    RecyclerView attachments;
    private AttachmentAdapter adapter;
    String email;
    SwipeRefreshLayout swipeContainer;
    DatabaseHelper db;
    Context activityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(getString(R.string.attachments));

        activityContext = getApplicationContext();
        db = new DatabaseHelper(getApplicationContext());
        email = getIntent().getStringExtra(getString(R.string.intent_email));

        makeRecyclerView();
        makeSwipeContainer();
    }
    private void makeRecyclerView(){
        attachments = findViewById(R.id.recycler_view_attachments);
        attachments.setLayoutManager(new LinearLayoutManager(this));
        attachments.setHasFixedSize(false);
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
                    int temp = 0;
                }

            }
            @Override
            public void onLongClick(View view, int position) {}
        }));
    }
    private void makeSwipeContainer(){
        swipeContainer = findViewById(R.id.swipeDelete);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {// Setup refresh listener which triggers new data loading
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void openFile(File file) throws IOException {
        String filePath = file.getAbsolutePath();
        Uri uri = Uri.parse(filePath);
        String ext = getExtension(filePath);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        Intent intent = new Intent();
        if(mime.contains("image/")){
            intent = new Intent(getApplicationContext(),ImageActivity.class);
            intent.putExtra("file_path",filePath);
            startActivity(intent);
        }else{
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
            uri = Uri.parse(sdCardFile.getAbsolutePath());
            if(ext.equals("") && filePath.contains(".pdf")){
                ext = "pdf";
            }
            if(ext != null){
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            }
            intent.setDataAndType(uri,mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PackageManager manager = getPackageManager();
            List<ResolveInfo> capableApps = manager.queryIntentActivities(intent,0);
            if(capableApps.size() > 0){
                startActivity(intent);
            }else{
                Toaster.toast(getString(R.string.cannot_find_app_to_run));
            }
        }
    }
    private String getExtension(String filePath) {
        int temp = filePath.length()-1;
        for(int i = temp;i>0;i--){
            if(filePath.charAt(i)=='.'){
                temp = i;
                i = 0;
            }
        }
        filePath = filePath.substring(temp+1);
        return filePath;
    }

    @Override
    public void onResume() {
        prepareAttachmentData();
        super.onResume();
    }
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        int deleteRow;
        String attachmentFilePath;
        File attachmentFile;
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
            File path = getDir(email,MODE_PRIVATE);
            attachmentFile = new File(path,attachmentName);
            attachmentFilePath = attachmentFile.getAbsolutePath();
            String deletionMessage =getString(R.string.attachment_message_prestring)+attachmentName+getString(R.string.attachment_message_poststring);
            new ConfirmDialog(deletionMessage,getString(R.string.delete_word),activityContext,deletionRunnable,cancelRunnable);
        }
        Runnable deletionRunnable = new Runnable() {
            @Override
            public void run() {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                db.deleteAttachment(email, attachmentFilePath);
                adapter.attachments.remove(deleteRow);
                attachmentFile.delete();
                prepareAttachmentData();
                Toaster.toast(R.string.attachment_deleted_message);
            }
        };
        Runnable cancelRunnable = new Runnable() {
            @Override
            public void run() {
                prepareAttachmentData();
                Toaster.toast(R.string.attachment_not_deleted_message);
            }
        };
    };

    public void prepareAttachmentData(){
        adapter = new AttachmentAdapter(email,db);
        attachments.setAdapter(adapter);
    }
}

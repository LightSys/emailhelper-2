package org.lightsys.emailhelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private boolean longClickDisable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(getString(R.string.attachments));
        longClickDisable = false;
        activityContext = this;
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
                File path = new File(activityContext.getFilesDir(),"sharedFiles");
                path = new File(path,adapter.email);
                File file = new File(path, attach);
                try{
                    openFile(file);
                }catch(SecurityException se){
                    int temp = 0;
                }catch(Exception e){
                    int temp = 0;
                }
            }
            @Override
            public void onLongClick(View view, final int position) {
                if(longClickDisable){//Why?
                    longClickDisable = false;
                    return;
                    //This section is to prevent a long click action after swipe deletion when swipe
                    //deletion is disabled. I'm not sure why long click is called then. It doesn't
                    // happen in any other view.
                }
                File path = getDir(email,MODE_PRIVATE);
                final File attachmentFile = new File(path,adapter.attachments.get(position));
                String attachmentName = attachmentFile.getName();
                Runnable deletionRunnable = new Runnable() {
                    @Override
                    public void run() {
                        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                        db.deleteAttachment(email, adapter.attachments.get(position));
                        adapter.attachments.remove(position);
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
                String deletionMessage ="Long Click" + getString(R.string.attachment_message_prestring)+attachmentName+getString(R.string.attachment_message_poststring);
                new ConfirmDialog(deletionMessage,getString(R.string.delete_word),activityContext,deletionRunnable,cancelRunnable);
            }
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
        Uri fileUri;
        String ext = CommonMethods.getExtension(filePath);
        ext.toLowerCase();
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        Intent intent = new Intent();
        if(mime.contains("image/")){
            intent = new Intent(getApplicationContext(),ImageActivity.class);
            intent.putExtra("file_path",filePath);
            startActivity(intent);
        }else{
            fileUri = Uri.fromFile(file);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri,mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PackageManager manager = getPackageManager();
            List<ResolveInfo> capableApps = manager.queryIntentActivities(intent,0);
            if(capableApps.size() > 0){
                startActivity(intent);
            }else{
                Toaster.toast(getString(R.string.cannot_find_app_to_run));
            }
        }
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
            SharedPreferences sp = getSharedPreferences(getString(R.string.preferences),CommonMethods.SHARED_PREFERENCES_DEFAULT_MODE);
            Resources r = getApplicationContext().getResources();
            if(!sp.getBoolean(getString(R.string.key_swipe_deletion),getApplicationContext().getResources().getBoolean(R.bool.default_enable_swipe_deletion))){
                Toaster.toast(R.string.swipe_deletion_disabled);
                prepareAttachmentData();
                longClickDisable = true;
            }else{
                deleteRow = viewHolder.getAdapterPosition();
                String attachmentName = adapter.attachments.get(deleteRow);
                //Need to delete it from DB before getting rid of it from the list
                File path = getDir(email,MODE_PRIVATE);
                attachmentFile = new File(path,attachmentName);
                attachmentFilePath = attachmentFile.getAbsolutePath();
                String deletionMessage =getString(R.string.attachment_message_prestring)+attachmentName+getString(R.string.attachment_message_poststring);
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
                new ConfirmDialog(deletionMessage,getString(R.string.delete_word),activityContext,deletionRunnable,cancelRunnable);
            }

        }

    };

    public void prepareAttachmentData(){
        adapter = new AttachmentAdapter(email,db);
        attachments.setAdapter(adapter);
    }
    public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder> {
        DatabaseHelper db;
        String email;
        List<String> attachments;
        int numItems;

        public AttachmentAdapter(String email, DatabaseHelper db){
            this.db = db;
            this.email = email;
            attachments = db.getAttachments(email);
            for(int i = 0;i<attachments.size();i++){
                String temp = attachments.remove(i);
                temp = temp.substring(temp.indexOf(email));
                temp = temp.substring(temp.indexOf("/")+1);
                attachments.add(i,temp);
            }
            numItems = attachments.size();

        }

        @NonNull
        @Override
        public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            Context context = viewGroup.getContext();
            db = new DatabaseHelper(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.item_attachment,viewGroup,false);
            AttachmentViewHolder viewHolder = new AttachmentViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
            String text = attachments.get(position);
            holder.attachmentName.setText(text);

        }

        @Override
        public int getItemCount() {
            return numItems;
        }



        class AttachmentViewHolder extends RecyclerView.ViewHolder {
            TextView attachmentName;
            public AttachmentViewHolder(View itemView) {
                super(itemView);
                attachmentName = itemView.findViewById(R.id.attachment_title);
            }
        }

    }
}

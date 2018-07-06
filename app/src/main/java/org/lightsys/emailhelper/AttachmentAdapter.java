package org.lightsys.emailhelper;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

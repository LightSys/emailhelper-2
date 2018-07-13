package org.lightsys.emailhelper.Conversation;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.ImageActivity;
import org.lightsys.emailhelper.R;

import java.util.ArrayList;
import java.util.List;

public class ConversationAttachmentAdapter extends RecyclerView.Adapter<ConversationAttachmentAdapter.ImageViewHolder> {
    private int numItems;
    List<String> attachments;


    public ConversationAttachmentAdapter(String messageID,DatabaseHelper db){
        attachments = new ArrayList<>();
        attachments = db.getAttachmentsforConvo(messageID);
        for(int i = attachments.size()-1;i>=0;i--){
            String temp = attachments.get(i);
            if(temp.contains(".txt")){
                attachments.remove(i);
            }
        }
        numItems = attachments.size();
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layout = R.layout.item_attachment_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layout,parent,false);
        return new ImageViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(attachments.get(position));
    }

    @Override
    public int getItemCount(){
        return numItems;
    }
    class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView attachmentImage;
        String filePath;

        public ImageViewHolder(View itemView, final Context context) {
            super(itemView);
            attachmentImage = itemView.findViewById(R.id.attachment_view_image);
            attachmentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra("file_path",filePath);
                    context.startActivity(intent);
                }
            });
        }
        public void bind(String filePath){
            attachmentImage.setImageBitmap(BitmapFactory.decodeFile(filePath));
            this.filePath = filePath;
        }
    }
}

package org.lightsys.emailhelper.Conversation;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.ImageActivity;
import org.lightsys.emailhelper.R;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the adapter for the attachments in the messages to be displayed.
 * Created by DSHADE Summer 18.
 */
public class ConversationAttachmentAdapter extends RecyclerView.Adapter<ConversationAttachmentAdapter.ImageViewHolder> {
    private int numItems;
    private List<String> attachments;

    /**
     * This constructor takes in the messageID and a link to the database to get the attachments for
     * the given message.
     */
    ConversationAttachmentAdapter(String messageID,DatabaseHelper db){
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

    /**
     * Inner class for the Adapter
     */
    class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView attachmentImage;
        String filePath;
        TextView attachmentName;

        /**
         * This constuctor connects the view to class variables so they can be set later.
         */
        ImageViewHolder(View itemView, final Context context) {
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
            attachmentName = itemView.findViewById(R.id.attachment_name);
        }

        /**
         * This bind method is called from onBindViewHolder in order that the mess is cleaned.
         */
        void bind(String filePath){
            LinearLayout.LayoutParams shrunk = new LinearLayout.LayoutParams(0,0);
            LinearLayout.LayoutParams expand = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            this.filePath = filePath;
            String output = filePath.substring(filePath.indexOf("@"));
            output = output.substring(output.indexOf("/")+1);
            attachmentName.setText(output);

            String ext = CommonMethods.getExtension(filePath);
            ext.toLowerCase();
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            if(!mime.contains("image")){
                attachmentImage.setLayoutParams(shrunk);
            }else{
                attachmentImage.setImageBitmap(BitmapFactory.decodeFile(filePath));
                attachmentImage.setLayoutParams(expand);
            }
        }
    }
}

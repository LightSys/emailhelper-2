package org.lightsys.emailhelper.Conversation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.vision.text.Line;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

import java.util.List;

/**
 * This adapter is for going between the ConversationWindow Fragment and the list of messages for that
 * contact in the database.
 * Created by nicholasweg on 7/10/17.
 * Edit by DSHADE Summer 2018.
 */

public class ConversationWindowAdapter extends RecyclerView.Adapter<ConversationWindowAdapter.MyViewHolder> {

    private List<Message> messageList;
    Context context;
    LinearLayout.LayoutParams original;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView subjectOfMessage;
        public TextView bodyOfMessage;
        public LinearLayout parent_layout;
        public LinearLayout gravity;

        public RecyclerView recyclerView;
        RelativeLayout wrapper;


        public MyViewHolder(View view) {
            super(view);
            subjectOfMessage = view.findViewById(R.id.subject);
            bodyOfMessage = view.findViewById(R.id.body);
            parent_layout = view.findViewById(R.id.bubble_layout);
            gravity = view.findViewById(R.id.chat_bubble_parent);
            recyclerView = view.findViewById(R.id.attachment_view);
            wrapper = view.findViewById(R.id.wrapper);
            original = (LinearLayout.LayoutParams) wrapper.getLayoutParams();

        }
    }

    public ConversationWindowAdapter(List<Message> messageList, Context c) {
        this.messageList = messageList;
        context = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_window, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Message message = messageList.get(position);

        holder.subjectOfMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent messageActivity = new Intent(context,MessageActivity.class);
                messageActivity.putExtra(context.getResources().getString(R.string.intent_message_id),message.getMessageId());
                context.startActivity(messageActivity);
            }
        });
        holder.bodyOfMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent messageActivity = new Intent(context,MessageActivity.class);
                messageActivity.putExtra(context.getResources().getString(R.string.intent_message_id),message.getMessageId());
                context.startActivity(messageActivity);
            }
        });

        Resources r = context.getResources();
        LinearLayout.LayoutParams size;
        int dpAsPixels = (int) (r.getDisplayMetrics().density);
        int spaceFromSide = 5;//This is the space between the message bubble and side
        switch(message.getSent()){
            case Message.SENT_BY_ME:
                holder.subjectOfMessage.setText(message.getSubject().trim());
                holder.subjectOfMessage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
                holder.bodyOfMessage.setText(message.getMessage());
                holder.bodyOfMessage.setClickable(true);
                holder.gravity.setGravity(Gravity.END);
                holder.parent_layout.setBackground(context.getResources().getDrawable(R.drawable.border));
                holder.gravity.setPadding(spaceFromSide*dpAsPixels,0,spaceFromSide*dpAsPixels,3*dpAsPixels);
                break;
            case Message.SENT_BY_OTHER:
                holder.subjectOfMessage.setText(message.getSubject().trim());
                holder.subjectOfMessage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
                holder.bodyOfMessage.setText(message.getMessage());
                holder.bodyOfMessage.setClickable(true);
                holder.gravity.setGravity(Gravity.START);
                holder.parent_layout.setBackground(context.getResources().getDrawable(R.drawable.border2));
                holder.gravity.setPadding(spaceFromSide*dpAsPixels,0,spaceFromSide*dpAsPixels,3*dpAsPixels);
                break;
            case Message.TIME:
                holder.subjectOfMessage.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                holder.bodyOfMessage.setClickable(false);
                holder.bodyOfMessage.setText(message.getSubject());
                holder.gravity.setGravity(Gravity.CENTER);
                holder.parent_layout.setBackground(context.getResources().getDrawable(R.drawable.border3));
                holder.gravity.setPadding(spaceFromSide*dpAsPixels,0,spaceFromSide*dpAsPixels,0);
                break;
        }
        if(message.hasAttachments()){
            holder.recyclerView.setHasFixedSize(false);
            ConversationAttachmentAdapter caa = new ConversationAttachmentAdapter(message.getMessageId(),new DatabaseHelper(context));
            holder.recyclerView.setAdapter(caa);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.wrapper.setLayoutParams(original);
        }
        else{
            size = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
            holder.wrapper.setLayoutParams(size);
        }
    }

    @Override
    public int getItemCount() {return messageList.size();}

}

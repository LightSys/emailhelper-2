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
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

import java.util.List;

/**
 * Created by nicholasweg on 7/10/17.
 */

public class ConversationWindowAdapter extends RecyclerView.Adapter<ConversationWindowAdapter.MyViewHolder> {

    private List<Message> messageList;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public LinearLayout parent_layout;
        public RecyclerView recyclerView;

        public MyViewHolder(View view) {
            super(view);
            message = view.findViewById(R.id.message);
            parent_layout = view.findViewById(R.id.chat_bubble_parent);
            recyclerView = view.findViewById(R.id.attachment_view);
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


        holder.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent messageActivity = new Intent(context,MessageActivity.class);
                messageActivity.putExtra(context.getResources().getString(R.string.intent_message_id),message.getMessageId());
                context.startActivity(messageActivity);
            }
        });

        Resources r = context.getResources();
        String display;
        ViewGroup.LayoutParams size;
        switch(message.getSent()){
            case Message.SENT_BY_ME:
                holder.message.setText(message.getMessage());
                holder.parent_layout.setGravity(Gravity.END);
                holder.message.setBackground(context.getResources().getDrawable(R.drawable.border));
                holder.message.setClickable(false);
                size = new ViewGroup.LayoutParams(0,0);
                holder.recyclerView.setLayoutParams(size);
                break;
            case Message.SENT_BY_OTHER:
                display = message.getSubject() + "\n"+ message.getMessage();
                holder.message.setText(display);
                holder.message.setClickable(true);
                holder.parent_layout.setGravity(Gravity.START);
                holder.message.setBackground(context.getResources().getDrawable(R.drawable.border2));
                holder.recyclerView.setBackground(context.getResources().getDrawable(R.drawable.border2));
                size = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                holder.recyclerView.setLayoutParams(size);
                break;
            case Message.TIME:
                display = message.getSubject();
                holder.message.setClickable(false);
                holder.message.setText(display);
                holder.parent_layout.setGravity(Gravity.CENTER);
                holder.message.setBackground(context.getResources().getDrawable(R.drawable.border3));
                size = new ViewGroup.LayoutParams(0,0);
                holder.recyclerView.setLayoutParams(size);
                break;
        }
        if(message.hasAttachments()){
            holder.recyclerView.setHasFixedSize(true);
            ConversationAttachmentAdapter caa = new ConversationAttachmentAdapter(message.getMessageId(),new DatabaseHelper(context));
            holder.recyclerView.setAdapter(caa);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        else{
            size = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
            holder.recyclerView.setLayoutParams(size);
        }
    }

    @Override
    public int getItemCount() {return messageList.size();}

}

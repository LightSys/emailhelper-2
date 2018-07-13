package org.lightsys.emailhelper.Conversation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    private List<ConversationWindow> conversationWindowList;
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

    public ConversationWindowAdapter(List<ConversationWindow> conversationWindowList, Context c) {
        this.conversationWindowList = conversationWindowList;
        context = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_window, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ConversationWindow conversationWindow = conversationWindowList.get(position);
        holder.message.setText(conversationWindow.getMessage());
        Resources r = context.getResources();
        SharedPreferences sp = context.getSharedPreferences(r.getString(R.string.preferences), CommonMethods.SHARED_PREFERENCES_DEFAULT_MODE);
        if(sp.getBoolean(r.getString(R.string.key_link_messages),r.getBoolean(R.bool.default_link_messages))){
            Linkify.addLinks(holder.message,Linkify.ALL);
        }

        if (conversationWindow.getSent()) {//This line analyzes sent
            holder.parent_layout.setGravity(Gravity.END);
            holder.message.setBackground(context.getResources().getDrawable(R.drawable.border));
        }
        else {
            holder.parent_layout.setGravity(Gravity.START);
            holder.message.setBackground(context.getResources().getDrawable(R.drawable.border2));
            holder.recyclerView.setBackground(context.getResources().getDrawable(R.drawable.border2));
        }
        if(conversationWindow.hasAttachments()){
            holder.recyclerView.setHasFixedSize(true);
            ConversationAttachmentAdapter caa = new ConversationAttachmentAdapter(conversationWindow.getMessageId(),new DatabaseHelper(context));
            holder.recyclerView.setAdapter(caa);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        else{
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
            holder.recyclerView.setLayoutParams(size);
        }
    }

    @Override
    public int getItemCount() {return conversationWindowList.size();}

}

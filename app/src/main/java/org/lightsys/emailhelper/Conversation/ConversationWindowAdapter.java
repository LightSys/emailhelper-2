package org.lightsys.emailhelper.Conversation;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        public LinearLayout layout;
        public LinearLayout parent_layout;
        public RecyclerView recyclerView;

        public MyViewHolder(View view) {
            super(view);
            message = view.findViewById(R.id.message);
            layout = view.findViewById(R.id.bubble_layout);
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_window_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ConversationWindow conversationWindow = conversationWindowList.get(position);
        holder.message.setText(conversationWindow.getMessage());

        if (conversationWindow.getSent() == true) {
            holder.parent_layout.setGravity(Gravity.RIGHT);

        }
        else {
            holder.parent_layout.setGravity(Gravity.LEFT);
        }
        if(conversationWindow.hasAttachments()){
            holder.recyclerView.setHasFixedSize(false);
            ConversationAttachmentAdapter caa = new ConversationAttachmentAdapter(conversationWindow.getEmail(),conversationWindow.getMessageId(),new DatabaseHelper(context));
            holder.recyclerView.setAdapter(caa);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }

    @Override
    public int getItemCount() {return conversationWindowList.size();}

}

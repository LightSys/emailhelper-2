package org.lightsys.emailhelper.Conversation;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.lightsys.emailhelper.R;

import java.util.List;

/**
 * Created by nicholasweg on 7/10/17.
 */

public class ConversationWindowAdapter extends RecyclerView.Adapter<ConversationWindowAdapter.MyViewHolder> {

    private List<ConversationWindow> conversationWindowList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public LinearLayout layout;
        public LinearLayout parent_layout;

        public MyViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.message);
            layout = (LinearLayout) view.findViewById(R.id.bubble_layout);
            parent_layout = (LinearLayout) view.findViewById(R.id.chat_bubble_parent);
        }
    }

    public ConversationWindowAdapter(List<ConversationWindow> conversationWindowList) {this.conversationWindowList = conversationWindowList;}

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
//            holder.layout.setBackgroundResource(R.drawable.bubble2);
            holder.parent_layout.setGravity(Gravity.RIGHT);
        }
        else {
//            holder.layout.setBackgroundResource(R.drawable.bubble1);
            holder.parent_layout.setGravity(Gravity.LEFT);
        }
    }

    @Override
    public int getItemCount() {return conversationWindowList.size();}

}

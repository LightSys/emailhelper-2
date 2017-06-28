package com.example.ben.emailhelper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nicholasweg on 6/28/17.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MyViewHolder> {

    private List<Conversation> conversationList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView email, name, time;

        public MyViewHolder(View view) {
            super(view);
            email = (TextView) view.findViewById(R.id.email);
            name = (TextView) view.findViewById(R.id.name);
            time = (TextView) view.findViewById(R.id.time);
        }
    }

    public ConversationAdapter(List<Conversation> conversationList) {this.conversationList = conversationList;}

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.conversation_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.email.setText(conversation.getEmail());
        holder.name.setText(conversation.getName());
        holder.time.setText(conversation.getTime());
    }

    @Override
    public int getItemCount() {return conversationList.size();}
}

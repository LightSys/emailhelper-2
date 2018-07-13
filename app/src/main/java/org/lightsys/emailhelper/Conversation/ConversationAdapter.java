package org.lightsys.emailhelper.Conversation;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.R;

import java.util.Calendar;
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
            email = view.findViewById(R.id.email);
            name = view.findViewById(R.id.name);
            time = view.findViewById(R.id.time);
        }
    }

    public ConversationAdapter(List<Conversation> conversationList) {this.conversationList = conversationList;}

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.name.setTypeface(null, Typeface.NORMAL);
        if(conversation.getMailStatus()){
            holder.name.setTypeface(null,Typeface.BOLD);
        }
        holder.email.setText(conversation.getEmail());
        holder.name.setText(conversation.getName());
        String temp = conversation.getLastDate();
        String temp2 = CommonMethods.getDate(Calendar.getInstance().getTime());
        if(!temp.equalsIgnoreCase(temp2)){
            String date = conversation.getLastDate();
            if(date.length() > 10){
                date = date.substring(0,6);
            }
            else{
                date = date.substring(0,5);
            }
            String message = date + " "+ conversation.getTime();
            holder.time.setText(message);
        }else{
            holder.time.setText(conversation.getTime());
        }
    }

    @Override
    public int getItemCount() {return conversationList.size();}
}

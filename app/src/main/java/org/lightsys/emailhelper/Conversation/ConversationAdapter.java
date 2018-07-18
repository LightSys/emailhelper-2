package org.lightsys.emailhelper.Conversation;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by nicholasweg on 6/28/17.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MyViewHolder> {

    private List<Conversation> conversationList;
    DatabaseHelper db;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView email, name, time;


        public MyViewHolder(View view) {
            super(view);
            email = view.findViewById(R.id.email);
            name = view.findViewById(R.id.name);
            time = view.findViewById(R.id.time);
        }
    }

    public ConversationAdapter(List<Conversation> conversationList,Context context) {
        this.conversationList = conversationList;
        db = new DatabaseHelper(context);
        Collections.sort(conversationList,new Comparator<Conversation>() {//need a sort that will go back farther
            @Override
            public int compare(Conversation o1, Conversation o2) {
                Date date1 = db.getContactUpdatedDate(o1.getEmail());
                Date date2 = db.getContactUpdatedDate(o2.getEmail());
                if(date1.before(date2)){
                    return 1;
                }
                else if(date1.after(date2)){
                    return -1;
                }
                else{
                    return 0;
                }
            }
        });
    }


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
        Date set = db.getContactUpdatedDate(conversation.getEmail());
        Date today = CommonMethods.getCurrentTime();
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);
        Date lastMonth = CommonMethods.getCurrentTime();
        lastMonth.setDate(0);
        lastMonth.setHours(0);
        lastMonth.setMinutes(0);
        lastMonth.setSeconds(0);
        Date lastYear = CommonMethods.getCurrentTime();
        lastYear.setMonth(0);
        lastYear.setDate(0);
        lastYear.setHours(0);
        lastYear.setMinutes(0);
        lastYear.setSeconds(0);
        if(today.before(set)){
            holder.time.setText(CommonMethods.getTime(set));
        }
        else if(today.after(set) && lastMonth.before(set)){
            holder.time.setText(CommonMethods.getDateAndTime(set));
        }
        else if(lastMonth.after(set) && lastYear.before(set)){
            holder.time.setText(CommonMethods.getDate(set));
        }
        else if(lastYear.after(set)){
            holder.time.setText(CommonMethods.getMonthAndYear(set));
        }
    }

    @Override
    public int getItemCount() {return conversationList.size();}
}

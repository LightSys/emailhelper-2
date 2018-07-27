package org.lightsys.emailhelper.Conversation;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.lightsys.emailhelper.CommonMethods;
import org.lightsys.emailhelper.DatabaseHelper;
import org.lightsys.emailhelper.R;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * This class is the adapter between the Conversation fragment and the Conversations in the database.
 * Created by nicholasweg on 6/28/17.
 * Edited by DSHADE 2018.
 * This class could get put into the class that uses it but MainActivity is already cluttered.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MyViewHolder> {

    private List<Conversation> conversationList;
    private DatabaseHelper db;
    private int size;


    /**
     * This Constructor sets the conversation list and sorts it. It also uses the context to initialize
     * a connection to the database.
     */
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
        size = conversationList.size();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation_row, parent, false);
        return new MyViewHolder(itemView);
    }

    /**
     * Connects the data to the views
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.name.setTypeface(null, Typeface.NORMAL);
        if(conversation.getMailStatus()){
            holder.name.setTypeface(null,Typeface.BOLD);
        }
        holder.email.setText(conversation.getEmail());
        holder.name.setText(conversation.getName());
        setTime(holder,conversation);
    }

    @Override
    public int getItemCount() {return size;}

    /**
     * This function is a very nasty way of getting the time for the view.
     * Will need refactored to get rid of depreceated functions.
     */
    private void setTime(MyViewHolder holder,Conversation convo) {
        Date set = db.getContactUpdatedDate(convo.getEmail());
        Calendar mCal = Calendar.getInstance();
        mCal.set(Calendar.MILLISECOND,0);
        mCal.set(Calendar.SECOND,0);
        mCal.set(Calendar.MINUTE,0);
        mCal.set(Calendar.HOUR,0);
        mCal.set(Calendar.AM_PM,Calendar.AM);
        Date today = mCal.getTime();
        mCal.set(Calendar.DAY_OF_MONTH,1);
        Date lastMonth = mCal.getTime();
        mCal.set(Calendar.MONTH,0);
        Date lastYear = mCal.getTime();
        if(today.before(set)){
            holder.time.setText(CommonMethods.getTime(set));
        }
        else if(today.after(set) && lastMonth.before(set)){
            holder.time.setText(CommonMethods.getDate(set));
        }
        else if(lastMonth.after(set) && lastYear.before(set)){
            holder.time.setText(CommonMethods.getMonth(set));
        }
        else if(lastYear.after(set)){
            holder.time.setText(CommonMethods.getYear(set));
        }
    }


    /**
     * Inner class used for the adapter.
     */
    protected class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView email, name, time;
        public MyViewHolder(View view) {
            super(view);
            email = view.findViewById(R.id.email);
            name = view.findViewById(R.id.name);
            time = view.findViewById(R.id.time);
        }
    }
}
